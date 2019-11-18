package main.mcts;

import main.mcts.base.*;

public class SimulateRandom implements ISimulationPolicy {
    IHeuristic heuristicP1Wins;

    public SimulateRandom(IHeuristic heuristicP1Wins) {
        this.heuristicP1Wins = heuristicP1Wins;
    }

    @Override
    public IResult Simulate(INodeMCTS node) {
        State s = ((NodeMCTS) node).ConstructNodeState();

        return null;
    }
}
