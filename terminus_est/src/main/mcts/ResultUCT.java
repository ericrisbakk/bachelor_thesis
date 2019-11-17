package main.mcts;

import main.mcts.base.IResult;

import javax.xml.transform.Result;

/**
 * Logs the necessary information for doing single-player UCT-MCTS.
 */
public class ResultUCT implements IResult {
    int wins = 0;
    int simulations = 0;

    @Override
    public void Update(IResult newResult) {
        wins += ((ResultUCT) newResult).wins;
        simulations += ((ResultUCT) newResult).simulations;
    }
}
