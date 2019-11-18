package test.tictactoe;

import main.mcts.SelectUCT;
import main.mcts.base.MCTS;

public class RunTicTacToeMCTS {
    public static void main(String[] args) {
        MCTS mcts = new MCTS(new TicTacToeState(), 100, new SelectUCT(), null);
    }
}
