package main;

import main.TerminusEst.*;
import main.mcts.*;
import main.mcts.base.Action;
import main.mcts.base.MCTS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

public class TerminusEstMCTS {

    public static boolean VERBOSE = true;

    public static int iterations = 100000;
    public static int simulations = 30;

    public NodeMCTS GetSearchTree(String file) {
        SelectUCT_SP select = new SelectUCT_SP();
        SelectUCT_SP.param_d = 20000;
        HeuristicNegativeDepth  heuristic = new HeuristicNegativeDepth();
        ResultUCT_SPGenerator gen = new ResultUCT_SPGenerator();
        SimulateRandom sim = new SimulateRandom(simulations, heuristic, gen);
        MCTS mcts = new MCTS(iterations, select, sim, gen);

        TerminusEstV4 te4 = new TerminusEstV4(file);

        Tree T1 = te4.t1.copy(null, null);
        Tree T2 = te4.t2.copy(null, null);
        TerminusEstState state = new TerminusEstState(T1, T2, 0);

        mcts.BuildTree(state);

        return mcts.root;
    }

    public static TerminusEstSolution GetExactSolution(String file) {
        TerminusEstMCTS tem = new TerminusEstMCTS();

        double timeStart = System.currentTimeMillis();

        NodeMCTS searchTree = tem.GetSearchTree(file);
        double timeEndSearchTree = System.currentTimeMillis();
        if (VERBOSE) System.out.println("Search tree constructed.");

        NodeMCTS bestFound = GetBestFound(searchTree);

        double seconds =  ((double)(timeEndSearchTree - timeStart))/1000.0;

        TerminusEstV4 te4 = new TerminusEstV4(file);

        int upperBound;
        if (bestFound == null) {
            // No solution found at all.
            // (Increase by one to ensure we also check for the case in which we have do delete *all* leaves).
            upperBound = te4.seenLeaves + 1;
        }
        else {
            // Best solution in tree is upper bound.
            upperBound = bestFound.depth;
        }

        // Find all candidate tree nodes to search from.
        NodeMCTS[][] searchNodes = GetCandidateLeaves(searchTree, upperBound);
        if (VERBOSE) System.out.println("Nodes to search from collected.");

        TerminusEstSolution solution = null;
        NodeMCTS solutionNode = null;
        // i = the current depth we're trying to compute at
        for (int i = 0; i < upperBound; ++i) {
            if (VERBOSE) System.out.println("Attempting hyb = " + i);
            // j = All search nodes at depth i which we wish to investigate.
            for (int j = 0; j < searchNodes.length; ++j) {
                for (int k = 0; k < searchNodes[j].length; ++k) {
                    // Construct trees we are searching from.
                    NodeMCTS node = searchNodes[j][k]; // Make it easier to reference this node.
                    TerminusEstState s = (TerminusEstState) node.ConstructNodeState();
                    solution = te4.ComputePartialSolution(s.t1, s.t2, node.depth, i);
                    solutionNode = node;

                    if (solution != null) {
                        if (VERBOSE) {
                            System.out.println("A solution was found!");
                            System.out.println(solution.toString());
                            System.out.println("At level: " + i);
                            System.out.println("Node:\n" + node.GetNewick());
                        }

                        break;
                    }
                }
            }
        } // End for-loop

        // Construct the network.
        TerminusEstState orig = (TerminusEstState) bestFound.root;
        Network currentNetwork = null;
        NodeMCTS nextNode = null;
        if (solution != null) {
            // Initial network already has been  constructed.
            currentNetwork = solution.root;

            if (solutionNode.IsRoot()) {
                System.out.println("Solution node was root, somewhow.");
                return new TerminusEstSolution(currentNetwork, 0, 0);
            }

            nextNode = solutionNode;
        }
        else if (bestFound != null) {
            // Construct using bestFound.
            // No solution was found through the search. construct initial solution from bestFound.
            TerminusEstState finalState = ((TerminusEstState) bestFound.ConstructNodeState());
            Vector ST = Tree.computeMaxSTsets(finalState.t1, finalState.t2);
            if (ST.size() != 1) {
                System.out.println("ERROR: best found trees not compatible.");
                System.exit(0);
            }

            currentNetwork = te4.ConstructInitialNetwork(ST, finalState.t1, finalState.t2, orig.t1, orig.t2, finalState.depth);

            if (bestFound.IsRoot()) { // Small subcase.
                System.out.println("BestFound was root.");
                return new TerminusEstSolution(currentNetwork, 0, 0);
            }

            nextNode = bestFound;
        }

        // Use solution node to construct
        do {
            TerminusEstState nextState = ((TerminusEstState) nextNode.ConstructNodeState());
            TerminusEstAction a = (TerminusEstAction) nextNode.GetLastAction();
            currentNetwork = te4.GrowNetwork(a.taxon, currentNetwork, orig.t1, orig.t2);
            nextNode = nextNode.parent;
        } while (!nextNode.IsRoot());

        if (VERBOSE) System.out.println("Network fully constructed.");

        return new TerminusEstSolution(currentNetwork, 0, 0);
    }

    private static class SortByVisits implements Comparator<NodeMCTS> {
        @Override
        public int compare(NodeMCTS o1, NodeMCTS o2) {
            if ( ((ResultUCT_SP) o1.GetResult()).simulations > ((ResultUCT_SP) o2.GetResult()).simulations ) {
                return 1;
            }
            else if ( ((ResultUCT_SP) o1.GetResult()).simulations < ((ResultUCT_SP) o2.GetResult()).simulations ) {
                return -1;
            }

            return 0;
        }
    }

