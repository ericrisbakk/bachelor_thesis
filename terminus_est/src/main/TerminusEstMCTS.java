package main;

import main.TerminusEst.TerminusEstInputHandler;
import main.TerminusEst.TerminusEstState;
import main.TerminusEst.TerminusEstV4;
import main.TerminusEst.Tree;
import main.mcts.HeuristicAvgDepth;
import main.mcts.ResultUCTGenerator;
import main.mcts.SelectUCT;
import main.mcts.SimulateRandom;
import main.mcts.base.Action;
import main.mcts.base.MCTS;

public class TerminusEstMCTS {

    public static void main(String[] args) {
        SelectUCT select = new SelectUCT();
        HeuristicAvgDepth heuristic = new HeuristicAvgDepth();
        SimulateRandom sim = new SimulateRandom(30, heuristic);
        ResultUCTGenerator gen = new ResultUCTGenerator();
        MCTS mcts = new MCTS(10000, select, sim, gen);

        TerminusEstInputHandler inp = new TerminusEstInputHandler();
        TerminusEstV4 te4 = new TerminusEstV4();
        inp.InterpretFile(args[0], te4);

        Tree T1 = te4.t1.copy(null, null);
        Tree T2 = te4.t2.copy(null, null);
        TerminusEstState state = new TerminusEstState(T1, T2, 0);

        System.out.println("Beginning MCTS: ");
        mcts.BuildTree(state);
        System.out.println("\n\nMCTS completed.");

        System.out.println(mcts.root.GetNewick());
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
