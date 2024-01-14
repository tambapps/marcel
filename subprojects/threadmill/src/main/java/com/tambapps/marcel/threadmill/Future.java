package com.tambapps.marcel.threadmill;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class Future<T> extends CompletableFuture<T> {

  @Override
  public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
    return super.thenApplyAsync(fn, executor);
  }
}
