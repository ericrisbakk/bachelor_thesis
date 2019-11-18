package main.mcts.base;

/**
 * MCTS Simulation step.
 */
public interface ISimulationPolicy {

    /**
     * Run simulation and get results.
     * @param node State we pass on.
     * @return Results from having run simulation on given state.
     */
    IResult Simulate(INodeMCTS node);

}
