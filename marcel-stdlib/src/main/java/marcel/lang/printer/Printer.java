package marcel.lang.printer;

public interface Printer {

  void print(String s);
  default void print(Object o) {
    print(String.valueOf(o));
  }
  void println(String s);
  default void println(Object o) {
    println(String.valueOf(o));
  }
  void println();

}