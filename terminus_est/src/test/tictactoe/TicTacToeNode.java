package test.tictactoe;

import main.mcts.ResultUCT;
import main.mcts.base.Action;
import main.mcts.base.IResult;
import main.mcts.base.NodeMCTS;

public class TicTacToeState implements NodeMCTS {

    Action lastAction;
    ResultUCT result;
    int currentPlayer;

    @Override
    public void Apply(Action a) {

    }

    @Override
    public Action GetLastAction() {
        return null;
    }

    @Override
    public IResult GetResult() {
        return null;
    }

    @Override
    public void Expand() {

    }

    @Override
    public NodeMCTS[] GetChildren() {
        return new NodeMCTS[0];
    }

    @Override
    public NodeMCTS GetParent() {
        return null;
    }

    @Override
    public NodeMCTS DeepCopy() {
        return null;
    }

    @Override
    public boolean IsExpanded() {
        return false;
    }

    @Override
    public boolean IsLeaf() {
        return false;
    }

    @Override
    public boolean IsTerminal() {
        return false;
    }

    @Override
    public int ExpandedChildCount() {
        return 0;
    }

    @Override
    public int ChildCount() {
        return 0;
    }
}
