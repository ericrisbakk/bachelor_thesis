package main.mcts.base;

import main.mcts.NodeMCTS;

/**
 * Interface for class which selects the next node to expand and simulate.
 */
public interface ISelectionPolicy {
    /**
     * Select the next leaf node to expand.
     * @param root from which we search from.
     * @return Next node to expand.
     */
    NodeMCTS Select(NodeMCTS root);

    /**
     * Behaviour for selecting which node to simulate from after an expansion.
     * @param parent The parent node whose children we (potentially) select from.
     * @return The state we wish to simulate from.
     */
    NodeMCTS SelectFromExpansion(NodeMCTS parent);

    /**
     * Choose child according to best value, that has been simulated at least once.
     * @param node Node from which we pick a child from.
     * @return Action.
     */
    Action SelectBestChildAction(NodeMCTS node);
}
