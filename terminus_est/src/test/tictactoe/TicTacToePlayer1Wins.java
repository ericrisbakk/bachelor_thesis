package test.tictactoe;

import main.mcts.base.IHeuristic;
import main.mcts.base.INodeMCTS;
import main.mcts.base.State;

public class TicTacToePlayer1Wins implements IHeuristic {
    @Override
    public double Calculate(State state) {
        TicTacToeState ttt = (TicTacToeState) state;

        // Vertical and horizontal.
        for (int i = 0; i < 3; ++i) {
            if (Row3(ttt, i, 0, 0, 1))
                return ttt.board[i][0] == 1 ? 1 : 0;

            if (Row3(ttt, 0, i, 1, 0))
                return ttt.board[0][i] == 1 ? 1 : 0;
        }

        // Diagonals
        if (Row3(ttt, 0, 0, 1, 1))
            return ttt.board[0][0] == 1 ? 1 : 0;

        if (Row3(ttt, 0, 2, 1, -1))
            return ttt.board[0][2] == 1 ? 1 : 0;

        return 0.5;
    }

    boolean Row3(TicTacToeState s, int x, int y, int dx, int dy) {
        if (s.board[x][y] != s.board[x+dx][y+dy]
            || s.board[x+dx][y+dy] != s.board[x+(2*dx)][y+(2*dy)] )
            return false;

        return true;
    }
}
