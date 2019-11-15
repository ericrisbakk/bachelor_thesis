package mcts;

import mcts.base.Action;
import mcts.base.IResult;
import mcts.base.NodeMCTS;

import java.util.Collection;

public class NodeTerminusEst implements NodeMCTS {
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
    public Collection<NodeMCTS> GetChildren() {
        return null;
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
