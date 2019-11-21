package main;

import main.TerminusEst.*;
import main.mcts.*;
import main.mcts.base.Action;
import main.mcts.base.MCTS;

public class TerminusEstMCTS {

    public static int iterations = 10000;
    public static int simulations = 30;

    public NodeMCTS GetSearchTree(String file) {
        SelectUCT select = new SelectUCT();
        HeuristicAvgDepth heuristic = new HeuristicAvgDepth();
        SimulateRandom sim = new SimulateRandom(simulations, heuristic);
        ResultUCTGenerator gen = new ResultUCTGenerator();
        MCTS mcts = new MCTS(iterations, select, sim, gen);

        TerminusEstV4 te4 = new TerminusEstV4(file);

        Tree T1 = te4.t1.copy(null, null);
        Tree T2 = te4.t2.copy(null, null);
        TerminusEstState state = new TerminusEstState(T1, T2, 0);

        mcts.BuildTree(state);

        return mcts.root;
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
        RunSingleInstance(args[0]);
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
