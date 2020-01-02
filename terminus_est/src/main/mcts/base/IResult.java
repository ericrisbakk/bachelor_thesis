package main.mcts.base;

/**
 * Used for objects that store the results from a simulation in a single node.
 * Child results will also be applied to ancestor results.
 */
public interface IResult {
    /**
     * Update this result with that from other.
     * @param newResult newResult from which values will be carried over to this.
     */
    void Update(IResult newResult);

    /**
     * Update with a single result.
     * @param score
     */
    void Update(double score);

    String ToString();

    static double GetRoundedScore(double score, int decimals) {
        double dec = Math.pow(10, decimals);
        return Math.floor(score * dec) / (double) dec;
    }
}