    /**
     * Method to get all leaves that are less than max-depth.
     * @param root
     * @param maxDepth
     * @return
     */
    public static NodeMCTS[][] GetCandidateLeaves(NodeMCTS root, int maxDepth) {
        ArrayList<NodeMCTS> n = new ArrayList<>();
        SortByVisits comp = new SortByVisits();
        int[] depthCount = new int[maxDepth];
        DFSWithSort(root, n, comp, maxDepth, depthCount);

        // Create array container.
        NodeMCTS[][] nodeByDepth = new NodeMCTS[maxDepth][];
        for (int i = 0; i < nodeByDepth.length; ++i) {
            nodeByDepth[i] = new NodeMCTS[depthCount[i]];
        }

        int[] count = new int[maxDepth];

        // Add everything!
        for (NodeMCTS node : n) {
            nodeByDepth[node.depth][count[node.depth]] = node;
            count[node.depth] += 1;
        }

        return nodeByDepth;
    }

    /**
     * Adds all leaf nodes, traversing in a DFS manner, visiting according to order imposed by the given comparator.
     * @param node
     * @param list
     * @param comp
     */
    private static void DFSWithSort(NodeMCTS node, ArrayList<NodeMCTS> list, Comparator<NodeMCTS> comp, int maxDepth, int[] depthCount) {
        if (node.IsTerminal()) {
            return;
        }

        // Only add nodes below the maxDepth
        if (node.depth >= maxDepth)
            return;

        // Only add if leaf and under max depth.
        if (node.IsLeaf()) {
            list.add(node);
            depthCount[node.depth] += 1;
            return;
        }

        NodeMCTS[] children = new NodeMCTS[node.children.length];
        System.arraycopy(node.children, 0, children, 0, node.children.length);
        Arrays.sort(children, comp);

        for (int i = 0; i < children.length; ++i) {
            DFSWithSort(children[i], list, comp, maxDepth, depthCount);
        }
    }

    public static TerminusEstSolution AttemptSolution(String file) {
        TerminusEstMCTS tem = new TerminusEstMCTS();

        double timeNow = System.currentTimeMillis();

        NodeMCTS searchTree = tem.GetSearchTree(file);
        NodeMCTS bestFound = GetBestFound(searchTree);

        double timeEnd = System.currentTimeMillis();
        double seconds =  ((double)(timeEnd - timeNow))/1000.0;

        if (bestFound != null) {
            Action[] actionSequence = bestFound.GetActionSequenceFromRoot();

            // Verify. If we are not able to, return empty.
            String[] taxons = new String[actionSequence.length];
            for (int i = 0; i < actionSequence.length; ++i) {
                taxons[i] = ((TerminusEstAction) actionSequence[i]).taxon.getName();
            }

            TerminusEstState s = (TerminusEstState) searchTree.root;
            if (!TerminusEstV4.verifyHybNum(s.t1, s.t2, taxons)) {
                return new TerminusEstSolution(null, -2, seconds);
            }

            // TODO: Also extract the network.
            return new TerminusEstSolution(null, bestFound.depth, seconds);
        } else {
            return new TerminusEstSolution(null, -1, seconds);
        }
    }

    public static NodeMCTS GetBestFound(NodeMCTS node) {
        if (node.IsTerminal()) {
            return node;
        }

        if (!node.expanded && node.leaf) {
            return null;
        }

        NodeMCTS bestChild = null;
        for (int i = 0; i < node.children.length; ++i) {
            NodeMCTS option = GetBestFound(node.children[i]);
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

    public static void RunSingleInstance(String file) {
        TerminusEstMCTS tem = new TerminusEstMCTS();
        System.out.println("Beginning MCTS: ");
        NodeMCTS searchTree = tem.GetSearchTree(file);
        System.out.println("\n\nMCTS completed.");
        NodeMCTS bestFound = GetBestFound(searchTree);

        if (bestFound != null) {
        System.out.println("Best solution found has depth: " + bestFound.depth);
        Action[] actionSequence = bestFound.GetActionSequenceFromRoot();
            for (int i = 0; i < actionSequence.length; ++i) {
                System.out.println(i + ": " + actionSequence[i].toString());
            }

            // Verify
            System.out.println("Verifying solution: ");
            String[] taxons = new String[actionSequence.length];
            for (int i = 0; i < actionSequence.length; ++i) {
                taxons[i] = ((TerminusEstAction) actionSequence[i]).taxon.getName();
            }

            TerminusEstState s = (TerminusEstState) searchTree.root;
            System.out.println(" > " +TerminusEstV4.verifyHybNum(s.t1, s.t2, taxons));

        } else {
            System.out.println("Didn't find a solution.");
        }

        System.out.println("\n\nNewick format search tree:");
        System.out.println(searchTree.GetNewick());
    }

    public static void main(String[] args) {
        // TerminusEstSolution solution = AttemptSolution(args[0]);
        // System.out.println(solution.toString());

        // RunSingleInstance(args[0]);

        GetExactSolution(args[0]);
    }

    
    /*public static void Testing(String file) {
        TerminusEstInputHandler inp = new TerminusEstInputHandler();
        inp.InterpretFile(file);
        Tree T1 = TerminusEstV4.t1.copy(null, null);
        Tree T2 = TerminusEstV4.t2.copy(null, null);
        TerminusEstState state = new TerminusEstState(T1, T2, 0);

        Action[] a = state.GetLegalActions();
        state.Apply(a[0]);
        a = state.GetLegalActions();
        state.Apply(a[2]);
        a = state.GetLegalActions();
        state.Apply(a[2]);
        a = state.GetLegalActions();
    }*/
}
