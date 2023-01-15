package marcel.lang;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for any Marcel scripts
 */
public abstract class Script {

  @Getter
  @Setter
  private Binding binding;

  public Script(Binding binding) {
    this.binding = binding;
  }

  public Script() {
    this(new Binding());
  }

  public abstract void run();

}
