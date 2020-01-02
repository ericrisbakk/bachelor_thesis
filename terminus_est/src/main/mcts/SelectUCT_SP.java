package main.mcts;

import main.mcts.base.Action;

public class SelectUCT_SP extends SelectUCT {

    @Override
    public double GetUCT(NodeMCTS node) {
        ResultUCT val = (ResultUCT) node.GetResult();
        ResultUCT pVal = (ResultUCT) node.GetParent().GetResult();

        return super.GetUCT(node);
    }
}
