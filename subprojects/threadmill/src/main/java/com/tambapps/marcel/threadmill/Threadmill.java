package com.tambapps.marcel.threadmill;

import lombok.SneakyThrows;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Threadmill {

  private static final Map<Long, ThredmillContext> CONTEXTS = new ConcurrentHashMap<>();
  private static volatile ExecutorServiceFactory executorServiceFactory;
  private static volatile ThreadIdSupplier threadIdSupplier;

  /**
   * Starts a new Threadmill context in the current thread. Use the returned closeble to close it
   * @return A Closeable allowing to close the Threadmill context
   */
  public static Closeable startNewContext() {
    Long threadId = getCurrentThreadId();
    if (CONTEXTS.containsKey(threadId)) throw new IllegalStateException("Cannot initialize new Threadmill context as there is already one present");
    ThredmillContext context = new ThredmillContext(getExecutorServiceFactory().newInstance());
    CONTEXTS.put(threadId, context);
    return () -> {
      CONTEXTS.remove(threadId);
      context.executorService.shutdown();
    };
  }

  /**
   * Supply the provided callable to be executed asynchronously thanks to a previously started Threadmill context
   * @param callable the callable to execute in the background
   * @return the Future representing the asynchronous call
   * @param <T> the type of the callable
   */
  public static <T> Future<T> supplyAsync(Callable<T> callable) {
    ThredmillContext context = getCurrentContext();

    return context.executorService.submit(() -> {
      Long threadId = getCurrentThreadId();
      CONTEXTS.put(threadId, context);
      try {
        return callable.call();
      } finally {
        CONTEXTS.remove(threadId);
      }
    });
  }

  /**
   * Run the provided runnable asynchronously thanks to a previously started Threadmill context
   * @param runnable the runnable to execute in the background
   * @return the Future representing the asynchronous call
   */
  public static Future<Void> runAsync(Runnable runnable) {
    ThredmillContext context = getCurrentContext();
    return context.executorService.submit(() -> {
      Long threadId = getCurrentThreadId();
      CONTEXTS.put(threadId, context);
      try {
        runnable.run();
      } finally {
        CONTEXTS.remove(threadId);
      }
    }, null);
  }

  private static ThredmillContext getCurrentContext() {
    ThredmillContext context = CONTEXTS.get(getCurrentThreadId());
    if (context == null) throw new IllegalStateException("Cannot run background operation: No Threadmill context was initialized");
    return context;
  }

  @SneakyThrows
  private static ExecutorServiceFactory getExecutorServiceFactory() {
    if (executorServiceFactory == null) {
      executorServiceFactory =
          isJavaXxOrPlus(21) ? Threadmill::newVirtualThreadExecutorService
              : () -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    }
    return executorServiceFactory;
  }

  @SneakyThrows
  private static ThreadIdSupplier getThreadIdSupplier() {
    if (threadIdSupplier == null) {
      String methodName = isJavaXxOrPlus(19) ? "threadId" : "getId";
      Method method = Thread.class.getMethod(methodName);
      threadIdSupplier = () -> {
        try {
          return (Long) method.invoke(Thread.currentThread());
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      };
    }
    return threadIdSupplier;
  }

  @SneakyThrows
  private static ExecutorService newVirtualThreadExecutorService() {
    return (ExecutorService) Executors.class.getMethod("newVirtualThreadPerTaskExecutor").invoke(null);
  }

  private static boolean isJavaXxOrPlus(int version8OrPlus) {
    String version = System.getProperty("java.version");
    if (version.contains(".")) return false;
    try {
      return Integer.parseInt(version) >= version8OrPlus;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @SneakyThrows
  private static Long getCurrentThreadId() {
    return getThreadIdSupplier().getCurrentThreadId();
  }
}
