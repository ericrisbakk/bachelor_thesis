package test.tictactoe;

import main.mcts.base.Action;
import main.mcts.base.State;

public class TicTacToeState implements State {
    public int[][] board;
    public int player;
    public int movesTotal;
    private int player1Val = 1;
    private int player2Val = 2;

    public TicTacToeState() {
        board = new int[3][3];
        player = 0;
        movesTotal = 0;
    }

    @Override
    public void Apply(Action a) {
        TicTacToeAction ta = (TicTacToeAction) a;
        if (board[ta.x][ta.y] != 0) {
            System.out.println("ERROR: Attempting to overwrite previous action.");
            System.exit(0);
        }

        board[ta.x][ta.y] = ta.player;
    }

    @Override
    public Action[] GetLegalActions() {
        if (movesTotal == 9) return null;
        int nextPlayer = player == 1 ? 2 : 1;
        TicTacToeAction[] actions = new TicTacToeAction[9-movesTotal];
        int added = 0;
        for (int i = 0; i < board.length; ++i) {
            for (int j = 0; j < board[i].length; ++j) {
                if (board[i][j] == 0) {
                    actions[added] = new TicTacToeAction(i, j, nextPlayer);
                    ++added;
                }
            }
        }

        return actions;
    }
}
