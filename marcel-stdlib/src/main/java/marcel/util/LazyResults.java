package marcel.util;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;
import java.util.function.Function;

final class LazyResults {

  private LazyResults() {}

  abstract static class AbstractLazyResult<T> extends Results.AbstractResult<T> {

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

    @Override
    public <U> Result<U> map(Function<? super T, ? extends U> transform) {
      return new MapResult<>(this, transform);
    }

    @Override
    public <U> Result<U> flatMap(Function<? super T, Result<U>> f) {
      return new FlatMapResult<>(this, f);
    }

    @Override
    public <U> Result<U> then(Result<U> result) {
      return new ThenResult<>(this, result);
    }

    @Override
    public Result<T> recover(Function<Throwable, T> fallback) {
      return new RecoverResult<>(this, fallback);
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

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Result<?>) {
        return Results.equals(this, (Result<?>) obj);
      }
      return super.equals(obj);
    }
  }

  @RequiredArgsConstructor
  static final class CallableResult<T> extends LazyResults.AbstractLazyResult<T> {

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
  static final class NullResult<T> extends LazyResults.AbstractLazyResult<T> {

    @Override
    Result<T> computeResult() {
      return Result.success(null);
    }
  }

  @RequiredArgsConstructor
  static final class MapResult<T, U> extends LazyResults.AbstractLazyResult<U> {

    private final Result<T> source;
    private final Function<? super T, ? extends U> transform;

    @Override
    Result<U> computeResult() {
      return source.map(transform);
    }
  }

  @RequiredArgsConstructor
  static final class FlatMapResult<T, U> extends LazyResults.AbstractLazyResult<U> {

    private final Result<T> source;
    private final Function<? super T, Result<U>> f;

    @Override
    Result<U> computeResult() {
      return source.flatMap(f);
    }
  }

  @RequiredArgsConstructor
  static final class ThenResult<T, U> extends LazyResults.AbstractLazyResult<U> {

    private final Result<T> source;
    private final Result<U> then;

    @Override
    Result<U> computeResult() {
      return source.then(then);
    }
  }

  @RequiredArgsConstructor
  static final class RecoverResult<T> extends LazyResults.AbstractLazyResult<T> {

    private final Result<T> source;
    private final Function<Throwable, T> fallback;

    @Override
    Result<T> computeResult() {
      return source.recover(fallback);
    }
  }
}
