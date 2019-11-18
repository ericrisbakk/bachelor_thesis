package main.utility;

/**
 * Object can create a deep copy of itself.
 */
public interface IDeepCopy {
    /**
     * @return Copy of object that is separate from the original.
     */
    IDeepCopy DeepCopy();
}
