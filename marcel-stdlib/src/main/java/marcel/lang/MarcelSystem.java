package marcel.lang;

import marcel.lang.printer.PrintStreamPrinter;
import marcel.lang.printer.Printer;

public class MarcelSystem {

  private static Printer printer;

  public static void setPrinter(Printer printer) {
    MarcelSystem.printer = printer;
  }

  public static Printer getPrinter() {
    if (printer == null) {
      printer = new PrintStreamPrinter(System.out);
    }
    return printer;
  }
}
