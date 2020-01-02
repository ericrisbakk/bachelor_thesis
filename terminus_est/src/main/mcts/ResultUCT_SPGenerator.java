package main.mcts;

import main.mcts.base.IResultGenerator;

public class ResultUCT_SPGenerator implements IResultGenerator {

    @Override
    public ResultUCT_SP Generate() {
        return new ResultUCT_SP();
    }
}
