package marcel.util;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.function.Function;

final class Results {

  private Results() {}

  abstract static class AbstractResult<T> implements Result<T> {

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Result<?>) {
        return Results.equals(this, (Result<?>) obj);
      }
      return super.equals(obj);
    }
  }
  static boolean equals(Result<?> r1, Result<?> r2) {
    if (r1.isSuccess() != r2.isSuccess()) return false;
    if (r1.isSuccess()) {
      return Objects.equals(r1.getOrNull(), r2.getOrNull());
    } else {
      return Objects.equals(r1.getExceptionOrNull(), r2.getExceptionOrNull());
    }
  }

  @AllArgsConstructor
  static final class SuccessResult<T> implements Result<T> {

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
    public String toString() {
      return "Result[value=" + result + "]";
    }

    @Override
    public int hashCode() {
      // Objects.hashCode() because result can be null
      return Objects.hashCode(result);
    }
  }

  @AllArgsConstructor
  static final class FailureResult<T> implements Result<T> {

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
  }
}
