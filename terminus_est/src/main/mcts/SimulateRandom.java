package main.mcts;

import main.mcts.base.*;

import java.util.Random;

public class SimulateRandom implements ISimulationPolicy {
    Random r = new Random();
    IHeuristic heuristicP1Wins;
    int simulations;
    IResultGenerator resultGenerator;

    public SimulateRandom(int simulations, IHeuristic heuristicP1Wins, IResultGenerator resultGenerator) {
        this.heuristicP1Wins = heuristicP1Wins;
        this.simulations = simulations;
        this.resultGenerator = resultGenerator;
    }

    @Override
    public IResult Simulate(NodeMCTS node) {
        State s = ((NodeMCTS) node).ConstructNodeState();
        IResult r = resultGenerator.Generate();
//        System.out.println("\n--------------------------------------------");
//        System.out.println("------------- START SIMULATION -------------");
//        System.out.println("--------------------------------------------\n");
        for (int i = 0; i < simulations; ++i) {
            // System.out.println("\n------------- NEW SIMULATION -------------\n");
            State sim = (State) s.DeepCopy();
            while (!sim.EndState()) {
//                System.out.println("---");
                Action[] actions = sim.GetLegalActions();
                sim.Apply( GetRandomAction(actions) );
            }

            double val = heuristicP1Wins.Calculate(sim);

            r.Update(val);
        }

        return r;
    }

    public Action GetRandomAction(Action[] actions) {
        return actions[r.nextInt(actions.length)];
    }
}
