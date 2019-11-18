package main.mcts.base;

/**
 * Calculates the "value" of a state.
 */
public interface IHeuristic {
    double Calculate(State state);
}
