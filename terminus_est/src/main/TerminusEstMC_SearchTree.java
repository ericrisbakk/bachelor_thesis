package main;

import main.TerminusEst.TerminusEstState;
import main.TerminusEst.TerminusEstV4;
import main.TerminusEst.Tree;
import main.mcts.*;
import main.mcts.base.MCTS;
import main.utility.Tuple2;

import java.util.ArrayList;

/**
 * Manages the aspects of the search tree created by MCTS.
 */
public class TerminusEstMC_SearchTree {
    public int trees;
    public int iterations;
    public int simulations;
    public double param_c;
    public double param_d;

    public TerminusEstMCTS manager;

    public TerminusEstMC_SearchTree(int trees, int iterations, int simulations, double param_c, double param_d, TerminusEstMCTS manager) {
        this.trees = trees;
        this.iterations = iterations;
        this.simulations = simulations;
        this.param_c = param_c;
        this.param_d = param_d;
        this.manager = manager;
    }

    public NodeMCTS GetSearchTree(TerminusEstV4 te4) {
        SelectUCT_SP select = new SelectUCT_SP();
        SelectUCT_SP.param_c = param_c;
        SelectUCT_SP.param_d = param_d;
        HeuristicNegativeDepth heuristic = new HeuristicNegativeDepth();
        ResultUCT_SPGenerator gen = new ResultUCT_SPGenerator();
        SimulateRandom sim = new SimulateRandom(simulations, heuristic, gen);
        MCTS mcts = new MCTS(iterations/trees, select, sim, gen);

        Tree T1 = te4.t1.copy(null, null);
        Tree T2 = te4.t2.copy(null, null);
        TerminusEstState state = new TerminusEstState(T1, T2, 0);

        if (manager.VERBOSE) System.out.println("Building search tree.");
        manager.timeSinceLastSearchTreeBuilt = System.currentTimeMillis();
        mcts.BuildTree(state);
        manager.timeSinceLastSearchTreeCompleted = System.currentTimeMillis();
        if (manager.VERBOSE) System.out.println("Search tree completed.");
        return mcts.root;
    }

    public NodeMCTS[] GetSearchTrees(TerminusEstV4 te4) {
        NodeMCTS[] ar = new NodeMCTS[trees];
        for (int i = 0; i < ar.length; ++i) {
            ar[i] = GetSearchTree(te4);
        }

        return ar;
    }

    public NodeMCTS GetBestFound(NodeMCTS node) {
        if (node.IsTerminal()) {
            return node;
        }

        if (!node.expanded && node.leaf) {
            return null;
        }

        NodeMCTS bestChild = null;
        for (int i = 0; i < node.children.length; ++i) {
            NodeMCTS option = GetBestFound(node.children[i]);
            if (option != null) {
                if (bestChild == null)
                    bestChild = option;
                else {
                    if (option.depth < bestChild.depth)
                        bestChild = option;
                }
            }
        }

        return bestChild;
    }

    public Tuple2<NodeMCTS, NodeMCTS> GetBestTreeAndLeaf(TerminusEstV4 te4) {
        NodeMCTS[] trees = GetSearchTrees(te4);
        NodeMCTS root = trees[0];
        NodeMCTS child = GetBestFound(trees[0]);
        for (int i = 1; i < trees.length; ++i) {
            NodeMCTS next = GetBestFound(trees[i]);
            if (next != null) {
                if (child == null) {
                    child = next;
                    root = trees[i];
                }
                else if (child.depth > next.depth) {
                    child = next;
                    root = trees[i];
                }
            }
        }

        return new Tuple2<>(root, child);
    }


}
