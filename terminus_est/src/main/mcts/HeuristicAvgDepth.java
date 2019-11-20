package main.mcts;

import main.TerminusEst.TerminusEstState;
import main.mcts.base.IHeuristic;
import main.mcts.base.State;

/**
 * Keeps track
 */
public class HeuristicAvgDepth implements IHeuristic {

    double sum = 0;
    long n = 0;

    @Override
    public double Calculate(State state) {
        TerminusEstState s = (TerminusEstState) state;
        sum +=  s.depth;
        n += 1;

        if (s.depth < sum / n)
            return 1;
        else if (s.depth > sum / n)
            return 0;

        return 0.5;
    }
}
