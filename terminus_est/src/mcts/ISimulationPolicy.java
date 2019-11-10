package mcts;

/**
 * MCTS Simulation step.
 */
public interface ISimulationPolicy {
    /**
     * Simulate from the given state.
     * @param state given state.
     */
    void Simulate(State state);
    
}
