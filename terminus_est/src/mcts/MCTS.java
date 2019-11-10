package mcts;

/**
 * Basic MCTS.
 */
public class MCTS {
    public int maxIterations = 0;
    public State root;
    public ISelectionPolicy selectionPolicy;
    public ISimulationPolicy simulationPolicy;

    public MCTS(State root, int maxIterations) {
        this.root = root.DeepCopy();
        this.maxIterations = maxIterations;
    }

    /**
     * Builds the search tree
     */
    public void BuildTree() {
        int iteration = 0;

        while (iteration < maxIterations) {
            State select = selectionPolicy.Select(root);
            select.Expand();
            State next = selectionPolicy.SelectFromExpansion(select.GetActionsAndChildren());

        }
    }

}
