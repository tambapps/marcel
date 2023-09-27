package marcel.lang;

/**
 * Object implementing a custom truth
 */
public interface TruthableObject {

    /**
     * Returns whether the object should be considered truthy or not
     *
     * @return whether the object should be considered truthy or not
     */
    boolean isTruthy();

}
