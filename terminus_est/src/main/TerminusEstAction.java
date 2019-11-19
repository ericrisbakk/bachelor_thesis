package main;

import main.TerminusEst.Tree;
import main.mcts.base.Action;
import main.utility.IDeepCopy;


public class TerminusEstAction implements Action {
    Tree taxon;

    public TerminusEstAction(Tree taxon) {
        this.taxon = taxon;
    }

    @Override
    public IDeepCopy DeepCopy() {
        return new TerminusEstAction(taxon);
    }
}
