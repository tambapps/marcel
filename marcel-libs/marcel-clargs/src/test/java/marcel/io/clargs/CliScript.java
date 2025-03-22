package marcel.io.clargs;

import lombok.Getter;
import marcel.lang.Script;

@Getter
public class CliScript extends Script {

  @Option
  private int n;

  @Override
  public Object run(String[] args) {
    return null;
  }
}
