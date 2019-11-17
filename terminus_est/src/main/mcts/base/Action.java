package main.mcts.base;

/**
 * Represents any discrete event which may change the state.
 * Ensure that a 'no-action' (e.g. passing your turn) is also encoded as a possible action in this,
 * as a Null object for an action is used to keep track of the root node.
 */
public interface Action {
}
