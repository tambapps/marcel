package marcel.util;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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
    public Result<T> recover(Class<? extends Throwable> type, Supplier<T> fallback) {
        return type.isInstance(exception) ? new SuccessResult<>(Objects.requireNonNull(fallback.get(), "fallback function returned null value")) : this;
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
        if (obj instanceof Result<?> other) {
          return other.isFailure() && Objects.equals(other.getExceptionOrNull(), exception);
        }
        return super.equals(obj);
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.empty();
    }
}
