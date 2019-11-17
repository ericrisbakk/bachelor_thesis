package test.tictactoe;

import main.mcts.base.Action;

public class TicTacToeAction implements Action {
    public int x, y;

    public TicTacToeAction(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
