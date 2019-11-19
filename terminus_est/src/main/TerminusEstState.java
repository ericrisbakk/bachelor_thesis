package main;

import main.mcts.base.Action;
import main.mcts.base.State;
import main.utility.IDeepCopy;

public class TerminusEstState implements State {
    @Override
    public void Apply(Action a) {

    }

    @Override
    public Action[] GetLegalActions() {
        return new Action[0];
    }

    @Override
    public boolean EndState() {
        return false;
    }

    @Override
    public IDeepCopy DeepCopy() {
        return null;
    }
}
