package main.TerminusEst;

/**
 * A solution contains both a network, and a hybridization number.
 */
public class TerminusEstSolution {
    public Network root;
    public int hyb;
    public double runtime;

    public TerminusEstSolution(Network root, int hyb, double runtime) {
        this.root = root;
        this.hyb = hyb;
        this.runtime = runtime;
    }

    @Override
    public String toString() {
        String s = "";
        if (root == null)
            s += "NULL";
        else
            s += "NETWORK";

        return s + ", " + hyb + ", " + runtime;
    }
}
