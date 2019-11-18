package main.mcts;

import main.mcts.base.INodeMCTS;
import main.mcts.base.IResult;
import main.mcts.base.ISimulationPolicy;
import main.mcts.base.State;

public class SimulateRandom implements ISimulationPolicy {
    @Override
    public IResult Simulate(INodeMCTS node) {
        State s = ((NodeMCTS) node).ConstructNodeState();

        return null;
    }
}
