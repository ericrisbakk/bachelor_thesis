package main.mcts;

import main.mcts.base.Action;
import main.mcts.base.INodeMCTS;
import main.mcts.base.IResult;
import main.mcts.base.State;

public class NodeMCTS implements INodeMCTS {
    public State root;
    public int depth;
    public Action lastAction;
    public IResult result;

    public NodeMCTS parent;
    public NodeMCTS[] children;

    public boolean expanded;
    public boolean leaf;

    public NodeMCTS(State root, Action lastAction, NodeMCTS parent) {
        this.root = root;
        this.lastAction = lastAction;
        this.parent = parent;
        if (parent == null)
            depth = 0;
        else
            depth = parent.depth + 1;

        expanded = false;
        leaf = false;
    }

    public State ConstructNodeState() {
        State newState = (State) root.DeepCopy();
        Action[] actions = new Action[depth];
        NodeMCTS toRoot = this;
        for (int i = 0; i < actions.length; ++i) {
            actions[i] = toRoot.lastAction;
            toRoot = toRoot.parent;
        }

        // Actions added in reverse.
        for (int i = actions.length-1; i >= 0; --i) {
            newState.Apply(actions[i]);
        }

        return newState;
    }

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
        State temp = ConstructNodeState();
        Action[] actions = temp.GetLegalActions();
    }

    @Override
    public INodeMCTS[] GetChildren() {
        return children;
    }

    @Override
    public INodeMCTS GetParent() {
        return parent;
    }

    @Override
    public INodeMCTS DeepCopy() {
        return null;
    }

    @Override
    public boolean IsExpanded() {
        return expanded;
    }

    @Override
    public boolean IsLeaf() {
        return leaf;
    }

    @Override
    public boolean IsTerminal() {
        return expanded && leaf;
    }

    @Override
    public int ExpandedChildCount() {
        return 0;
    }

    @Override
    public int ChildCount() {
        return children.length;
    }
}
