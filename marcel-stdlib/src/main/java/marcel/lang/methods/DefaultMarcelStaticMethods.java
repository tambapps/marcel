package marcel.lang.methods;

import lombok.SneakyThrows;
import marcel.lang.MarcelSystem;

public class DefaultMarcelStaticMethods {

  public static void println(Object o) {
    MarcelSystem.getPrinter().println(o);
  }

  public static void println() {
    MarcelSystem.getPrinter().println();
  }

  public static void print(Object o) {
    MarcelSystem.getPrinter().print(o);
  }

  // TODO delete me once handled throw instruction
  @SneakyThrows
  public static void throwThrowable(Throwable throwable) {
    throw throwable;
  }
}
