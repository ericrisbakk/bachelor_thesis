package main;

import main.TerminusEst.TerminusEstAction;
import main.TerminusEst.TerminusEstState;
import main.TerminusEst.TerminusEstV4;
import main.TerminusEst.Tree;
import main.mcts.*;
import main.mcts.base.MCTS;
import main.mcts.processing.Traversal;
import main.utility.Tuple2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;

/**
 * Manages the aspects of the search tree created by MCTS.
 */
public class TerminusEstMC_SearchTree {

    public static boolean VERBOSE = false;

    // Params set at construction.
    public int trees;
    public int iterations;
    public int simulations;
    public double param_c;
    public double param_d;
    public TerminusEstMCTS manager;

    // Created dynamically
    public NodeMCTS[] searchTrees;
    public Hashtable<String, Double>[] scores;
    public Hashtable<String, Double> heuristic;


    public TerminusEstMC_SearchTree(int trees, int iterations, int simulations, double param_c, double param_d, TerminusEstMCTS manager) {
        this.trees = trees;
        this.iterations = iterations;
        this.simulations = simulations;
        this.param_c = param_c;
        this.param_d = param_d;
        this.manager = manager;
    }

    public NodeMCTS GetSearchTree(TerminusEstV4 te4) {
        SelectUCT_SP select = new SelectUCT_SP();
        SelectUCT_SP.param_c = param_c;
        SelectUCT_SP.param_d = param_d;
        HeuristicNegativeDepth heuristic = new HeuristicNegativeDepth();
        ResultUCT_SPGenerator gen = new ResultUCT_SPGenerator();
        SimulateRandom sim = new SimulateRandom(simulations, heuristic, gen);
        MCTS mcts = new MCTS(iterations/trees, select, sim, gen);

        Tree T1 = te4.t1.copy(null, null);
        Tree T2 = te4.t2.copy(null, null);
        TerminusEstState state = new TerminusEstState(T1, T2, 0);

        if (manager.VERBOSE) System.out.println("Building search tree.");
        manager.timeSinceLastSearchTreeBuilt = System.currentTimeMillis();
        mcts.BuildTree(state);
        manager.timeSinceLastSearchTreeCompleted = System.currentTimeMillis();
        if (manager.VERBOSE) System.out.println("Search tree completed.");
        return mcts.root;
    }

    public NodeMCTS[] GetSearchTrees() {
        return searchTrees;
    }

    public void CreateSearchTrees(TerminusEstV4 te4) {
        searchTrees = new NodeMCTS[trees];
        for (int i = 0; i < searchTrees.length; ++i) {
            searchTrees[i] = GetSearchTree(te4);
        }
    }

    public NodeMCTS GetBestInTree(NodeMCTS node) {
        if (node.IsTerminal()) {
            return node;
        }

        if (!node.expanded && node.leaf) {
            return null;
        }

        NodeMCTS bestChild = null;
        for (int i = 0; i < node.children.length; ++i) {
            NodeMCTS option = GetBestInTree(node.children[i]);
            if (option != null) {
                if (bestChild == null)
                    bestChild = option;
                else {
                    if (option.depth < bestChild.depth)
                        bestChild = option;
                }
            }
        }

        return bestChild;
    }

    /**
     * Run multiple times and return the single best tree found, according to the best solution discovered in that tree.
     * @return
     */
    public Tuple2<NodeMCTS, NodeMCTS> GetBestTreeAndLeaf() {
        NodeMCTS[] trees = searchTrees;
        NodeMCTS root = trees[0];
        NodeMCTS child = GetBestInTree(trees[0]);
        for (int i = 1; i < trees.length; ++i) {
            NodeMCTS next = GetBestInTree(trees[i]);
            if (next != null) {
                if (child == null) {
                    child = next;
                    root = trees[i];
                }
                else if (child.depth > next.depth) {
                    child = next;
                    root = trees[i];
                }
            }
        }

        return new Tuple2<>(root, child);
    }

    public void ComputeHeuristic() {
        scores = new Hashtable[searchTrees.length];
        for (int i = 0; i < scores.length; ++i) {
            scores[i] = ScoreActions(searchTrees[i]);
        }

        heuristic = CombineScores(scores);
    }

    /**
     * TODO: FINISH THIS.
     * Call to score actions of a single tree.
     * @param root
     * @return
     */
    private Hashtable<String, Double> ScoreActions(NodeMCTS root) {
        ActionHeuristicTraversal aht = new ActionHeuristicTraversal();
        aht.StartDepthFirstTraversal(root);
        Hashtable<String, Double> ht = aht.scores;
        return ht;
    }

    private Hashtable<String, Double> CombineScores(Hashtable<String, Double>[] hTables) {
        // Get all keys.
        Set<String> keys = hTables[0].keySet();
        for (int i = 1; i < hTables.length; ++i) {
            keys.addAll(hTables[i].keySet());
        }

        Hashtable<String, Double> combined = new Hashtable<>();
        for (String id:
             keys) {
            double sum = 0;
            int count = 0;
            for (int i = 0; i < hTables.length; ++i) {
                if (hTables[i].containsKey(id)) {
                    sum += hTables[i].get(id);
                    count += 1;
                }
            }
            combined.put(id, sum/count);
        }

        return combined;
    }

