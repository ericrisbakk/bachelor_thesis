package main.mcts.base;

/**
 * Basic MCTS.
 */
public class MCTS {
    public int maxIterations = 0;
    public INodeMCTS root;
    public ISelectionPolicy selectionPolicy;
    public ISimulationPolicy simulationPolicy;

    public MCTS(INodeMCTS root, int maxIterations, ISelectionPolicy selectionPolicy, ISimulationPolicy simulationPolicy) {
        this.root = root.DeepCopy();
        this.maxIterations = maxIterations;
        this.selectionPolicy = selectionPolicy;
        this.simulationPolicy = simulationPolicy;
    }

    /**
     * Builds the search tree.
     * During the expansion stage, all states are simulated from once, to get an initial result.
     */
    public void BuildTree() {
        int iteration = 0;

        while (iteration < maxIterations) {

            // Select
            INodeMCTS select = selectionPolicy.Select(root);

            // Expand
            if (!select.IsExpanded()) select.Expand();
            INodeMCTS next = selectionPolicy.SelectFromExpansion(select);

            // Simulate
            IResult result = simulationPolicy.Simulate(next);

            // Back-propagation.
            INodeMCTS bpNode = next;
            while (bpNode != null) {
                bpNode.GetResult().Update(result);
                bpNode = bpNode.GetParent();
            }
        }
    } // End building tree.

}
