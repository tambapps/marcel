package marcel.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for any Marcel scripts
 */
@AllArgsConstructor
public abstract class Script {

  final String[] args;

  @Getter
  @Setter
  private Binding binding;


  public Script(Binding binding) {
    this(null, binding);
  }

  public Script() {
    this(new Binding());
  }

  public abstract void run();

  public void run(String[] args) {
    binding.setVariable("args", args);
    run();
  }

}
