package main;

import main.TerminusEst.*;
import main.mcts.*;
import main.mcts.base.*;
import main.utility.Tuple2;

import java.util.*;

public class TerminusEstMCTS {

    public static boolean VERBOSE = false;

    public int iterations = 100000;
    public int simulations = 10;
    public double param_c = Math.sqrt(2);
    public double param_d = 1000;

    public int LeafCollection_NodesTraversed = 0;
    public int LeafCollection_Duplicates = 0;

    private long timeStart;
    private long timeSinceLastSearchTreeBuilt;
    private long timeSinceLastSearchTreeCompleted;

    public TerminusEstMCTS() {}

    public TerminusEstMCTS(int iterations, int simulations, double param_c, double param_d) {
        this.iterations = iterations;
        this.simulations = simulations;
        this.param_c = param_c;
        this.param_d = param_d;
    }

    /**
     * Runs a single experiment on the problem found in the given fName, for the duration given.
     * @param fName filename of problem
     * @param maxTime max duration for search in seconds.
     */
    public ExperimentData RunExperiment(String fName, double maxTime) {
        if (VERBOSE) System.out.println("\n\nRunning experiment for: " + fName);
        ExperimentData data = new ExperimentData();
        data.fName = fName;

        Tuple2<NodeMCTS, TerminusEstV4> tup = GetSearchTree(fName);
        NodeMCTS searchTree = tup.item1;
        TerminusEstV4 te4 = tup.item2;
        NodeMCTS bestFound = GetBestFound(searchTree);

        timeStart = timeSinceLastSearchTreeBuilt;
        data.timeBuildingSearchTree = getIntervalInSeconds(timeSinceLastSearchTreeCompleted, timeSinceLastSearchTreeBuilt);

        int upperBound;
        if (bestFound == null) {
            if (VERBOSE) System.out.println("No solution in the tree.");
            // No solution found at all.
            // (Increase by one to ensure we also check for the case in which we have do delete *all* leaves).
            upperBound = te4.seenLeaves + 1;
        }
        else {
            if (VERBOSE) System.out.println("A solution was found in the tree.");
            if (VERBOSE) System.out.println("At level: " + bestFound.depth);

            // Best solution in tree is upper bound.
            upperBound = bestFound.depth;
            data.hybNumFromMCTS = bestFound.depth;
        }

        // Find all candidate tree nodes to search from.
        NodeMCTS[][] searchNodes = GetCandidateLeaves(searchTree, upperBound, te4);
        if (VERBOSE) System.out.println("Nodes to search from collected.");
        if (VERBOSE) {
            System.out.println("Distribution: ");
            for (int i = 0; i < searchNodes.length; ++i) {
                System.out.println("Depth " + i + ": " + searchNodes[i].length);
            }

        }

        te4.startTime = timeStart;
        te4.setRuntime(maxTime);
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

                    if (te4.isCanceled()) {
                        if (VERBOSE) System.out.println("Time-out occurred!");
                        data.canceled = true; break;
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
                        data.timeTotal = getIntervalInSeconds(System.currentTimeMillis(), timeStart);
                        break;
                    }
                }
                if (solution != null || te4.isCanceled()) break;
            }
            if (VERBOSE) System.out.println();
            if (solution != null || te4.isCanceled()) break;
        } // End for-loop

        if (solution != null || bestFound != null) {
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


    public Tuple2<NodeMCTS, TerminusEstV4> GetSearchTree(String file) {
        SelectUCT_SP select = new SelectUCT_SP();
        SelectUCT_SP.param_c = param_c;
        SelectUCT_SP.param_d = param_d;
        HeuristicNegativeDepth  heuristic = new HeuristicNegativeDepth();
        ResultUCT_SPGenerator gen = new ResultUCT_SPGenerator();
        SimulateRandom sim = new SimulateRandom(simulations, heuristic, gen);
        MCTS mcts = new MCTS(iterations, select, sim, gen);

        TerminusEstV4 te4 = new TerminusEstV4(file);

        Tree T1 = te4.t1.copy(null, null);
        Tree T2 = te4.t2.copy(null, null);
        TerminusEstState state = new TerminusEstState(T1, T2, 0);

        if (VERBOSE) System.out.println("Building search tree.");
        timeSinceLastSearchTreeBuilt = System.currentTimeMillis();
        mcts.BuildTree(state);
        timeSinceLastSearchTreeCompleted = System.currentTimeMillis();
        if (VERBOSE) System.out.println("Search tree completed.");
        return new Tuple2<NodeMCTS, TerminusEstV4>(mcts.root, te4);
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
        public int hybNumFromMCTS = -1;
        public int solutionNodeDepth = -1;
        public int solutionNodeInstance = -1;
        public int solutionDepthTotalInstances = -1;
        public boolean canceled = false;
        public String network = "";

        public static final String hdr = "ID, TIME_TOTAL, TIME_BUILD_SEARCH_TREE, HYB_EXACT, HYB_APPROX, SOLUTION_NODE_DEPTH, SOLUTION_NODE_INSTANCE, SOLUTION_DEPTH_TOTAL, CANCELED";
        public String GetData() {
            String s = fName + ", " + timeTotal + ", " + timeBuildingSearchTree + ", " + hybNumExact + ", " + hybNumFromMCTS + ", " + solutionNodeDepth + ", " + solutionNodeInstance + ", " + solutionDepthTotalInstances + ", ";
            if (canceled) s += "true";
            else s += "false";
            return s;
        }
    }

    public static TerminusEstSolution GetExactSolution(String file) {
        TerminusEstMCTS tem = new TerminusEstMCTS();

        double timeStart = System.currentTimeMillis();

        NodeMCTS searchTree = tem.GetSearchTree(file).item1;
        double timeEndSearchTree = System.currentTimeMillis();

        NodeMCTS bestFound = GetBestFound(searchTree);

        double seconds =  ((double)(timeEndSearchTree - timeStart))/1000.0;

        TerminusEstV4 te4 = new TerminusEstV4(file);

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
        NodeMCTS[][] searchNodes = tem.GetCandidateLeaves(searchTree, upperBound, te4);
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
    public NodeMCTS[][] GetCandidateLeaves(NodeMCTS root, int maxDepth, TerminusEstV4 te4) {
        ArrayList<NodeMCTS> n = new ArrayList<>();
        SortByVisits comp = new SortByVisits();
        int[] depthCount = new int[maxDepth];

        // Let's keep track of only unique nodes!
        Hashtable<String, NodeMCTS> uniques = new Hashtable<>();

        DFSWithSort(root, n, comp, maxDepth, depthCount, uniques, te4);

        if (VERBOSE) {
            System.out.println("Candidate leaves collected. Information: ");
            System.out.println("Total traversed: " + LeafCollection_NodesTraversed);
            System.out.println("Duplicates detected " + LeafCollection_Duplicates);
            System.out.println("Leaves added: " + n.size());
        }

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
    private void DFSWithSort(NodeMCTS node, ArrayList<NodeMCTS> list, Comparator<NodeMCTS> comp,
                                    int maxDepth, int[] depthCount, Hashtable<String, NodeMCTS> uniques, TerminusEstV4 te4) {
        LeafCollection_NodesTraversed += 1;
        if (node.IsTerminal()) {
            return;
        }

        // Only add nodes below the maxDepth
        if (node.depth >= maxDepth)
            return;

        // Only add if leaf and under max depth.
        if (node.IsLeaf()) {
            if (IsUnique(uniques, node, te4)) {
                list.add(node);
                depthCount[node.depth] += 1;
            }
            else {
                LeafCollection_Duplicates += 1;
            }

            return;
        }

        NodeMCTS[] children = new NodeMCTS[node.children.length];
        System.arraycopy(node.children, 0, children, 0, node.children.length);
        Arrays.sort(children, comp);

        for (int i = 0; i < children.length; ++i) {
            DFSWithSort(children[i], list, comp, maxDepth, depthCount, uniques, te4);
        }
    }

    public static boolean IsUnique(Hashtable<String, NodeMCTS> uniques, NodeMCTS n, TerminusEstV4 te4) {
        TerminusEstState s = (TerminusEstState) n.ConstructNodeState();
        String bitString = te4.GetBitString(s.t1, s.t2);
        if (uniques.containsKey(bitString)) {
            return false;
        }

        uniques.put(bitString, n);
        return true;
    }

    public static TerminusEstSolution AttemptSolution(String file) {
        TerminusEstMCTS tem = new TerminusEstMCTS();

        double timeNow = System.currentTimeMillis();

        NodeMCTS searchTree = tem.GetSearchTree(file).item1;
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
        NodeMCTS searchTree = tem.GetSearchTree(file).item1;
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

        TerminusEstMCTS test = new TerminusEstMCTS((int) Math.pow(10, 5), 10, Math.sqrt(2), 1000);

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
