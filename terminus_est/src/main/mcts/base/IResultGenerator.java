package main.mcts.base;

import javax.xml.transform.Result;

/**
 * Generates instance of some IResult type as needed.
 */
public interface IResultGenerator {
    IResult Generate();
}
