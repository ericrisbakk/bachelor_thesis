package main.mcts;

import main.mcts.base.IResultGenerator;

import javax.xml.transform.Result;

/**
 * Generates result instances for UCT.
 */
public class ResultUCTGenerator implements IResultGenerator<ResultUCT> {

    @Override
    public ResultUCT Generate() {
        return new ResultUCT();
    }
}
