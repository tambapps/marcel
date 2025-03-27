package marcel.io.clargs;

import lombok.Getter;
import lombok.Setter;
import marcel.lang.Script;
import marcel.lang.compile.NullDefaultValue;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import static marcel.io.clargs.OptionsAccessor.getOptionName;
import static org.apache.commons.cli.Option.UNLIMITED_VALUES;

@Getter
@Setter
public class ClArgs {

  // util methods to use when using marcel scripts
  public static void init(
      Script instance,
      String[] args,
      @NullDefaultValue String usage,
      @NullDefaultValue String header,
      @NullDefaultValue String footer,
      @NullDefaultValue Boolean stopAtNonOption,
      @NullDefaultValue Integer width) {
    init(instance, args, () -> System.exit(1), usage, header, footer, stopAtNonOption, width);
  }

  public static void init(
      Script instance,
      String[] args,
      Runnable onError,
      @NullDefaultValue String usage,
      @NullDefaultValue String header,
      @NullDefaultValue String footer,
      @NullDefaultValue Boolean stopAtNonOption,
      @NullDefaultValue Integer width) {
    ClArgs builder = new ClArgs();
    if (usage != null) {
      builder.setUsage(usage);
    }
    if (header != null) {
      builder.setHeader(header);
    }
    if (footer != null) {
      builder.setFooter(footer);
    }
    if (stopAtNonOption != null) {
      builder.setStopAtNonOption(stopAtNonOption);
    }
    if (width != null) {
      builder.setWidth(width);
    }
    try {
      builder.parseFromInstance(instance, args);
    } catch (OptionParserException e) {
      System.out.println(e.getMessage());
      builder.usageFromInstance(instance);
      onError.run();
    }
  }

  /**
   * Usage summary displayed as the first line when <code>cli.usage()</code> is called.
   */
  private String usage = "marcl";

  /**
   * Defaults to stdout but you can provide your own PrintWriter if desired.
   */
  private PrintWriter writer = new PrintWriter(System.out);

  /**
   * Normally set internally but can be overridden if you want to customise how the usage message is displayed.
   */
  private HelpFormatter formatter = new HelpFormatter();

  /**
   * Optional additional message for usage; displayed after the usage summary but before the options are displayed.
   */
  private String header = "";

  /**
   * Optional additional message for usage; displayed after the options are displayed.
   */
  private String footer = "";

  /**
   * Indicates that option processing should continue for all arguments even
   * if arguments not recognized as options are encountered (default true).
   */
  private boolean stopAtNonOption = true;

  /**
   * Allows customisation of the usage message width.
   */
  private int width = HelpFormatter.DEFAULT_WIDTH;

  public void parseFromInstance(Object instance, List<String> args) {
    parseFromInstance(instance, args.toArray(String[]::new));
  }

  public void parseFromInstance(Object instance, String[] args) {
    Objects.requireNonNull(instance, "instance must not be null");
    Class<?> clazz = instance.getClass();
    Options options = getOptionsFromInstance(clazz);
    try {
      OptionsAccessor optionsAccessor = parse(options, args);
      setOptionsFromAnnotations(optionsAccessor, instance, clazz);
    } catch (ParseException pe) {
      writer.println("error: " + pe.getMessage());
      usage(options, findArgumentsFromInstance(instance));
    }
  }

  private Options getOptionsFromInstance(Class<?> clazz) {
    Options options = new Options();
    for (Field field : clazz.getDeclaredFields()) {
      Option optionAnnotation = field.getAnnotation(Option.class);
      if (optionAnnotation == null) continue;
      org.apache.commons.cli.Option cliOption = toCliOption(optionAnnotation, field);
      options.addOption(cliOption);
    }
    return options;
  }

