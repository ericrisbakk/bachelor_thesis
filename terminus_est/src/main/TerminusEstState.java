package main;

import main.TerminusEst.Network;
import main.TerminusEst.Tree;
import main.mcts.base.Action;
import main.mcts.base.State;
import main.utility.IDeepCopy;

import java.util.Vector;

public class TerminusEstState implements State {
    public Tree t1;
    public Tree t2;
    public Tree original1;
    public Tree original2;
    public int depth;

    public TerminusEstState(Tree t1, Tree t2, Tree original1, Tree original2, int depth) {
        this.t1 = t1;
        this.t2 = t2;
        this.original1 = original1;
        this.original2 = original2;
        this.depth = depth;

        // TODO: Consider if new state should be collapsed upon creation.
        // CollapseTrees();
    }

    @Override
    public void Apply(Action a) {
        CollapseTrees();

        TerminusEstAction tea = (TerminusEstAction) a;

        String zoekNaam = tea.taxon.getName();

        Tree alpha[] = new Tree [1];
        Tree beta[] = new Tree [1];

        Tree newGuyA = t1.copy(alpha,zoekNaam);
        Tree newGuyB = t2.copy(beta, zoekNaam);

        Tree killA = alpha[0].delete();
        if( killA == null )
            killA = newGuyA;

        Tree killB = beta[0].delete();
        if( killB == null )
            killB = newGuyB;

        t1 = killA;
        t2 = killB;
    }

    @Override
    public Action[] GetLegalActions() {
        return new Action[0];
    }

    @Override
    public boolean EndState() {
        Vector ST = main.TerminusEst.Tree.computeMaxSTsets(t1,t2);
        if( ST.size() == 1 ) {
            Tree.collapseMaxSTsets(t1, t2, ST);
            if (!t1.isLeaf()) {
                System.out.println("ERROR! Compatible ST-set not a leaf.");
                System.exit(0);
            }
            return true;
        }

        // TODO: Other finish states.

        return false;
    }

    @Override
    public IDeepCopy DeepCopy() {
        return null;
    }

    private void CollapseTrees() {
        Vector ST = Tree.computeMaxSTsets(t1,t2);
        Tree.collapseMaxSTsets(t1,t2,ST);
    }
}
