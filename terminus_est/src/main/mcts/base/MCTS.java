package main.mcts.base;

import main.mcts.NodeMCTS;

/**
 * Basic MCTS. The steps have been abstracted to facilitate an object-oriented workflow.
 */
public class MCTS {
    public int maxIterations = 0;
    public NodeMCTS root;
    public ISelectionPolicy selectionPolicy;
    public ISimulationPolicy simulationPolicy;
    public IResultGenerator resultGenerator;

    public MCTS(int maxIterations, ISelectionPolicy selectionPolicy, ISimulationPolicy simulationPolicy, IResultGenerator resultGenerator) {
        this.maxIterations = maxIterations;
        this.selectionPolicy = selectionPolicy;
        this.simulationPolicy = simulationPolicy;
        this.resultGenerator = resultGenerator;
    }

    /**
     * Builds the search tree.
     * During the expansion stage, all states are simulated from once, to get an initial result.
     */
    public void BuildTree(State state) {
        NodeMCTS.resultGenerator = resultGenerator;
        root = new NodeMCTS(state, null, null);

        int iteration = 0;

        while (iteration < maxIterations) {
            ++iteration;

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
