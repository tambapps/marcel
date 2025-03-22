package marcel.io.clargs;

import org.apache.commons.cli.CommandLine;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class OptionsAccessor {

  private final List<CliOption> options;

  public OptionsAccessor(CommandLine commandLine) {
    this.options = Arrays.stream(commandLine.getOptions())
        .map(CliOption.class::cast)
        .toList();
  }

  public CliOption getOption(Option option, Field field) {
    Predicate<CliOption> predicate;
    if (!option.shortName().isEmpty()) {
      predicate = (opt) -> opt.getOpt().equals(option.shortName());
    } else {
      String longName = option.longName().isEmpty() ? field.getName() : option.longName();
      predicate = (opt) -> opt.getOpt().equals(longName);
    }
    return options.stream().filter(predicate).findFirst().orElse(null);
  }
}
