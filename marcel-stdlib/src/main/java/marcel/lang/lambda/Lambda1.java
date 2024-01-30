package marcel.lang.lambda;

import java.util.function.Function;

public interface Lambda1<T, R> extends Lambda, Runnable, Function<T, R> {

  R invoke(T arg0);

  default void run() {
    invoke(null);
  }

  @Override
  default R apply(T t) {
    return invoke(t);
  }
}
