package marcel.io.clargs;

import lombok.Getter;
import marcel.lang.Script;

import java.io.File;
import java.util.List;

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

  @Option(longName = "unspecified", shortName = "u", optional = true)
  private String unspecified = "default";

  @Option
  private File path;

  @Arguments
  private List<String> args;

  @Arguments
  private String joinedArgs;

  @Override
  public Object run(String[] args) {
    return null;
  }
}
