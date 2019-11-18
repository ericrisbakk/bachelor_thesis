package test.tictactoe;

import main.mcts.base.IHeuristic;
import main.mcts.base.INodeMCTS;
import main.mcts.base.State;

public class TicTacToePlayer1Wins implements IHeuristic {
    @Override
    public double Calculate(State state) {
        TicTacToeState ttt = (TicTacToeState) state;

        int winner = ttt.Winner();
        if (winner == 1) return 1;
        if (winner == 2) return 0;

        return 0.5;
    }
}
