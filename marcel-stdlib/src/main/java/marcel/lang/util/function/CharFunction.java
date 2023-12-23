package marcel.lang.util.function;

@FunctionalInterface
public interface CharFunction<R> {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    R apply(char value);
}