  private org.apache.commons.cli.Option toCliOption(Option annotation, Field field) {
    String description = annotation.description();
    char valueSeparator = 0;
    if (!annotation.valueSeparator().isEmpty()) valueSeparator = annotation.valueSeparator().charAt(0);
    boolean optionalArg = annotation.optional();

    // TODO handle convert. For that I will have to handle Lambdas as annotation parameters first
    Class<?> convert = annotation.convert() != Void.class ? annotation.convert() : null;
    String longName = annotation.longName().isEmpty() ? field.getName() : annotation.longName();
    String shortName = annotation.shortName().isEmpty() ? null : annotation.shortName();

    org.apache.commons.cli.Option.Builder builder = org.apache.commons.cli.Option.builder(shortName != null ? shortName : longName);
    if (shortName != null) {
      // yes, because if only the longName was specified, the longName is the main option name as passed in the builder
      builder.longOpt(longName);
    }
    Class<?> type = PrimitiveToWrapperUtil.getWrapperClassOrSelf(field.getType());
    builder.type(type);

    if (description != null) builder.desc(description);
    if (valueSeparator != 0) builder.valueSeparator(valueSeparator);
    if (type.isArray()) {
      builder.optionalArg(optionalArg);
    }
    boolean isFlag = type.getSimpleName().toLowerCase().equals("boolean");
    if (!isFlag) {
      String numberOfArguments = annotation.numberOfArguments();
      if (!numberOfArguments.matches("(\\d+)|((\\d+)?\\+)|\\*")) {
        throw new OptionParserException("Invalid number of arguments " + numberOfArguments + " for option " + (shortName != null ? shortName : longName));
      }
      if (numberOfArguments.matches("\\d+")) {
        builder.numberOfArgs(Integer.parseInt(numberOfArguments));
      } else if (numberOfArguments.equals("*") || numberOfArguments.endsWith("+")) {
        builder.numberOfArgs(UNLIMITED_VALUES);
      }
    }
    return builder.build();
  }


  public void setOptionsFromAnnotations(OptionsAccessor optionsAccessor, Object instance, Class<?> clazz) {
    for (Field field : clazz.getDeclaredFields()) {
      Option optionAnnotation = field.getAnnotation(Option.class);
      Arguments argumentsAnnotation = field.getAnnotation(Arguments.class);
      if (optionAnnotation != null) {
        setOptionsFromAnnotation(optionsAccessor, instance, field, optionAnnotation);
      } else if (argumentsAnnotation != null) {
        setFieldValue(field, instance, optionsAccessor.getArguments(field.getType()));
      }
    }
  }

  private void setOptionsFromAnnotation(OptionsAccessor optionsAccessor, Object instance, Field field, Option optionAnnotation) {
    Object optionValue = optionsAccessor.getOptionValue(optionAnnotation, field);
    if (optionValue == null) {
      if (!optionAnnotation.optional()) {
        throw new OptionParserException("Option %s is required".formatted(getOptionName(optionAnnotation, field)));
      } else {
        return;
      }
    }
    if (optionValue instanceof Collection<?>) {
      Collection<?> optionValues = (Collection<?>) optionValue;
      // verifying the number of arguments
      String numberOfArguments = optionAnnotation.numberOfArguments();
      if (numberOfArguments.matches("\\d+")) {
        int number = Integer.parseInt(numberOfArguments);
        if (number != optionValues.size()) {
          throw new OptionParserException("Expected %d values but got %d for option %s".formatted(number, optionValues.size(), getOptionName(optionAnnotation, field)));
        }
      } else if (numberOfArguments.matches("(\\d+)?\\+")) {
        numberOfArguments = numberOfArguments.substring(0, numberOfArguments.length() - 1);
        int number = numberOfArguments.isEmpty() ? 1 : Integer.parseInt(numberOfArguments);
        if (number != optionValues.size()) {
          throw new OptionParserException("Expected at least %d values but got %d for option %s".formatted(number, optionValues.size(), getOptionName(optionAnnotation, field)));
        }
      }
    }
    setFieldValue(field, instance, optionValue);
  }

  public OptionsAccessor parse(Options options, String[] args) throws ParseException {
    DefaultParser parser = new DefaultParser();
    CommandLine commandLine = parser.parse(options, args, stopAtNonOption);
    return new OptionsAccessor(commandLine, options);
  }

  private void setFieldValue(Field field, Object instance, Object value) {
    if ((field.getModifiers() & Modifier.PUBLIC) == 0) {
      field.setAccessible(true);
    }
    try {
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void usageFromInstance(Object instance) {
    Options options = getOptionsFromInstance(instance.getClass());
    usage(options, findArgumentsFromInstance(instance));
  }

  private Arguments findArgumentsFromInstance(Object instance) {
    return Arrays.stream(instance.getClass().getDeclaredFields())
        .map(f -> f.getAnnotation(Arguments.class))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  private void usage(Options options, Arguments arguments) {
    String usage = this.getUsage();
    if (arguments != null) {
      usage += " " + arguments.description();
    }
    formatter.printHelp(writer, width, usage, header, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, footer);
    writer.flush();
  }
}
