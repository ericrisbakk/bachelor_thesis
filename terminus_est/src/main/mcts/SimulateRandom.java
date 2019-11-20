package main.mcts;

import main.mcts.base.*;

import java.util.Random;

public class SimulateRandom implements ISimulationPolicy {
    Random r = new Random();
    IHeuristic heuristicP1Wins;
    int simulations;

    public SimulateRandom(int simulations, IHeuristic heuristicP1Wins) {
        this.heuristicP1Wins = heuristicP1Wins;
        this.simulations = simulations;
    }

    @Override
    public IResult Simulate(NodeMCTS node) {
        State s = ((NodeMCTS) node).ConstructNodeState();
        ResultUCT r = new ResultUCT();

        for (int i = 0; i < simulations; ++i) {
            State sim = (State) s.DeepCopy();
            while (!sim.EndState()) {
                System.out.println("\n------------- NEW SIMULATION -------------\n");
                Action[] actions = sim.GetLegalActions();
                sim.Apply( GetRandomAction(actions) );
            }

            double val = heuristicP1Wins.Calculate(sim);
            r.wins += val;
            r.simulations += 1;
        }

        return r;
    }

    public Action GetRandomAction(Action[] actions) {
        return actions[r.nextInt(actions.length)];
    }
}
