package main.mcts.base;

import main.utility.IDeepCopy;

/**
 * Symbolizes a game state.
 */
public interface State extends IDeepCopy {
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
