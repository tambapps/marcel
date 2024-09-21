package marcel.util;

import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.function.Function;

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
    public T get() {
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
        return new SuccessResult<>(transform.apply(result));
    }

    @Override
    public <U> Result<U> tryMap(Function<? super T, ? extends U> transform) {
        try {
            return map(transform);
        } catch (Exception e) {
            return new FailureResult<>(e);
        }
    }

    @Override
    public <U> Result<U> flatMap(Function<? super T, Result<U>> f) {
        return f.apply(result);
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
    public int hashCode() {
        // Objects.hashCode() because result can be null
        return Objects.hashCode(result);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Result<?>) {
            return LazyCallableResult.equals(this, (Result<?>) obj);
        }
        return super.equals(obj);
    }
}
