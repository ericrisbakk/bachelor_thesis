package test.tictactoe;

import main.mcts.ResultUCT;
import main.mcts.base.Action;
import main.mcts.base.IResult;
import main.mcts.base.INodeMCTS;

public class TicTacToeNode implements INodeMCTS {

    Action lastAction;
    ResultUCT result;

    @Override
    public Action GetLastAction() {
        return lastAction;
    }

    @Override
    public IResult GetResult() {
        return result;
    }

    @Override
    public void Expand() {

    }

    @Override
    public INodeMCTS[] GetChildren() {
        return new INodeMCTS[0];
    }

    @Override
    public INodeMCTS GetParent() {
        return null;
    }

    @Override
    public INodeMCTS DeepCopy() {
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
