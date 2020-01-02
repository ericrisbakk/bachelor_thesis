package main.mcts;

import main.TerminusEst.TerminusEstState;
import main.mcts.base.IHeuristic;
import main.mcts.base.State;

public class HeuristicBestDepth implements IHeuristic {
    public int bestDepth;


    @Override
    public double Calculate(State state) {
        TerminusEstState s = (TerminusEstState) state;

        if (s.depth < bestDepth)
            return 1;
        else if (s.depth > bestDepth)
            return 0;

        return 0.5;
    }
}
