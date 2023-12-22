package marcel.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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

  protected void println() {
    out.println();
  }

  protected void println(Object o) {
    out.println(o);
  }

  protected void print(Object o) {
    out.print(o);
  }
}
