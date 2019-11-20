package main.mcts;

import main.mcts.base.Action;
import main.mcts.base.IResult;
import main.mcts.base.IResultGenerator;
import main.mcts.base.State;

public class NodeMCTS {
    // Properties related to the problem.
    public State root;
    public Action lastAction;
    public static IResultGenerator resultGenerator;
    public IResult result;

    // Properties related to rest of tree.
    public NodeMCTS parent;
    public NodeMCTS[] children;
    public int depth;
    public int expandedChildren;

    // Node-specific properties.
    public boolean expanded;
    public boolean leaf;

    private NodeMCTS() {}

    public NodeMCTS(State root, Action lastAction, NodeMCTS parent) {
        this.root = root;
        this.lastAction = lastAction;
        this.parent = parent;
        if (parent == null)
            depth = 0;
        else
            depth = parent.depth + 1;

        expandedChildren = 0;

        // All new nodes are assumed to be leaf nodes.
        expanded = false;
        leaf = true;

        result = resultGenerator.Generate();
    }

    /**
     * Creates a deep copy of the root, then applies all the actions from root to the current node.
     * @return State in this node (does not affect root).
     */
    public State ConstructNodeState() {
        State newState = (State) root.DeepCopy();
        if (depth == 0)
            return newState;

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

    /**
     * Creates the children of this node using all the legal actions of the current state.
     */
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

        if (parent != null)
            parent.expandedChildren += 1;
    }


    public NodeMCTS[] GetChildren() {
        return children;
    }


    public NodeMCTS GetParent() {
        return parent;
    }


    public Action GetLastAction() {
        return lastAction;
    }


    public IResult GetResult() {
        return result;
    }

    public NodeMCTS GetRootNode() {
        NodeMCTS node = this;
        while (parent != null) {
            node = node.GetParent();
        }

        return node;
    }


    public boolean IsExpanded() {
        return expanded;
    }

    /**
     * Is true either if this node has not yet been expanded,
     * or the node has no children left
     * @return
     */
    public boolean IsLeaf() {
        return leaf;
    }


    public boolean IsTerminal() {
        return expanded && leaf;
    }


    public boolean IsRoot() {
        return (parent == null);
    }


    public int ExpandedChildCount() {
        return expandedChildren;
    }


    public int ChildCount() {
        return children.length;
    }

    /**
     * Returns (recursively) the search tree from this node, represented in Newick format.
     * @return
     */
    public String GetNewick() {
        if (leaf)
            return lastAction.toString() + "  " + ((ResultUCT) result).wins + ".." + ((ResultUCT) result).simulations;
        String s = "(";
        boolean first = true;
        for (int i = 0; i < children.length; ++i) {
            if (!first)
                s += ",";
            s += children[i].GetNewick();
            first = false;
        }

        s += ")" + (parent == null ? "Root" : lastAction.toString());
        s += "  " + ((ResultUCT) result).wins + ".." + ((ResultUCT) result).simulations;
        if (parent == null) s += ";";
        return s;
    }
}
