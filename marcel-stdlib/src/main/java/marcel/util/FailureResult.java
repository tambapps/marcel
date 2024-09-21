package marcel.util;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.function.Function;

@AllArgsConstructor
final class FailureResult<T> implements Result<T> {

    private final Throwable exception;

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public T getOrDefault(T value) {
        return value;
    }

    @Override
    public T get() throws Throwable {
        throw exception;
    }

    @Override
    public T getOrElse(Function<Throwable, ? extends T> fallback) {
        return fallback.apply(exception);
    }

    @Override
    public T getOrNull() {
        return null;
    }

    @SneakyThrows
    @Override
    public T getOrThrow() {
        throw exception;
    }

    @Override
    public Throwable getExceptionOrNull() {
        return exception;
    }

    @Override
    public <U> Result<U> map(Function<? super T, ? extends U> transform) {
        return new FailureResult<>(exception);
    }

    @Override
    public <U> Result<U> flatMap(Function<? super T, Result<U>> f) {
        return new FailureResult<>(exception);
    }

    @Override
    public <U> Result<U> then(Result<U> result) {
        return new FailureResult<>(exception);
    }

    @Override
    public Result<T> recover(Function<Throwable, T> fallback) {
        return new SuccessResult<>(fallback.apply(exception));
    }

    @Override
    public int hashCode() {
        return exception.hashCode();
    }

    @Override
    public String toString() {
        return "Result[exception=" + exception + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Result<?>) {
            return LazyCallableResult.equals(this, (Result<?>) obj);
        }
        return super.equals(obj);
    }
}
