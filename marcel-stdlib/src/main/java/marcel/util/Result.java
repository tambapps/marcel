package marcel.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

// TODO document this
/**
 * Class representing an abstraction of the result of any operation having to possible outcomes: success or failure.
 *
 * @param <T> the type of the success value
 */
public interface Result<T> {

    /**
     * Returns an instance that encapsulates the given value as successful value.
     *
     * @param value the success value
     * @param <U>   the type of the success value
     * @return an instance that encapsulates the given value as successful value.
     */
    static <U> Result<U> success(U value) {
        return new SuccessResult<>(value);
    }

    /**
     * Returns an instance that encapsulates the given Throwable as failure.
     *
     * @param throwable the exception
     * @param <U>       the type of the success value if it were a success
     * @return an instance that encapsulates the given Throwable as failure.
     */
    static <U> Result<U> failure(Throwable throwable) {
        return new FailureResult<>(throwable);
    }

    /**
     * Returns an instance that encapsulates the given value as successful value if the callable succeeds, or
     * else an instance that encapsulates the given Throwable as failure.
     *
     * @param callable the callable to get the value from
     * @param <U>      the type of the success value
     * @return a result
     */
    static <U> Result<U> of(Callable<U> callable) {
        try {
            return success(callable.call());
        } catch (Exception e) {
            return failure(e);
        }
    }

    /**
     * Returns true if this instance represents a successful outcome. In this case isFailure returns false.
     *
     * @return true if this instance represents a successful outcome. In this case isFailure returns false.
     */
    boolean isSuccess();

    /**
     * Returns true if this instance represents a failed outcome. In this case isSuccess returns false.
     *
     * @return true if this instance represents a failed outcome. In this case isSuccess returns false.
     */
    boolean isFailure();

    /**
     * Returns the encapsulated value if this instance represents success or the defaultValue if it is failure
     *
     * @param value the default value
     * @return the encapsulated value if this instance represents success or the defaultValue if it is failure
     */
    T getOrDefault(T value);

    /**
     * Returns the encapsulated value if this instance represents success or the result of fallback function for the encapsulated Throwable exception if it is failure.
     *
     * @param fallback the fallback function
     * @return the encapsulated value if this instance represents success or the result of fallback function for the encapsulated Throwable exception if it is failure.
     */
    T getOrElse(Function<Throwable, ? extends T> fallback);

    /**
     * Returns the encapsulated value if this instance represents success or null if it is failure.
     *
     * @return the encapsulated value if this instance represents success or null if it is failure.
     */
    T getOrNull();

    /**
     * Returns the encapsulated value if this instance represents success or throws the encapsulated Throwable exception if it is failure.
     *
     * @return the encapsulated value if this instance represents success or throws the encapsulated Throwable exception if it is failure.
     */
    T getOrThrow();

    /**
     * Returns the encapsulated Throwable exception if this instance represents failure or null if it is success.
     *
     * @return the encapsulated Throwable exception if this instance represents failure or null if it is success.
     */
    Throwable getExceptionOrNull();

    /**
     * Returns the encapsulated result of the given transform function applied to the encapsulated value if this instance represents success or the original encapsulated Throwable exception if it is failure or an error
     * occured while applying the transformation.
     *
     * @param transform the mapping function
     * @param <U>       the type of the mapping
     * @return the encapsulated result of the given transform function applied to the encapsulated value if this instance represents success or the original encapsulated Throwable exception if it is failure.
     */
    <U> Result<U> map(Function<? super T, ? extends U> transform);

    /**
     * Returns the encapsulated result of the given transform function applied to the encapsulated value if this instance represents success or the original encapsulated Throwable exception if it is failure.
     *
     * @param f   the mapping function
     * @param <U> the type of the mapping
     * @return the encapsulated result of the given transform function applied to the encapsulated value if this instance represents success or the original encapsulated Throwable exception if it is failure.
     */
    <U> Result<U> flatMap(Function<? super T, Result<U>> f);


    /**
     * Returns the encapsulated result of the given result parameter if this instance represents success or the original encapsulated Throwable exception if it is failure.
     *
     * @param <U> the type of the mapping
     * @return the encapsulated result of the given result parameter if this instance represents success or the original encapsulated Throwable exception if it is failure.
     */
    <U> Result<U> then(Result<U> result);


    /**
     * Returns the encapsulated result of the given callable if this instance represents success or the original encapsulated Throwable exception if it is failure.
     *
     * @param <U> the type of the mapping
     * @param callable the callable to get the value from
     * @return the encapsulated result of the given callable if this instance represents success or the original encapsulated Throwable exception if it is failure.
     */
    default <U> Result<U> then(Callable<U> callable) {
        return then(of(callable));
    }

    /**
     * Returns the encapsulated result of the given fallback function applied to the encapsulated Throwable exception if this instance represents failure or the original encapsulated value if it is success.
     *
     * @param fallback the fallback function
     * @return the encapsulated result of the given transform function applied to the encapsulated Throwable exception if this instance represents failure or the original encapsulated value if it is success.
     */
    Result<T> recover(Function<Throwable, T> fallback);

    Optional<T> toOptional();
}
