package main.mcts;

import main.mcts.base.IResult;
import main.mcts.base.IResultGenerator;

public class ResultUCT_SP implements IResult {
    public long score = 0;
    public long score2 = 0;
    public long simulations = 0;
    
    @Override
    public void Update(IResult newResult) {
        ResultUCT_SP u = (ResultUCT_SP) newResult;
        score += u.score;
        score2 += u.score2;
        simulations += u.simulations;
    }

    @Override
    public void Update(double score) {
        this.score += score;
        this.score2 += (long) Math.pow(score, 2);
        simulations += 1;
    }

    @Override
    public String ToString() {
        if (simulations > 0) {
            double ratio = (double) score / (double) simulations;
            return String.valueOf((float) ratio);
        }
        else {
            return "X";
        }
    }


}
