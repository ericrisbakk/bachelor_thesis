package mcts;

import java.util.Collection;

/**
 * Interface for class which selects the next node to expand.
 */
public interface ISelectionPolicy {
    /**
     * Select the next leaf node to expand.
     * @param root from which we expand from.
     * @return Next node to expand.
     */
    State Select(State root);

    /**
     * Behaviour for selecting which node to simulate from after an expansion.
     * @param collection Collection of actions and the states we get from the node that expanded
     * @return The state we wish to simulate from.
     */
    State SelectFromExpansion(Collection<ActionStatePair> collection);
}