    /**
     * Collect the leaves of a search tree, if we want to use it as a jumping point for search.
     */
    public static class CollectLeaves extends Traversal {

        public int LeafCollection_NodesTraversed;
        public int LeafCollection_Duplicates;
        public ArrayList<NodeMCTS> nodes;
        public Hashtable<String, NodeMCTS> uniques;
        public int[] depthCount;
        public TerminusEstV4 te4;
        public int maxDepth;

        // Out.
        NodeMCTS[][] nodeByDepth;

        public CollectLeaves(int maxDepth, TerminusEstV4 te4) {
            LeafCollection_Duplicates = 0;
            LeafCollection_NodesTraversed = 0;
            nodes = new ArrayList<>();
            uniques = new Hashtable<>();
            depthCount = new int[maxDepth];
            this.maxDepth = maxDepth;
            this.te4 = te4;
            comp = new SortByVisits();
        }

        @Override
        public void PostProcess() {
            // Create array container.
            nodeByDepth = new NodeMCTS[maxDepth][];
            for (int i = 0; i < nodeByDepth.length; ++i) {
                nodeByDepth[i] = new NodeMCTS[depthCount[i]];
            }

            int[] count = new int[maxDepth];

            // Add everything!
            for (NodeMCTS node : nodes) {
                nodeByDepth[node.depth][count[node.depth]] = node;
                count[node.depth] += 1;
            }
        }

        public NodeMCTS[][] GetNodeByDepth() {
            return nodeByDepth;
        }

        @Override
        public void BeforeStop(NodeMCTS n) {
            LeafCollection_NodesTraversed += 1;
        }

        @Override
        public boolean StopCondition(NodeMCTS n) {
            if (n.IsTerminal() || n.depth >= maxDepth || n.IsLeaf())
                return true;

            return false;
        }

        @Override
        public void OnStop(NodeMCTS n) {
            if (n.IsLeaf() && !n.IsTerminal() && n.depth < maxDepth) {
                if (AddUnique(n)) {
                    nodes.add(n);
                    depthCount[n.depth] += 1;
                }
                else {
                    LeafCollection_Duplicates += 1;
                }
            }
        }

        /**
         * @param n
         * @return true if n is not in the list of uniques, and adds n to uniques, false otherwise.
         */
        private boolean AddUnique(NodeMCTS n) {
            TerminusEstState s = (TerminusEstState) n.ConstructNodeState();
            String bitString = te4.GetBitString(s.t1, s.t2);
            if (uniques.containsKey(bitString)) {
                return false;
            }

            uniques.put(bitString, n);
            return true;
        }
    }

    /**
     * Traverses an MCTS tree to compute heuristic scores for all actions encountered.
     */
    public static class ActionHeuristicTraversal extends Traversal {

        public Hashtable<String, Double> sums;
        public Hashtable<String, Long> count;
        public Hashtable<String, Double> scores;

        public ActionHeuristicTraversal() {
            sums = new Hashtable<>();
            count = new Hashtable<>();
        }

        @Override
        public boolean StopCondition(NodeMCTS n) {
            if (n.leaf)
                return true;

            if (n.expandedChildren >= n.children.length)
                return false;

            return true;
        }

        /**
         * Go through contents of sum and count to create final score.
         */
        @Override
        public void PostProcess() {
            scores = new Hashtable<>();
            for (var a:
                 sums.keySet()) {
                scores.put(a, sums.get(a)/count.get(a));
            }
        }

        /**
         * Go through all children of this node and log value/sum.
         * @param n
         */
        @Override
        public void BeforeSort(NodeMCTS n) {
            for (var c:
                 n.children) {
                AddScore(c,n);
            }
        }

        private void AddScore(NodeMCTS c, NodeMCTS n) {
            String a = ((TerminusEstAction) c.lastAction).toString();
            if (sums.containsKey(a)) {
                sums.put(a, sums.get(a) + GetScore(c, n) );
                count.put(a, count.get(a) + 1);
            }
            else {
                sums.put(a, GetScore(c, n));
                count.put(a, (long) 1);
            }
        }

        private double GetScore(NodeMCTS c, NodeMCTS p) {
            return ((double) GetVisits(c) / (double) GetVisits(p));
        }

        private long GetVisits(NodeMCTS n) {
            return ((ResultUCT_SP) n.result).simulations;
        }
    }

    /**
     * Comparator to be used in Arrays.sort. Sorts nodes with more visits in front of nodes with less.
     */
    public static class SortByVisits implements Comparator<NodeMCTS> {
        @Override
        public int compare(NodeMCTS o1, NodeMCTS o2) {
            if ( ((ResultUCT_SP) o1.GetResult()).simulations > ((ResultUCT_SP) o2.GetResult()).simulations ) {
                return -1;
            }
            else if ( ((ResultUCT_SP) o1.GetResult()).simulations < ((ResultUCT_SP) o2.GetResult()).simulations ) {
                return 1;
            }

            return 0;
        }
    }
}
