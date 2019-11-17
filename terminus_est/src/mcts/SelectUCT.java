package mcts;

import mcts.base.ISelectionPolicy;
import mcts.base.NodeMCTS;

public class SelectUCT implements ISelectionPolicy {
    public static double param_c = Math.sqrt(2);

    /**
     * The method goes down in the tree, guided by the UCT value of nodes.
     * If a node has unexpanded children, these will always be prioritized,
     * otherwise, it selects the child node with the best UCT value.
     * @param root from which we search from.
     * @return Node from which we expand from.
     */
    @Override
    public NodeMCTS Select(NodeMCTS root) {
        return null;
    }

    /**
     * Chooses either among expanded children, and if the node has no children (i.e. is a terminal node)
     * returns that node.
     * @param parent The parent node whose children we (potentially) select from.
     * @return Node to simulate from.
     */
    @Override
    public NodeMCTS SelectFromExpansion(NodeMCTS parent) {
        return null;
    }
}
