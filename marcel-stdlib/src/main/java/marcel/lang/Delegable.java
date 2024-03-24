package marcel.lang;

/**
 * Interface specifying that the class is delegable, meaning that within this class
 * we can call methods from the delegate without having to reference it
 * @param <T> the type of the delegate
 */
public interface Delegable<T> {

    /**
     * Returns the delegate of this class
     * @return the delegate of this class
     */
    T getDelegate();

}
