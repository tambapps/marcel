package marcel.lang.util.function;

@FunctionalInterface
public interface ToCharFunction<T> {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    char applyAsChar(T value);
}
