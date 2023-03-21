package marcel.lang.printer;

public interface Printer {

  void print(CharSequence s);
  default void print(Object o) {
    print(o instanceof CharSequence ? (CharSequence) o : String.valueOf(o));
  }
  void println(CharSequence s);
  default void println(Object o) {
    println(o instanceof CharSequence ? (CharSequence) o : String.valueOf(o));
  }
  void println();

}