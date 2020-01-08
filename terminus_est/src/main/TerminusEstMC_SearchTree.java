package main;

import main.TerminusEst.TerminusEstState;
import main.TerminusEst.TerminusEstV4;
import main.TerminusEst.Tree;
import main.mcts.*;
import main.mcts.base.MCTS;
import main.utility.Tuple2;

import java.util.ArrayList;

public class TerminusEstMC_SearchTree {
    public int iterations;
    public int simulations;
    public double param_c;
    public double param_d;

    public TerminusEstMCTS manager;

    public TerminusEstMC_SearchTree(int iterations, int simulations, double param_c, double param_d, TerminusEstMCTS manager) {
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
        MCTS mcts = new MCTS(iterations, select, sim, gen);

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
}
