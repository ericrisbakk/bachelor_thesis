package main.mcts;

import main.TerminusEst.TerminusEstState;
import main.mcts.base.IHeuristic;
import main.mcts.base.State;

public class HeuristicNegativeDepth implements IHeuristic {
    @Override
    public double Calculate(State state) {
        return -((TerminusEstState) state).depth;
    }
}
