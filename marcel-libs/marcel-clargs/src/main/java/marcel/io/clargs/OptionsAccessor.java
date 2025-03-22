package marcel.io.clargs;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OptionsAccessor {

  private final CommandLine commandLine;
  private final List<org.apache.commons.cli.Option> allOptions;
  private final List<org.apache.commons.cli.Option> parsedOptions;

  public OptionsAccessor(CommandLine commandLine, Options allOptions) {
    this.commandLine = commandLine;
    this.allOptions = allOptions.getOptions()
        .stream()
        .toList();
    this.parsedOptions = Arrays.stream(commandLine.getOptions())
        .toList();
  }

  public Object getOptionValue(Option optionAnnotation, Field field) {
    Optional<org.apache.commons.cli.Option> optOption = findOption(optionAnnotation, field, parsedOptions)
        .or(() -> findOption(optionAnnotation, field, allOptions));
    if (optOption.isEmpty()) {
      return null;
    }
    org.apache.commons.cli.Option cliOption = optOption.get();
    String optionValue = cliOption.getValue();
    if (optionValue == null) {
      if (!optionAnnotation.defaultValue().isEmpty()) {
        optionValue = optionAnnotation.defaultValue();
      } else if (cliOption.getType().equals(boolean.class) || cliOption.getType().equals(Boolean.class)) {
        optionValue = String.valueOf(commandLine.hasOption(cliOption.getOpt()));
        cliOption.setConverter(Boolean::parseBoolean);
      }
    }
    try {
      return cliOption.getConverter().apply(optionValue);
    } catch (Throwable e) {
      // TODO wrap exception with appropriate error message
      throw new RuntimeException(e);
    }
  }

  private Optional<org.apache.commons.cli.Option> findOption(Option option, Field field, List<org.apache.commons.cli.Option> allOptions) {
    String optName = getOptionName(option, field);
    return allOptions.stream().filter((opt) -> opt.getOpt().equals(optName)).findFirst();
  }

  private String getOptionName(Option option, Field field) {
    if (!option.shortName().isEmpty()) {
      return option.shortName();
    } else {
      return option.longName().isEmpty() ? field.getName() : option.longName();
    }
  }
}
