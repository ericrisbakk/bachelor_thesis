package main.TerminusEst;

/**
 * A solution contains both a network, and a hybridization number.
 */
public class TerminusEstSolution {
    public Network root;
    public int hyb;

    public TerminusEstSolution(Network root, int hyb) {
        this.root = root;
        this.hyb = hyb;
    }
}
