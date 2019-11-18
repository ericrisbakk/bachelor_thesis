package main.mcts.base;

import main.mcts.NodeMCTS;

/**
 * MCTS Simulation step.
 */
public interface ISimulationPolicy {

    /**
     * Run simulation and get results.
     * @param node State we pass on.
     * @return Results from having run simulation on given state.
     */
    IResult Simulate(NodeMCTS node);

}
