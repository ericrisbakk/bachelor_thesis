package main.mcts.processing;

import main.mcts.NodeMCTS;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Class for traversing an MCTS search tree by using NodeMCTS. Has a collection of methods that can be overrided.
 */
public abstract class Traversal {
    public Comparator<NodeMCTS> comp;

    public void Setup() {}
    public void PostProcess() {}

    public void StartDepthFirstTraversal(NodeMCTS n) {
        Setup();
        DepthFirstTraversal(n);
        PostProcess();
    }

    protected void DepthFirstTraversal(NodeMCTS n) {
        BeforeStop(n);
        if (StopCondition(n)) {
            OnStop(n);
            return;
        }
        BeforeSort(n);
        NodeMCTS[] children = n.children.clone();
        if (comp != null) {
            Arrays.sort(children, comp);
        }
        BeforeRecursion(n, children);
        for (var c :
                children) {
            ChildPreProcess(c);
            DepthFirstTraversal(c);
            ChildPostProcess(c);
        }

        Final(n, children);
    }

    /**
     * @return true if we should stop searching from this node, false otherwise.
     */
    public boolean StopCondition(NodeMCTS n) { return true; }

    /**
     * Called before StopCondition method is called.
     * @param n
     */
    public void BeforeStop(NodeMCTS n) {}

    /**
     * Called if StopCondition was evaluated as true.
     * @param n
     */
    public void OnStop(NodeMCTS n) {}

    /**
     * Called if StopCondition was evaluated as false, before children have been sorted.
     * @param n
     */
    public void BeforeSort(NodeMCTS n) {}

    public void BeforeRecursion(NodeMCTS n, NodeMCTS[] children) {}

    public void Final(NodeMCTS n, NodeMCTS[] children) {}

    public void ChildPreProcess(NodeMCTS c) {}
    public void ChildPostProcess(NodeMCTS c) {}
}
