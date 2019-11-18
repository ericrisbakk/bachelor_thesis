package test.tictactoe;

import main.mcts.base.Action;
import main.mcts.base.State;
import main.utility.IDeepCopy;

public class TicTacToeState implements State {
    public int[][] board;
    public int player;
    public int movesTotal;
    private int player1Val = 1;
    private int player2Val = 2;

    public TicTacToeState() {
        board = new int[3][3];
        player = 1;
        movesTotal = 0;
    }

    @Override
    public void Apply(Action a) {
        TicTacToeAction ta = (TicTacToeAction) a;
        if (board[ta.x][ta.y] != 0) {
            System.out.println("ERROR: Attempting to overwrite previous action.");
            System.exit(0);
        }

        // Update board.
        board[ta.x][ta.y] = ta.player;
        ++ movesTotal;

        // Set new player.
        if (ta.player == 1)
            player = 2;
        else
            player = 1;
    }

    @Override
    public Action[] GetLegalActions() {
        if (EndState()) return new Action[0];

        TicTacToeAction[] actions = new TicTacToeAction[9-movesTotal];
        int added = 0;
        for (int i = 0; i < board.length; ++i) {
            for (int j = 0; j < board[i].length; ++j) {
                if (board[i][j] == 0) {
                    actions[added] = new TicTacToeAction(i, j, player);
                    ++added;
                }
            }
        }

        return actions;
    }

    @Override
    public boolean EndState() {
        if (Winner() > 0 || movesTotal == 9)
            return true;


        return false;
    }

    @Override
    public IDeepCopy DeepCopy() {
        if (movesTotal == 0)
            return new TicTacToeState();

        TicTacToeState newState = new TicTacToeState();
        for (int i = 0; i < board.length; ++i) {
            for (int j = 0; j < board[i].length; ++j) {
                newState.board[i][j] = board[i][j];
            }
        }

        newState.player = player;
        newState.movesTotal = movesTotal;

        return newState;
    }

    /**
     * @param s
     * @param x
     * @param y
     * @param dx
     * @param dy
     * @return True if non-zero three-in-a-row.
     */
    public static boolean Row3(TicTacToeState s, int x, int y, int dx, int dy) {
        if (s.board[x][y] == s.board[x+dx][y+dy]
                && s.board[x+dx][y+dy] == s.board[x+(2*dx)][y+(2*dy)]
                && s.board[x][y] != 0)
            return true;

        return false;
    }

    /**
     *
     * @return 0 if no win, 1 if P1 wins, 2 if P2 wins.
     */
    public int Winner() {
        // Vertical and horizontal.
        for (int i = 0; i < 3; ++i) {
            if (Row3(this, i, 0, 0, 1))
                return board[i][0];

            if (Row3(this, 0, i, 1, 0))
                return board[0][i];
        }

        // Diagonals
        if (Row3(this, 0, 0, 1, 1))
            return board[0][0];

        if (Row3(this, 0, 2, 1, -1))
            return board[0][2];

        return 0;
    }

    @Override
    public String toString() {
        String s = "Board:\n";
        for (int i = 0; i < board.length; ++i) {
            for (int j = 0; j < board[i].length; ++j) {
                s += " " + board[i][j] + " ";
            }

            s += "\n";
        }
        s +="\nPlayer: " + player;

        return s;
    }
}
