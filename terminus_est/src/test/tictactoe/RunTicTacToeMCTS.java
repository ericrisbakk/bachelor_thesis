package test.tictactoe;

import main.mcts.ResultUCTGenerator;
import main.mcts.SelectUCT;
import main.mcts.SimulateRandom;
import main.mcts.base.Action;
import main.mcts.base.IResultGenerator;
import main.mcts.base.MCTS;
import java.util.Scanner;  // Import the Scanner class

/**
 * Test class to try MCTS. I'm fully aware that SelectUCT doesn't work work for 2 players (when it is player 2 turn,
 * it tries to maximize UCT, when it should minimize). Because my problem is "single-player", this is not something I
 * have to take into account right now, and this code is only meant to show that MCTS can run on a basic problem.
 */
public class RunTicTacToeMCTS {


    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        TicTacToePlayer1Wins h = new TicTacToePlayer1Wins();
        TicTacToeState game = new TicTacToeState();
        ResultUCTGenerator gen = new ResultUCTGenerator();
        MCTS mcts = new MCTS(100, new SelectUCT(), new SimulateRandom(30, h, gen), gen);

        System.out.println("Tic Tac Toe.");
        System.out.println("Start!.");

        while (!game.EndState()) {
            System.out.println(game.toString());
            TicTacToeAction a;
            if (game.player == 1) {
                mcts.BuildTree(game);
                a = (TicTacToeAction) mcts.GetBestAction();
            }
            else {
                System.out.println("\nTurn: " + game.movesTotal);
                Action[] legal = game.GetLegalActions();
                System.out.println("Choose: \n");
                for (int i = 0; i < legal.length; ++i) {
                    System.out.println(i +" - " + legal[i].toString() + "\n");
                }

                int inp;
                do {
                    System.out.println("Enter: ");
                    inp = s.nextInt();
                } while (!(inp >= 0 && inp < legal.length));
                a = (TicTacToeAction) legal[inp];
            }

            game.Apply(a);
        }

        System.out.println("Final board:\n" + game + "\n0");
        System.out.println("\nWINNER: " + game.Winner());
    }
}
