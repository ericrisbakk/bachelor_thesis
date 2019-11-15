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
    NodeMCTS Select(NodeMCTS root);

    /**
     * Behaviour for selecting which node to simulate from after an expansion.
     * @param parent The parent node whose children we (potentially) select from.
     * @return The state we wish to simulate from.
     */
    NodeMCTS SelectFromExpansion(NodeMCTS parent);
}
