package main.mcts.base;

import main.mcts.NodeMCTS;

/**
 * Basic MCTS.
 */
public class MCTS {
    public int maxIterations = 0;
    public NodeMCTS root;
    public ISelectionPolicy selectionPolicy;
    public ISimulationPolicy simulationPolicy;

    public MCTS(int maxIterations, ISelectionPolicy selectionPolicy, ISimulationPolicy simulationPolicy) {
        this.maxIterations = maxIterations;
        this.selectionPolicy = selectionPolicy;
        this.simulationPolicy = simulationPolicy;
    }

    /**
     * Builds the search tree.
     * During the expansion stage, all states are simulated from once, to get an initial result.
     */
    public void BuildTree(State state) {
        root = new NodeMCTS(state, null, null);

        int iteration = 0;

        while (iteration < maxIterations) {

            // Select
            NodeMCTS select = selectionPolicy.Select(root);

            // Expand
            if (!select.IsExpanded()) select.Expand();
            NodeMCTS next = selectionPolicy.SelectFromExpansion(select);

            // Simulate
            IResult result = simulationPolicy.Simulate(next);

            // Back-propagation.
            NodeMCTS bpNode = next;
            while (bpNode != null) {
                bpNode.GetResult().Update(result);
                bpNode = bpNode.GetParent();
            }
        }
    } // End building tree.

    public Action GetBestAction() {
        return selectionPolicy.SelectBestChildAction(root);
    }

}
