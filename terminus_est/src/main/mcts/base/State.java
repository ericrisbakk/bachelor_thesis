package main.mcts.base;

/**
 * Symbolizes a game state.
 */
public interface State {
    /**
     * Apply given action to the state (does not create a new copy of the state).
     * @param a A legal action.
     */
    void Apply(Action a);

    /**
     * @return Get all possible legal actions for the current state.
     */
    Action[] GetLegalActions();
}
