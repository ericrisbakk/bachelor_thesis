package test.tictactoe;

import main.mcts.base.IHeuristic;
import main.mcts.base.INodeMCTS;

public class Player1Wins implements IHeuristic {
    @Override
    public double Calculate(INodeMCTS state) {
        return 0;
    }
}
