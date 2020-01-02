package main.mcts;

import main.mcts.base.Action;

public class SelectUCT_SP extends SelectUCT {
    public static double param_d = 0;

    @Override
    public double GetUCT(NodeMCTS node) {
        ResultUCT_SP val = (ResultUCT_SP) node.GetResult();
        ResultUCT_SP pVal = (ResultUCT_SP) node.GetParent().GetResult();

        double avg = ( (double) val.score / (double) val.simulations);
        double uctScore = ( avg + (param_c*Math.sqrt( (Math.log(pVal.simulations))/val.simulations )));

        uctScore += Math.sqrt((val.score2 - (val.simulations*Math.pow(avg, 2)) + param_d)/((double) val.simulations));

        return uctScore;
    }

    @Override
    public int GetVisits(NodeMCTS node) {
        return (int) ((ResultUCT_SP) node.GetResult()).simulations;
    }

    @Override
    public boolean HasBeenSimulatedFrom(NodeMCTS node) {
        return ((ResultUCT_SP) node.GetResult()).simulations != 0;
    }
}
