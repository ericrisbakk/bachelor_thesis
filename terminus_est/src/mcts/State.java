package mcts;

/**
 * Represents the collection of values which defines an instance of some object.
 */
public interface State {
    /**
     * Apply action to this state.
     * @param a Given action. It should always be a legal w.r.t. State params.
     */
    void Apply(Action a);

    void Revert();
}
