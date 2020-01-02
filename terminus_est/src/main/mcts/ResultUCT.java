package main.mcts;

import main.mcts.base.IResult;

import javax.xml.transform.Result;

/**
 * Logs the necessary information for doing single-player UCT-MCTS.
 */
public class ResultUCT implements IResult {
    public int wins = 0;
    public int simulations = 0;

    @Override
    public void Update(IResult newResult) {
        wins += ((ResultUCT) newResult).wins;
        simulations += ((ResultUCT) newResult).simulations;
    }

    @Override
    public void Update(double score) {
        if (score < 0 || score > 1) {
            System.out.println("ERROR - score outside [0,1] interval!");
        }
        wins += score;
        simulations += 1;
    }

    @Override
    public String ToString() {
        if (simulations > 0) {
            double ratio = (double) wins / (double) simulations;
            return  String.valueOf((float) ratio);
        }
        else {
            return "X";
        }
    }


}
