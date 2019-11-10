package mcts;

/**
 * Basic MCTS.
 */
public class MCTS {
    public int maxIterations = 0;
    public State root;

    public MCTS(State root, int maxIterations) {
        this.root = root;
        this.maxIterations = maxIterations;
    }

    /**
     * Builds the search tree
     */
    public void BuildTree() {

    }

}
