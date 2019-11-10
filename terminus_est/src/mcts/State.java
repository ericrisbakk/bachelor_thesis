package mcts;

import java.util.Collection;

/**
 * Represents the collection of values which defines an instance of some object, as well
 * as an MCTS node.
 */
public interface State {
    /**
     * Apply action to this state.
     * @param a Given action. It should always be a legal w.r.t. State params.
     */
    void Apply(Action a);

    /**
     * Create ActionStatePair for all allowed actions and store to this object.
     */
    void Expand();

    /**
     * Fetch results from expansion.
     * @return Some collection of
     */
    Collection<ActionStatePair> GetActionsAndChildren();

    /**
     * Get parent in search tree.
     * @return parent state-node.
     */
    State GetParent();

    /**
     * Creates a deep copy of this object.
     * @return Deep copy.
     */
    State DeepCopy();
}
