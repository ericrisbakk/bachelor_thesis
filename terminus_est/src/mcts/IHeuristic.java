package mcts;

/**
 * Calculates the value of a state.
 */
public interface IHeuristic {
    float Calculate(State state);
}
