package marcel.lang.lambda;

public interface Lambda1<T, R> extends Lambda, Runnable {

  R invoke(T arg0);

  default void run() {
    invoke(null);
  }
}
