package mcts;

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
}
