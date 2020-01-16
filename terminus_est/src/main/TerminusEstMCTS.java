package main;

import main.TerminusEst.*;
import main.mcts.*;
import main.mcts.base.*;
import main.utility.Tuple2;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TerminusEstMCTS {

    public static final boolean VERBOSE = false;
    public static final boolean PARALLEL = false;
    public static final boolean USE_HEURISTIC = false;
    public static final boolean USE_TREE = false;
    public static final boolean SORT_BY_DEPTH = false;

    public int iterations = 100000;
    public int simulations = 10;
    public double param_c = Math.sqrt(2);
    public double param_d = 1000;

    public int LeafCollection_NodesTraversed = 0;
    public int LeafCollection_Duplicates = 0;

    public long timeStart;
    public long timeSinceLastSearchTreeBuilt;
    public long timeSinceLastSearchTreeCompleted;

    public TerminusEstMC_SearchTree searchTreeUtil;

    private TerminusEstV4 te4;
    private NodeMCTS searchTree;
    private NodeMCTS bestFound;
    private Hashtable<String, Double> heuristic;
    private NodeMCTS[] sorted;

    // Searching
    TerminusEstSolution solution;
    NodeMCTS solutionNode;
    ExecutorService exec;

    // Data
    private ExperimentData data;
    private int upperBound;
    private int lowerBound;

    public TerminusEstMCTS() {
        searchTreeUtil = new TerminusEstMC_SearchTree(1, iterations, simulations, param_c, param_d, this);
    }

    public TerminusEstMCTS(int iterations, int simulations, double param_c, double param_d) {
        this.iterations = iterations;
        this.simulations = simulations;
        this.param_c = param_c;
        this.param_d = param_d;

        searchTreeUtil = new TerminusEstMC_SearchTree(1, iterations, simulations, param_c, param_d, this);
    }

    public TerminusEstMCTS(int iterations, int simulations, double param_c, double param_d, int trees) {
        this.iterations = iterations;
        this.simulations = simulations;
        this.param_c = param_c;
        this.param_d = param_d;

        searchTreeUtil = new TerminusEstMC_SearchTree(trees, iterations, simulations, param_c, param_d, this);
    }

    private void ExperimentSetup(String fName, double maxTime) {
        // Setup.
        if (VERBOSE) System.out.println("\n\nRunning experiment for: " + fName);
        data = new ExperimentData();
        data.fName = fName;
        te4 = new TerminusEstV4(fName);

        searchTreeUtil.CreateSearchTrees(te4);
        // Get best search tree to start from, and the heuristic.
        Tuple2<NodeMCTS, NodeMCTS> b = searchTreeUtil.GetBestTreeAndLeaf();
        searchTree = b.item1;
        bestFound = b.item2;

        if (USE_HEURISTIC) {
            searchTreeUtil.ComputeHeuristic();
            heuristic = searchTreeUtil.heuristic;
            te4.UseHeuristic(heuristic);
        }

        SetUpperBound();

        timeStart = timeSinceLastSearchTreeBuilt;
        data.timeBuildingSearchTree = getIntervalInSeconds(timeSinceLastSearchTreeCompleted, timeSinceLastSearchTreeBuilt);

        if (USE_TREE){
            sorted = GetTree();
            if (SORT_BY_DEPTH) {
                SortByDepth(sorted);
            }
        } else {
            // Use arbitrary root as search start.
            sorted = new NodeMCTS[] {searchTree};
        }

        te4.startTime = timeStart;
        te4.setRuntime(maxTime);
    }

    private void Search() {
        // Time to run!
        solution = null;
        solutionNode = null;

        if (PARALLEL) {
            if (VERBOSE) System.out.println("Running parallel!");
            exec = Executors.newWorkStealingPool();
        } else {
            if (VERBOSE) System.out.println("Running single-threaded!");
        }

        // i = investigation decision problem r = i.
        data.hybLowerBound = lowerBound;
        for (int i = lowerBound; i < upperBound; ++i) {
            if (VERBOSE) System.out.println("Attempting hyb = " + i);
            if (VERBOSE) System.out.print("\tProgress: ");
            // j = All search nodes at depth i which we wish to investigate.
            NodeMCTS[] subset = GetSubset(sorted, i);

            if (subset.length > 0) {
                if (PARALLEL) {
                    ParallelSearch(subset, i);
                } else {
                    LinearSearch(subset, i);
                }
            }

                if (VERBOSE) System.out.println();

                data.hybLowerBound = i;
                if (solution != null || te4.isCanceled()) break;
        } // End of search
    }

    /**
     * Search the subset at hyb=i in a parallel fashion.
     * @param subset
     * @param i
     */
    private void ParallelSearch(NodeMCTS[] subset, int i) {
        // Create the parallel callables.
            ArrayList<TerminusEstParallel> l = new ArrayList<>(subset.length);
            for (int m = 0; m < subset.length; ++m) {
                TerminusEstState s = (TerminusEstState) subset[m].ConstructNodeState();
                l.add(new TerminusEstParallel(subset[m], te4, s.t1, s.t2, subset[m].depth, i-subset[m].depth, m));
            }

            try {
                Stream<TerminusEstSolution> strm = exec.invokeAll(l)
                        .stream()
                        .map(
                                future -> {
                                    try {
                                        return future.get();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    return null;
                                }
                        );

                ArrayList<TerminusEstSolution> possible = strm.collect(Collectors.toCollection(ArrayList::new));

                for (int m = 0; m < possible.size(); ++m) {

                    if (possible.get(m) != null) {
                        if (VERBOSE) {
                            System.out.println("A solution was found!");
                            System.out.println(solution.toString());
                            System.out.println("Hyb: " + i + "\tAt depth: (NOT SURE)");
                        }

                        // TODO: How to collect data?
                        data.hybNumExact = i;
                        data.solutionNodeDepth = -1;
                        data.solutionNodeInstance = -1;
                        data.solutionDepthTotalInstances = subset.length;
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    /**
     * Search the subset at hyb=i in a linear fashion.
     * @param subset
     * @param i
     */
    private void LinearSearch(NodeMCTS[] subset, int i) {
        for (int k = 0; k < subset.length; ++k) {

            if (VERBOSE) {
                int step = (subset.length/10) + 1;
                if (k%step == 0) {
                    System.out.print(". ");
                }
            }

            // Construct trees we are searching from.
            NodeMCTS node = subset[k]; // Make it easier to reference this node.
            TerminusEstState s = (TerminusEstState) node.ConstructNodeState();
            solution = te4.ComputePartialSolution(s.t1, s.t2, node.depth, i - node.depth);
            solutionNode = node;

            if (te4.isCanceled()) {
                if (VERBOSE) System.out.println("Time-out occurred!");
                data.timeTotal = 600;
                data.canceled = true;
                break;
            }

            if (solution != null) {
                if (VERBOSE) {
                    System.out.println("A solution was found!");
                    System.out.println(solution.toString());
                    System.out.println("Hyb: " + i + "\tAt depth: " + node.depth);
                }

                data.hybNumExact = i;
                data.solutionNodeDepth = node.depth;
                data.solutionNodeInstance = k;
                data.solutionDepthTotalInstances = subset.length;
                break;
            }
        }
    }

    /**
     * @param ar
     * @param depth
     * @return Subset array of mcts nodes with depth equal to or less than the one given.
     */
    private NodeMCTS[] GetSubset(NodeMCTS[] ar, int depth) {
        ArrayList<NodeMCTS> subset = new ArrayList<>();

        for (var n:
             ar) {
            if (n.depth <= depth)
                subset.add(n);
        }

        return subset.toArray(new NodeMCTS[subset.size()]);
    }

    /**
     * To be run after search has been performed. Creation of the network and any additional data gathering happens
     * in this section.
     */
    private void PostSearchEvaluation() {
        if (solution == null && bestFound != null && !te4.isCanceled()) {
            data.hybNumExact = bestFound.depth;
            data.solutionNodeDepth = bestFound.depth;
        }

        if (solution != null || bestFound != null) {
            data.timeTotal = getIntervalInSeconds(System.currentTimeMillis(), timeStart);
            Network net = buildNetwork(solution, solutionNode, bestFound, te4);

            if (VERBOSE) TerminusEstV4.DumpENewick(net);
            if (VERBOSE) System.out.println("Network constructed.");

            data.network = TerminusEstV4.GetENewick(net);
        } else {
            if (VERBOSE) System.out.println("No network constructed.");
        }
    }

    /**
     * Runs a single experiment on the problem found in the given fName, for the duration given.
     * @param fName filename of problem
     * @param maxTime max duration for search in seconds.
     */
    public ExperimentData RunExperiment(String fName, double maxTime, int lowerBound) {
        this.lowerBound = lowerBound;
        ExperimentSetup(fName, maxTime);
        Search();
        PostSearchEvaluation();
        if (VERBOSE) System.out.println("\nDATA:\n" + data.GetData());
        return data;
    }

    public ExperimentData RunExperiment(String fName, double maxTime) {
        return RunExperiment(fName, maxTime, 0);
    }

    /**
     * Builds a network after an exact solution is found.
     * @param solution
     * @param solutionNode
     * @param bestFound
     * @param te4
     * @return
     */
    public Network buildNetwork(TerminusEstSolution solution, NodeMCTS solutionNode, NodeMCTS bestFound, TerminusEstV4 te4) {
        // Construct the network.
        Network currentNetwork = null;
        NodeMCTS nextNode = null;
        if (solution != null) {
            if (VERBOSE) System.out.println("Using solution found in search.");
            // Initial network already has been  constructed.
            currentNetwork = solution.root;

            if (solutionNode.IsRoot()) {
                System.out.println("Solution node was root.");
                return te4.FixNetwork(currentNetwork);
            }

            nextNode = solutionNode;
        }
        else if (bestFound != null) {
            if (VERBOSE) System.out.println("Using best solution from MCTS.");
            // Construct using bestFound.
            // No solution was found through the search. construct initial solution from bestFound.
            TerminusEstState finalState = ((TerminusEstState) bestFound.ConstructNodeState());
            Vector ST = Tree.computeMaxSTsets(finalState.t1, finalState.t2);
            if (ST.size() != 1) {
                System.out.println("ERROR: best found trees not compatible.");
                System.exit(0);
            }

            currentNetwork = TerminusEstV4.ConstructInitialNetwork(ST, finalState.t1, finalState.t2, te4.t1, te4.t2, finalState.depth);

            if (bestFound.IsRoot()) { // Small subcase.
                System.out.println("BestFound was root.");
                // return new TerminusEstSolution(currentNetwork, 0, 0);
            }

            nextNode = bestFound;
        }

        // Use solution node to construct
        do {
            TerminusEstState nextState = ((TerminusEstState) nextNode.ConstructNodeState());
            TerminusEstAction a = (TerminusEstAction) nextNode.GetLastAction();
            currentNetwork = TerminusEstV4.GrowNetwork(a.taxon, currentNetwork, te4.t1, te4.t2);
            nextNode = nextNode.parent;
        } while (!nextNode.IsRoot());

        return te4.FixNetwork(currentNetwork);
    }

    /**
     * Class which contains data run by an experiment.
     * Missing data definitions:
     *  \n> String: empty string.
     *  \n> Numeric: -1.
     */
    public class ExperimentData {
        public String fName = "";
        public double timeTotal = -1;
        public double timeBuildingSearchTree = -1;
        public int hybNumExact = -1;
        public int hybLowerBound = -1;
        public int hybNumFromMCTS = -1;
        public int solutionNodeDepth = -1;
        public int solutionNodeInstance = -1;
        public int solutionDepthTotalInstances = -1;
        public boolean canceled = false;
        public String network = "";

        public static final String hdr = "ID, TIME_TOTAL, TIME_BUILD_SEARCH_TREE, HYB_EXACT, HYB_LOWER, HYB_UPPER, SOLUTION_NODE_DEPTH, SOLUTION_NODE_INSTANCE, SOLUTION_DEPTH_TOTAL, CANCELED";
        public String GetData() {
            String s = fName + ", " + timeTotal + ", " + timeBuildingSearchTree + ", " + hybNumExact + ", " + hybLowerBound + ", " + hybNumFromMCTS + ", " + solutionNodeDepth + ", " + solutionNodeInstance + ", " + solutionDepthTotalInstances + ", ";
            if (canceled) s += "true";
            else s += "false";
            return s;
        }
    }

    public class TreeData {
        public static final String del = ",";
        public String fName = "";
        public double timeToBuildTree = -1;
        public double upperBound = -1;
        public double treeDepthAvg = -1;
        public int shallowestLeaf = -1;
        public int treeDepth25 = -1;
        public int treeDepth50 = -1;
        public int treeDepth75 = -1;
        public int deepestNode = -1;
        public int depthOfFirstSolution = -1;
        public int nodesTotal = -1;
        public int trees = -1;
        public int treesCompleted = 0;

        public static final String hdr = "ID,TIME_BUILDING_TREE,UPPER_BOUND,DEPTH_AVG,SHALLOWEST_LEAF,DEPTH_25,DEPTH_50,DEPTH_75,DEEPEST_NODE,DEPTH_FIRST_SOLUTION,NODES_TOTAL,trees,T_COMPLETED";
        public String GetData() {
            return fName + del + timeToBuildTree + del + upperBound + del + treeDepthAvg + del
                    + shallowestLeaf + del + treeDepth25 + del + treeDepth50 + del
                    + treeDepth75 + del + deepestNode + del + depthOfFirstSolution + del + nodesTotal
                    + del + trees + del + treesCompleted;
        }
    }

    /**
     * Abstracting out method that collects the different possible search path options.
     * // TODO: Update this.
     * @param upperBound
     * @param searchTree
     * @param te4
     * @return
     */
    private NodeMCTS[][] GetSortedByDepth(int upperBound, NodeMCTS searchTree, TerminusEstV4 te4) {
        // Find all candidate tree nodes to search from.
        TerminusEstMC_SearchTree.CollectLeaves collectLeaves = new TerminusEstMC_SearchTree.CollectLeaves(upperBound, te4);
        collectLeaves.StartDepthFirstTraversal(searchTree);
        LeafCollection_Duplicates = collectLeaves.LeafCollection_Duplicates;
        LeafCollection_NodesTraversed = collectLeaves.LeafCollection_NodesTraversed;
        if (VERBOSE) {
            System. out.println("Nodes traversed: " + LeafCollection_NodesTraversed
                    + "\tNode duplicates in tree: " + LeafCollection_Duplicates);
        }
        return collectLeaves.GetNodeByDepth();
    }

    public void SortByDepth(NodeMCTS[] toBeSorted) {

        Arrays.sort(toBeSorted, new TerminusEstMC_SearchTree.SortByDepth());
    }

    private NodeMCTS[] GetTree() {
        TerminusEstMC_SearchTree.CollectLeaves collectLeaves = new TerminusEstMC_SearchTree.CollectLeaves(upperBound, te4);
        if (USE_HEURISTIC) collectLeaves.comp = new TerminusEstMC_SearchTree.SortByHeuristic(heuristic);

        collectLeaves.StartDepthFirstTraversal(searchTree);

        LeafCollection_Duplicates = collectLeaves.LeafCollection_Duplicates;
        LeafCollection_NodesTraversed = collectLeaves.LeafCollection_NodesTraversed;
        if (VERBOSE) {
            System. out.println("Nodes traversed: " + LeafCollection_NodesTraversed
                    + "\tNode duplicates in tree: " + LeafCollection_Duplicates);
        }

        return collectLeaves.nodes.toArray(new NodeMCTS[collectLeaves.nodes.size()]);
    }

    private void SetUpperBound() {
        if (bestFound == null) {
            if (VERBOSE) System.out.println("No solution in the tree.");
            // No solution found at all.
            // (Increase by one to ensure we also check for the case in which we have do delete *all* leaves).
            upperBound = te4.seenLeaves + 1;
        } else {
            if (VERBOSE) System.out.println("A solution was found in the tree.");
            if (VERBOSE) System.out.println("At level: " + bestFound.depth);

            // Best solution in tree is upper bound.
            upperBound = bestFound.depth;
            data.hybNumFromMCTS = bestFound.depth;
        }
    }

    public TreeData GetTreeData(String fName) {
        // Setup.
        if (VERBOSE) System.out.println("\n\nRunning experiment for: " + fName);
        TreeData data = new TreeData();
        data.fName = fName;
        data.trees = searchTreeUtil.trees;
        te4 = new TerminusEstV4(fName);
        searchTreeUtil.CreateSearchTrees(te4);
        // Get best search tree to start from, and the heuristic.
        Tuple2<NodeMCTS, NodeMCTS> b = searchTreeUtil.GetBestTreeAndLeaf();
        data.treesCompleted = searchTreeUtil.treesCompleted;
        searchTree = b.item1;
        bestFound = b.item2;

        data.timeToBuildTree = getIntervalInSeconds(timeSinceLastSearchTreeCompleted, timeSinceLastSearchTreeBuilt);
        if (bestFound != null) data.upperBound = bestFound.depth;
        searchTreeUtil.CollectTreeDepthStatistics(searchTree, data);
        return data;
    }

    public static TerminusEstSolution GetExactSolution(String file) {
        TerminusEstMCTS tem = new TerminusEstMCTS();

        double timeStart = System.currentTimeMillis();
        TerminusEstV4 te4 = new TerminusEstV4(file);

        Tuple2<NodeMCTS, NodeMCTS> b = tem.searchTreeUtil.GetBestTreeAndLeaf();
        NodeMCTS searchTree = b.item1;
        NodeMCTS bestFound = b.item2;

        int upperBound;
        if (bestFound == null) {
            if (VERBOSE) System.out.println("No solution in the tree.");
            // No solution found at all.
            // (Increase by one to ensure we also check for the case in which we have do delete *all* leaves).
            upperBound = te4.seenLeaves + 1;
        }
        else {
            if (VERBOSE) System.out.println("A solution was found in the tree.");
            // Best solution in tree is upper bound.
            upperBound = bestFound.depth;
            if (VERBOSE) System.out.println("At level: " + upperBound);
        }

        // Find all candidate tree nodes to search from.
        NodeMCTS[][] searchNodes = tem.GetSortedByDepth(upperBound, searchTree, te4);
        if (VERBOSE) System.out.println("Nodes to search from collected.");
        if (VERBOSE) {
            System.out.println("Distribution: ");
            for (int i = 0; i < searchNodes.length; ++i) {
                System.out.println("Depth " + i + ": " + searchNodes[i].length);
            }

        }

        TerminusEstSolution solution = null;
        NodeMCTS solutionNode = null;
        // i = the current depth we're trying to compute at
        for (int i = 0; i < upperBound; ++i) {
            if (VERBOSE) System.out.println("Attempting hyb = " + i);
            if (VERBOSE) System.out.print("\tAt nodes of depth: ");
            // j = All search nodes at depth i which we wish to investigate.
            for (int j = i; j >= 0; --j) {
                if (VERBOSE) System.out.print(j + ", ");
                for (int k = 0; k < searchNodes[j].length; ++k) {
                    // Construct trees we are searching from.
                    NodeMCTS node = searchNodes[j][k]; // Make it easier to reference this node.
                    TerminusEstState s = (TerminusEstState) node.ConstructNodeState();
                    solution = te4.ComputePartialSolution(s.t1, s.t2, j, i-j);
                    solutionNode = node;

                    if (solution != null) {
                        if (VERBOSE) {
                            System.out.println("A solution was found!");
                            System.out.println(solution.toString());
                            System.out.println("Hyb: " + i + "\tAt depth: " + j);
                            System.out.println("Node:\n" + node.GetNewick());
                        }

                        break;
                    }
                }
                if (solution != null) break;
            }
            if (VERBOSE) System.out.println();
            if (solution != null) break;
        } // End for-loop

        // Construct the network.
        Network currentNetwork = null;
        NodeMCTS nextNode = null;
        if (solution != null) {
            if (VERBOSE) System.out.println("Using solution found in search.");
            // Initial network already has been  constructed.
            currentNetwork = solution.root;

            if (solutionNode.IsRoot()) {
                System.out.println("Solution node was root, somewhow.");
                return new TerminusEstSolution(currentNetwork, 0, 0);
            }

            nextNode = solutionNode;
        }
        else if (bestFound != null) {
            if (VERBOSE) System.out.println("Using best solution from MCTS.");
            // Construct using bestFound.
            // No solution was found through the search. construct initial solution from bestFound.
            TerminusEstState finalState = ((TerminusEstState) bestFound.ConstructNodeState());
            Vector ST = Tree.computeMaxSTsets(finalState.t1, finalState.t2);
            if (ST.size() != 1) {
                System.out.println("ERROR: best found trees not compatible.");
                System.exit(0);
            }

            currentNetwork = TerminusEstV4.ConstructInitialNetwork(ST, finalState.t1, finalState.t2, te4.t1, te4.t2, finalState.depth);

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
            currentNetwork = TerminusEstV4.GrowNetwork(a.taxon, currentNetwork, te4.t1, te4.t2);
            nextNode = nextNode.parent;
        } while (!nextNode.IsRoot());

        Network net = te4.FixNetwork(currentNetwork);
        if (VERBOSE) TerminusEstV4.DumpENewick(net);
        if (VERBOSE) System.out.println("Network fully constructed.");

        return new  TerminusEstSolution(currentNetwork, 0, 0);
    }

    public static TerminusEstSolution AttemptSolution(String file) {
        TerminusEstMCTS tem = new TerminusEstMCTS();

        double timeNow = System.currentTimeMillis();
        TerminusEstV4 te4 = new TerminusEstV4(file);

        NodeMCTS searchTree = tem.searchTreeUtil.GetSearchTree(te4);
        NodeMCTS bestFound = tem.searchTreeUtil.GetBestInTree(searchTree);

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


    /**
     * Retuurn time interval of (a-b) in seconds.
     * @param a Time in milliseconds
     * @param b Time in milliseconds.
     * @return
     */
    public static double getIntervalInSeconds(long a, long b) {
        return ((double) (a-b))/1000.0;
    }

    public static void main(String[] args) {
        // TerminusEstSolution solution = AttemptSolution(args[0]);
        // System.out.println(solution.toString());

        // RunSingleInstance(args[0]);

        // GetExactSolution(args[0]);

        TerminusEstMCTS test = new TerminusEstMCTS( (int) (3*Math.pow(10, 3)), 10, Math.sqrt(2), 1000, 10);

        test.RunExperiment(args[0], 3600, 14);
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
