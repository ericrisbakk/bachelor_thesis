package main;

import main.TerminusEst.TerminusEstInputHandler;
import main.TerminusEst.TerminusEstState;
import main.TerminusEst.TerminusEstV4;
import main.TerminusEst.Tree;
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

        TerminusEstInputHandler inp = new TerminusEstInputHandler();
        TerminusEstV4 te4 = new TerminusEstV4();
        inp.InterpretFile(file, te4);

        Tree T1 = te4.t1.copy(null, null);
        Tree T2 = te4.t2.copy(null, null);
        TerminusEstState state = new TerminusEstState(T1, T2, 0);

        mcts.BuildTree(state);

        return mcts.root;
    }

    public static void RunSingleInstance(String file) {
        TerminusEstMCTS tem = new TerminusEstMCTS();
        System.out.println("Beginning MCTS: ");
        NodeMCTS searchTree = tem.GetSearchTree(file);
        System.out.println("\n\nMCTS completed.");
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
