package marcel.lang.printer;

import lombok.AllArgsConstructor;

import java.io.PrintStream;

@AllArgsConstructor
public class PrintStreamPrinter implements Printer {

  private final PrintStream printStream;

  @Override
  public void print(String s) {
    printStream.print(s);
  }

  @Override
  public void print(Object o) {
    printStream.print(o);
  }

  @Override
  public void println(String s) {
    printStream.println(s);
  }

  @Override
  public void println(Object o) {
    printStream.println(o);
  }

  @Override
  public void println() {
    printStream.println();
  }
}