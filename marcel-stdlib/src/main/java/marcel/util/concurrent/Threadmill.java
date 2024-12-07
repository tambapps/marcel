package marcel.util.concurrent;

import lombok.SneakyThrows;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
      try {
        if (!context.executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)) {
          context.executorService.shutdownNow();
        }
      } catch (InterruptedException ex) {
        context.executorService.shutdownNow();
        Thread.currentThread().interrupt();
      }
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
    Future<T> future = context.executorService.submit(callable);
    context.futures.add(future);
    return future;
  }

  /**
   * Run the provided runnable asynchronously thanks to a previously started Threadmill context
   * @param runnable the runnable to execute in the background
   * @return the Future representing the asynchronous call
   */
  public static Future<Void> runAsync(Runnable runnable) {
    ThredmillContext context = getCurrentContext();
    Future<Void> future = context.executorService.submit(runnable, null);
    context.futures.add(future);
    return future;
  }

  /**
   * Waits if necessary for the computation to complete, and then retrieves the Future's result
   * @param future the future
   * @return the result of the future
   * @param <T> the return type of the future
   */
  @SneakyThrows
  public static <T> T await(Future<T> future) {
    try {
      return future.get();
    } catch (ExecutionException e) {
      if (e.getCause() != null) throw e.getCause();
      else throw e;
    }
  }

  /**
   * Waits if necessary for the computation of all Futures to complete, and then retrieves all the results into an array
   * @param collection a collection holding futures
   * @return all the results put into an array
   */
  public static Object[] await(Collection<?> collection) {
    Object[] result = new Object[collection.size()];
    Iterator<?> iterator = collection.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      result[i++] = await((Future<?>) iterator.next());
    }
    return result;
  }

  /**
   * Waits if necessary for the computation of all Futures to complete, and then retrieves all the results into an array
   * @param array an array holding futures
   * @return all the results put into an array
   */
  public static Object[] await(Object[] array) {
    Object[] result = new Object[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = await((Future<?>) array[i]);
    }
    return result;
  }

  /**
   * Wait for all previously submitted async tasks to complete
   */
  public static void await() {
    ThredmillContext context = getCurrentContext();
    for (Future<?> future : context.futures) {
      await(future);
    }
    context.futures.clear();
  }

  public static void await(AwaitProgressListener listener) {
    ThredmillContext context = getCurrentContext();
    int size = context.futures.size();

    int lastProgress = -1;
    while (lastProgress < size) {
      int currentProgress = (int) context.futures.stream()
          .filter(Future::isDone)
          .count();
      if (currentProgress != lastProgress) {
        listener.onProgress(currentProgress, size);
      }
      lastProgress = currentProgress;
      try {
        Thread.sleep(100L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    context.futures.clear();
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
