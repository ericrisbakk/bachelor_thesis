package main.mcts;

import main.mcts.base.Action;
import main.mcts.base.INodeMCTS;
import main.mcts.base.IResult;
import main.mcts.base.State;
import org.w3c.dom.Node;

public class NodeMCTS implements INodeMCTS {
    // Properties related to the problem.
    public State root;
    public Action lastAction;
    public IResult result;

    // Properties related to rest of tree.
    public NodeMCTS parent;
    public NodeMCTS[] children;
    public int depth;

    // Node-specific properties.
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

        // All new nodes are assumed to be leaf nodes.
        expanded = false;
        leaf = true;
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

        children = new NodeMCTS[actions.length];

        for (int i = 0; i < children.length; ++i) {
            children[i] = new NodeMCTS(root, actions[i], this);
        }

        expanded = true;
        if (actions.length == 0)
            leaf = true;
        else
            leaf = false;
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
    public INodeMCTS GetRootNode() {
        INodeMCTS node = this;
        while (parent != null) {
            node = node.GetParent();
        }

        return node;
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
    public boolean IsRoot() {
        return (parent == null);
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
