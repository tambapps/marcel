package marcel.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for any Marcel scripts
 */
@AllArgsConstructor
public abstract class Script {
  @Getter
  @Setter
  private Binding binding;


  public Script() {
    this(new Binding());
  }

  public abstract Object run(String[] args);

  public Object run() {
    return run(null);
  }


  public <T> T getVariable(String name) {
    return binding.getVariable(name);
  }

  public <T> void setVariable(String name, Object value) {
    binding.setVariable(name, value);
  }
}
