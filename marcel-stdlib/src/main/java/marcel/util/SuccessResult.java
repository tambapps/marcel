package marcel.util;

import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor
final class SuccessResult<T> implements Result<T> {

    private final T result;

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public T getOrDefault(T value) {
        return result;
    }

    @Override
    public T getOrElse(Function<Throwable, ? extends T> fallback) {
        return result;
    }

    @Override
    public T getOrNull() {
        return result;
    }

    @Override
    public T getOrThrow() {
        return result;
    }

    @Override
    public Throwable getExceptionOrNull() {
        return null;
    }

    @Override
    public <U> Result<U> map(Function<? super T, ? extends U> transform) {
        try {
            return new SuccessResult<>(transform.apply(result));
        } catch (Exception e) {
            return new FailureResult<>(e);
        }
    }

    @Override
    public <U> Result<U> flatMap(Function<? super T, Result<U>> f) {
        try {
            return f.apply(result);
        } catch (Exception e) {
            return new FailureResult<>(e);
        }
    }

    @Override
    public <U> Result<U> then(Result<U> result) {
        return result;
    }

    @Override
    public Result<T> recover(Function<Throwable, T> fallback) {
        return this;
    }

    @Override
    public Result<T> recover(Class<? extends Throwable> type, Supplier<T> fallback) {
        return this;
    }

    @Override
    public String toString() {
        return "Result[value=" + result + "]";
    }

    @Override
    public int hashCode() {
        // Objects.hashCode() because result can be null
        return Objects.hashCode(result);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Result<?> other) {
          return other.isSuccess() && Objects.equals(other.getOrNull(), result);
        }
        return super.equals(obj);
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.ofNullable(result);
    }
}
