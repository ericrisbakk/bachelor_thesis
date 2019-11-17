package test.tictactoe;

import main.mcts.base.Action;

public class TicTacToeAction implements Action {
    public int x, y;
    public int player;

    public TicTacToeAction(int x, int y, int player) {
        this.x = x;
        this.y = y;
        this.player = player;
    }
}