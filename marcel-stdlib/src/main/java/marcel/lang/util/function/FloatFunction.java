package marcel.lang.util.function;

@FunctionalInterface
public interface FloatFunction<R> {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    R apply(float value);
}
