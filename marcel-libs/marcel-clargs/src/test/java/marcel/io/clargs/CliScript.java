package marcel.io.clargs;

import lombok.Getter;
import marcel.lang.Script;

@Getter
public class CliScript extends Script {

  @Option
  private int n;

  @Option(longName = "size", shortName = "s")
  private Integer size;

  @Option(longName = "foo", shortName = "f")
  private String foo;

  @Option(longName = "help", shortName = "h")
  private boolean help;

  @Option(longName = "unspecified", shortName = "u", defaultValue = "default", optional = true)
  private String unspecified;

  @Override
  public Object run(String[] args) {
    return null;
  }
}
