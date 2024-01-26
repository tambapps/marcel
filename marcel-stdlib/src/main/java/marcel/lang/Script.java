package marcel.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Console;
import java.io.PrintStream;

/**
 * Base class for any Marcel scripts
 */
@AllArgsConstructor
public abstract class Script {
  @Getter
  @Setter
  private Binding binding;

  private PrintStream out;

  public Script() {
    this(new Binding());
  }

  public Script(Binding binding) {
    this(binding, System.out);
  }

  public abstract Object run(String[] args);

  public Object run() {
    return run(null);
  }

  public <T> T getVariable(String name) {
    return binding.getVariable(name);
  }

  public void setVariable(String name, Object value) {
    binding.setVariable(name, value);
    if ("out".equals(name) && value instanceof PrintStream) {
      out = (PrintStream) value;
    }
  }

  public void println() {
    out.println();
  }

  public void println(Object o) {
    out.println(o);
  }

  public void print(Object o) {
    out.print(o);
  }

  public String readLine() {
    return getConsole().readLine();
  }

  public String readLine(String fmt, Object ... args) {
    return getConsole().readLine(fmt, args);
  }

  public String readPassword() {
    char[] c = getConsole().readPassword();
    return c != null ? new String(c) : null;
  }

  public String readPassword(String fmt, Object ... args) {
    char[] c = getConsole().readPassword(fmt, args);
    return c != null ? new String(c) : null;
  }

  protected Console getConsole() {
    Console console = System.console();
    if (console == null) throw new UnsupportedOperationException("No console was found");
    return console;
  }
}
