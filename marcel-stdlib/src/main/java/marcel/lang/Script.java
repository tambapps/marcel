package marcel.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Console;
import java.util.Arrays;

/**
 * Base class for any Marcel scripts
 */
@NullMarked
@AllArgsConstructor
public abstract class Script {
  @Getter
  @Setter
  private Binding binding;

  public Script() {
    this(new Binding());
  }

  @Nullable
  public abstract Object run(String[] args);

  @Nullable
  public Object run() {
    return run(new String[0]);
  }

  @Nullable
  public <T> T getVariable(String name) {
    return binding.getVariable(name);
  }

  public void setVariable(String name, Object value) {
    binding.setVariable(name, value);
  }

  public void println() {
    System.out.println();
  }

  public void println(@Nullable Object o) {
    System.out.println(o);
  }

  public void println(@Nullable Object[] o) {
    System.out.println(Arrays.toString(o));
  }

  public void println(@Nullable int[] o) {
    System.out.println(Arrays.toString(o));
  }

  public void println(@Nullable long[] o) {
    System.out.println(Arrays.toString(o));
  }

  public void println(@Nullable float[] o) {
    System.out.println(Arrays.toString(o));
  }

  public void println(@Nullable double[] o) {
    System.out.println(Arrays.toString(o));
  }

  public void println(@Nullable short[] o) {
    System.out.println(Arrays.toString(o));
  }

  public void println(@Nullable byte[] o) {
    System.out.println(Arrays.toString(o));
  }

  public void println(@Nullable boolean[] o) {
    System.out.println(Arrays.toString(o));
  }

  public void println(@Nullable char[] o) {
    System.out.println(Arrays.toString(o));
  }

  public void print(@Nullable Object o) {
    System.out.print(o);
  }

  public void print(@Nullable Object[] o) {
    System.out.print(Arrays.toString(o));
  }

  public void print(@Nullable int[] o) {
    System.out.print(Arrays.toString(o));
  }

  public void print(@Nullable long[] o) {
    System.out.print(Arrays.toString(o));
  }

  public void print(@Nullable float[] o) {
    System.out.print(Arrays.toString(o));
  }

  public void print(@Nullable double[] o) {
    System.out.print(Arrays.toString(o));
  }

  public void print(@Nullable short[] o) {
    System.out.print(Arrays.toString(o));
  }

  public void print(@Nullable byte[] o) {
    System.out.print(Arrays.toString(o));
  }

  public void print(@Nullable boolean[] o) {
    System.out.print(Arrays.toString(o));
  }

  public void print(@Nullable char[] o) {
    System.out.print(Arrays.toString(o));
  }

  @Nullable
  public String readLine() {
    return getConsole().readLine();
  }

  @Nullable
  public String readLine(String fmt, Object ... args) {
    return getConsole().readLine(fmt, args);
  }

  @Nullable
  public String readPassword() {
    char[] c = getConsole().readPassword();
    return c != null ? new String(c) : null;
  }

  @Nullable
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
