package test.tictactoe;

import main.mcts.SelectUCT;
import main.mcts.SimulateRandom;
import main.mcts.base.MCTS;

public class RunTicTacToeMCTS {
    public static void main(String[] args) {
        TicTacToePlayer1Wins h = new TicTacToePlayer1Wins();
        MCTS mcts = new MCTS(new TicTacToeState(), 100, new SelectUCT(), new SimulateRandom(h));
    }
}
