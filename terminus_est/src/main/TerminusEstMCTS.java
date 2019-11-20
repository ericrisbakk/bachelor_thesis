package main;

import main.TerminusEst.TerminusEstInputHandler;
import main.TerminusEst.TerminusEstState;
import main.TerminusEst.TerminusEstV4;
import main.TerminusEst.Tree;
import main.mcts.HeuristicAvgDepth;
import main.mcts.ResultUCTGenerator;
import main.mcts.SelectUCT;
import main.mcts.SimulateRandom;
import main.mcts.base.MCTS;

public class TerminusEstMCTS {

    public static void main(String[] args) {
        SelectUCT select = new SelectUCT;
        HeuristicAvgDepth heuristic = new HeuristicAvgDepth();
        SimulateRandom sim = new SimulateRandom(30, heuristic);
        ResultUCTGenerator gen = new ResultUCTGenerator();
        MCTS mcts = new MCTS(1000, select, sim, gen);

        TerminusEstInputHandler inp = new TerminusEstInputHandler();
        inp.InterpretFile(args[0]);
        TerminusEstV4 te4 = new TerminusEstV4();

        Tree T1 = TerminusEstV4.t1.copy(null, null);
        Tree T2 = TerminusEstV4.t2.copy(null, null);
        TerminusEstState state = new TerminusEstState(T1, T2, 0);

    }
}
