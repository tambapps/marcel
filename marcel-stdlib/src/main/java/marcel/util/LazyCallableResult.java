package marcel.util;

import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;

@RequiredArgsConstructor
final class LazyCallableResult<T> implements Result<T> {

  private final Callable<T> callable;

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
    return getActualResult().map(transform);
  }

  @Override
  public <U> Result<U> flatMap(Function<? super T, Result<U>> f) {
    return getActualResult().flatMap(f);
  }

  @Override
  public <U> Result<U> then(Result<U> result) {
    return getActualResult().then(result);
  }

  @Override
  public Result<T> recover(Function<Throwable, T> fallback) {
    return getActualResult().recover(fallback);
  }

  private Result<T> getActualResult() {
    if (actualResult == null) {
      try {
        actualResult = Result.success(callable.call());
      } catch (Exception e) {
        actualResult = Result.failure(e);
      }
    }
    return actualResult;
  }

  @Override
  public int hashCode() {
    return getActualResult().hashCode();
  }

  @Override
  public String toString() {
    return getActualResult().toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Result<?>) {
      return equals(this, (Result<?>) obj);
    }
    return super.equals(obj);
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
