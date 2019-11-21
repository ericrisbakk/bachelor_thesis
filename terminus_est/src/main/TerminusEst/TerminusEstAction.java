package main.TerminusEst;

import main.mcts.base.Action;
import main.utility.IDeepCopy;


public class TerminusEstAction implements Action {
    public Tree taxon;

    public TerminusEstAction(Tree taxon) {
        this.taxon = taxon;
    }

    @Override
    public IDeepCopy DeepCopy() {
        return new TerminusEstAction(taxon);
    }

    @Override
    public String toString() {
        return taxon.getName();
    }
}
