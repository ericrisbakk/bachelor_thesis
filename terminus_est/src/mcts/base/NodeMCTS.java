package mcts.base;

import java.util.Collection;

/**
 * Represents the collection of values which defines an instance of some object, as well
 * as an MCTS node.
 */
public interface NodeMCTS {
    /**
     * Apply action to this state.
     * @param a Given action. It should always be a legal w.r.t. State params.
     */
    void Apply(Action a);

    /**
     * Returns the action which was last taken, and so created the state of this node.
     * @return Last action. Only the root node can return null.
     */
    Action GetLastAction();

    /**
     * @return Collected results of this node (and child nodes).
     */
    IResult GetResult();

    /**
     * Create ActionStatePair for all allowed actions and store to this object.
     */
    void Expand();

    /**
     * Fetch results from expansion.
     * @return Some collection of
     */
    NodeMCTS[] GetChildren();

    /**
     * Get parent in search tree.
     * @return parent state-node.
     */
    NodeMCTS GetParent();

    /**
     * Creates a deep copy of this object.
     * @return Deep copy.
     */
    NodeMCTS DeepCopy();

    /**
     * See whether node has been expanded.
     * @return True if it has been expanded, false otherwise.
     */
    boolean IsExpanded();

    /**
     * See whether node is a leaf. A node is a leaf node if it either has not been expanded, or
     * it is a terminal state.
     * @return True if a leaf node, false otherwise.
     */
    boolean IsLeaf();

    /**
     * Determine whether we have reached an ending (and can/shouldn't expand from this node any longer)
     * @return True if terminal, false otherwise.
     */
    boolean IsTerminal();

    /**
     * @return The number of children that have been expanded.
     */
    int ExpandedChildCount();

    /**
     * @return Total number of children.
     */
    int ChildCount();
}
