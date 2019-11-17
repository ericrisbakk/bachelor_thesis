package main.mcts;

import main.mcts.base.ISelectionPolicy;
import main.mcts.base.NodeMCTS;

public class SelectUCT implements ISelectionPolicy {
    private static final boolean DEBUG = true;
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
        NodeMCTS current = root;

        // Find best and most immediate unexpanded node.
        while (current.IsExpanded()) {
            if (current.IsTerminal()) {
                return current;
            }

            // Get child with highest UCT value, or the one which has not been expanded (if it exists).
            NodeMCTS best = current.GetChildren()[0];
            for (int i = 0; i < current.GetChildren().length; ++i) {
                NodeMCTS option = current.GetChildren()[i];
                // Check whether child is unexpanded first.
                if (!HasBeenSimulatedFrom(option)) {
                    return current;
                }
                // Comparing only happens if both are expanded.
                if (GetUCT(best) < GetUCT(option))
                    best = option;
            }

            // We only get here if all child nodes have been expanded.
            current = best;
        }

        return current;
    }

    /**
     * Chooses either among expanded children, and if the node has no children (i.e. is a terminal node)
     * returns that node. Implementation-wise, this just remembers the best solution found from Select call.
     * @param parent The parent node whose children we (potentially) select from. Has always been expanded.
     * @return Node to simulate from.
     */
    @Override
    public NodeMCTS SelectFromExpansion(NodeMCTS parent) {
        // Know this has no children.
        if (parent.IsTerminal())
            return parent;

        // Otherwise select first un-simulated option.
        for (int i = 0; i < parent.GetChildren().length; ++i) {
            if ( HasBeenSimulatedFrom(parent.GetChildren()[i]) )
                return parent.GetChildren()[i];
        }

        System.out.println("ERROR: node is not terminal, and all child actions have been simulated.");
        return null;
    }

    public boolean HasBeenSimulatedFrom(NodeMCTS node) {
        return ((ResultUCT) node.GetResult()).simulations == 0;
    }

    public double GetUCT(NodeMCTS node) {
        ResultUCT values = (ResultUCT) node.GetResult();
        ResultUCT parentValues = (ResultUCT) node.GetParent().GetResult();

        return ( (double) values.wins / (double) values.simulations)
                + (param_c*Math.sqrt( (Math.log(parentValues.simulations))/values.simulations ));
    }
}
