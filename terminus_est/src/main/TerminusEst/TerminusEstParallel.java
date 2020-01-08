package main.TerminusEst;

import main.mcts.NodeMCTS;

import java.util.concurrent.Callable;

/**
 * Used to run TerminusEst partial solution computations in parallel.
 */
public class TerminusEstParallel implements Callable<TerminusEstSolution> {

    public NodeMCTS node;
    public TerminusEstV4 te4;
    public Tree t1;
    public Tree t2;
    public int target;
    public int depth;
    public int instance;

    public TerminusEstParallel(NodeMCTS node, TerminusEstV4 te4, Tree t1, Tree t2, int depth, int target, int instance) {
        this.node = node;
        this.te4 = te4;
        this.t1 = t1;
        this.t2 = t2;
        this.target = target;
        this.depth = depth;
        this.instance = instance;
    }

    @Override
    public TerminusEstSolution call() throws Exception {
        // System.out.println("Trying hybridization number: " + l);
        Tree T1 = t1.copy(null,null);
        Tree T2 = t2.copy(null,null);

        Network net = te4.hybNumAtMost( T1, T2, target, t1, t2, depth );
        if( net != null )
        {
            // We do not remove the fake root when computing a partial solution.
            if( net.root.children.size() != 1 )
            {
                System.out.println("CATASTROPHIC ERROR, we lost the fake root...");
                System.exit(0);
            }

            long timeEnd = System.currentTimeMillis();
            double seconds = TerminusEstV4.getIntervalInSeconds(timeEnd, te4.startTime);

            return new TerminusEstSolution(net, target, seconds);
        }

        return null;
    }
}
