package marcel.util;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.concurrent.Callable;
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

    @Override
    public final <U> Result<U> map(Function<? super T, ? extends U> transform) {
      return new MapResult<>(this, transform);
    }

    @Override
    public final <U> Result<U> flatMap(Function<? super T, Result<U>> f) {
      return new FlatMapResult<>(this, f);
    }

    @Override
    public final <U> Result<U> then(Result<U> result) {
      return new ThenResult<>(this, result);
    }

    @Override
    public final Result<T> recover(Function<Throwable, T> fallback) {
      return new RecoverResult<>(this, fallback);
    }
  }

  @AllArgsConstructor
  static final class SuccessResult<T> extends AbstractResult<T> {

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
  static final class FailureResult<T> extends AbstractResult<T> {

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
    public int hashCode() {
      return exception.hashCode();
    }

    @Override
    public String toString() {
      return "Result[exception=" + exception + "]";
    }
  }

  abstract static class AbstractLazyResult<T> extends AbstractResult<T> {

    private Result<T> actualResult;

    @Override
    public boolean isSuccess() {
      return getActualResult().isSuccess();
    }

    @Override
    public boolean isFailure() {
      return getActualResult().isFailure();
    }

    @Override
    public T getOrDefault(T value) {
      return getActualResult().getOrDefault(value);
    }

    @Override
    public T get() throws Throwable {
      return getActualResult().get();
    }

    @Override
    public T getOrElse(Function<Throwable, ? extends T> fallback) {
      return getActualResult().getOrElse(fallback);
    }

    @Override
    public T getOrNull() {
      return getActualResult().getOrNull();
    }

    @Override
    public T getOrThrow() {
      return getActualResult().getOrThrow();
    }

    @Override
    public Throwable getExceptionOrNull() {
      return getActualResult().getExceptionOrNull();
    }


    private Result<T> getActualResult() {
      if (actualResult == null) {
        actualResult = computeResult();
      }
      return actualResult;
    }

    abstract Result<T> computeResult();

    @Override
    public int hashCode() {
      return getActualResult().hashCode();
    }
  }

  @RequiredArgsConstructor
  static final class CallableResult<T> extends Results.AbstractLazyResult<T> {

    private final Callable<T> callable;

    @Override
    Result<T> computeResult() {
      try {
        return Result.success(callable.call());
      } catch (Exception e) {
        return Result.failure(e);
      }
    }
  }

  @RequiredArgsConstructor
  static final class NullResult<T> extends Results.AbstractLazyResult<T> {

    @Override
    Result<T> computeResult() {
      return Result.success(null);
    }
  }

  @RequiredArgsConstructor
  static final class MapResult<T, U> extends Results.AbstractLazyResult<U> {

    private final Result<T> source;
    private final Function<? super T, ? extends U> transform;

    @Override
    Result<U> computeResult() {
      return source.map(transform);
    }
  }

  @RequiredArgsConstructor
  static final class FlatMapResult<T, U> extends Results.AbstractLazyResult<U> {

    private final Result<T> source;
    private final Function<? super T, Result<U>> f;

    @Override
    Result<U> computeResult() {
      return source.flatMap(f);
    }
  }

  @RequiredArgsConstructor
  static final class ThenResult<T, U> extends Results.AbstractLazyResult<U> {

    private final Result<T> source;
    private final Result<U> then;

    @Override
    Result<U> computeResult() {
      return source.then(then);
    }
  }

  @RequiredArgsConstructor
  static final class RecoverResult<T> extends Results.AbstractLazyResult<T> {

    private final Result<T> source;
    private final Function<Throwable, T> fallback;

    @Override
    Result<T> computeResult() {
      return source.recover(fallback);
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

}
