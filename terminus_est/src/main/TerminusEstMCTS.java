package main;

import main.TerminusEst.*;
import main.mcts.*;
import main.mcts.base.*;
import main.utility.Tuple2;

import javax.swing.plaf.synth.SynthTableUI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TerminusEstMCTS {

    public static final boolean VERBOSE = false;
    public static final boolean PARALLEL = false;
    public static final boolean SORT = true;

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

    private ExperimentData data;
    private TerminusEstV4 te4;
    private NodeMCTS searchTree;
    private NodeMCTS bestFound;
    private Hashtable<String, Double> heuristic;

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

    private void ExperimentSetup(String fName) {
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

        searchTreeUtil.ComputeHeuristic();
        heuristic = searchTreeUtil.heuristic;

        SetUpperBound();
    }

    /**
     * Runs a single experiment on the problem found in the given fName, for the duration given.
     * @param fName filename of problem
     * @param maxTime max duration for search in seconds.
     */
    public ExperimentData RunExperiment(String fName, double maxTime) {
        ExperimentSetup(fName);

        timeStart = timeSinceLastSearchTreeBuilt;
        data.timeBuildingSearchTree = getIntervalInSeconds(timeSinceLastSearchTreeCompleted, timeSinceLastSearchTreeBuilt);

        NodeMCTS[][] searchNodes = GetLeavesForSearch(upperBound, searchTree, te4);

        if (VERBOSE) System.out.println("Nodes to search from collected.");
        if (VERBOSE) {
            System.out.println("Distribution: ");
            for (int i = 0; i < searchNodes.length; ++i) {
                System.out.println("Depth " + i + ": " + searchNodes[i].length);
            }

        }

        // Time to run!
        te4.startTime = timeStart;
        te4.setRuntime(maxTime);
        TerminusEstSolution solution = null;
        NodeMCTS solutionNode = null;

        ExecutorService exec =  null;
        if (PARALLEL) {
            if (VERBOSE) System.out.println("Running parallel!");
            exec = Executors.newWorkStealingPool();
        } else {
            if (VERBOSE) System.out.println("Running single-threaded!");
        }

        // i = the current depth we're trying to compute at
        for (int i = 0; i < upperBound; ++i) {
            if (VERBOSE) System.out.println("Attempting hyb = " + i);
            if (VERBOSE) System.out.print("\tAt nodes of depth: ");
            // j = All search nodes at depth i which we wish to investigate.
            for (int j = i; j >= 0; --j) {
                if (VERBOSE) System.out.print(j + ", ");

                if (PARALLEL) {
                    // Create the parallell callables.
                    NodeMCTS[] subset = searchNodes[j];
                    if (subset.length > 0) {
                        ArrayList<TerminusEstParallel> l = new ArrayList<>(subset.length);
                        for (int m = 0; m < subset.length; ++m) {
                            TerminusEstState s = (TerminusEstState) subset[m].ConstructNodeState();
                            l.add(new TerminusEstParallel(subset[m], te4, s.t1, s.t2, j, i-j, m));
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

                            for (int m = 0; m < 0; ++m) {

                                if (possible.get(m) != null) {
                                    if (VERBOSE) {
                                        System.out.println("A solution was found!");
                                        System.out.println(solution.toString());
                                        System.out.println("Hyb: " + i + "\tAt depth: " + j);
                                    }

                                    data.hybNumExact = i;
                                    data.solutionNodeDepth = j;
                                    data.solutionNodeInstance = -1;
                                    data.solutionDepthTotalInstances = searchNodes[j].length;
                                    break;
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    for (int k = 0; k < searchNodes[j].length; ++k) {
                        // Construct trees we are searching from.
                        NodeMCTS node = searchNodes[j][k]; // Make it easier to reference this node.
                        TerminusEstState s = (TerminusEstState) node.ConstructNodeState();
                        solution = te4.ComputePartialSolution(s.t1, s.t2, j, i - j);
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
                                System.out.println("Hyb: " + i + "\tAt depth: " + j);
                            }

                            data.hybNumExact = i;
                            data.solutionNodeDepth = j;
                            data.solutionNodeInstance = k;
                            data.solutionDepthTotalInstances = searchNodes[j].length;
                            break;
                        }
                    }
                }

                data.hybLowerBound = i;
            if (solution != null || te4.isCanceled()) break;
        }
            if (VERBOSE) System.out.println();
            if (solution != null || te4.isCanceled()) break;
        } // End for-loop

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

        return data;
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
                System.out.println("Solution node was root, somewhow.");
                // return new TerminusEstSolution(currentNetwork, 0, 0);
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

    /**
     * Abstracting out method that collects the different possible search path options.
     * // TODO: Update this.
     * @param upperBound
     * @param searchTree
     * @param te4
     * @return
     */
    private NodeMCTS[][] GetLeavesForSearch(int upperBound, NodeMCTS searchTree, TerminusEstV4 te4) {
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
        NodeMCTS[][] searchNodes = tem.GetLeavesForSearch(upperBound, searchTree, te4);
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

        TerminusEstMCTS test = new TerminusEstMCTS((int) Math.pow(10, 4), 10, Math.sqrt(2), 1000);

        test.RunExperiment(args[0], 600);
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
