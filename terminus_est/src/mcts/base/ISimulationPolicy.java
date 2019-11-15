package mcts.base;

/**
 * MCTS Simulation step.
 */
public interface ISimulationPolicy {

    /**
     * Run simulation and get results.
     * @param state State we pass on.
     * @return Results from having run simulation on given state.
     */
    IResult Simulate(NodeMCTS state);

}
