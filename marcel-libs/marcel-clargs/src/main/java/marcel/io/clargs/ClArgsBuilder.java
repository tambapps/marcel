package marcel.io.clargs;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class ClArgsBuilder {

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
    OptionsAccessor optionsAccessor = parse(options, args);
    setOptionsFromAnnotations(optionsAccessor, instance, clazz);
  }

  private Options getOptionsFromInstance(Class<?> clazz) {
    Options options = new Options();
    for (Field field : clazz.getDeclaredFields()) {
      Option optionAnnotation = field.getAnnotation(Option.class);
      if (optionAnnotation == null || (field.getModifiers() & Modifier.STATIC) != 0) continue;
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
    int numberOfArguments = annotation.numberOfArguments();
    String numberOfArgumentsString = annotation.numberOfArgumentsString().isEmpty() ? null : annotation.numberOfArgumentsString();

    // TODO handle convert. For that I will have to handle Lambdas as annotation parameters first
    Class<?> convert = annotation.convert() != Void.class ? annotation.convert() : null;
    String longName = annotation.longName().isEmpty() ? field.getName() : annotation.longName();
    String shortName = annotation.shortName().isEmpty() ? null : annotation.shortName();

    org.apache.commons.cli.Option.Builder builder = org.apache.commons.cli.Option.builder(shortName != null ? shortName : longName);
    if (shortName != null) {
      // yes, because if only the longName was specified, the longName is the main option name as passed in the builder
      builder.longOpt(longName);
    }
    if (numberOfArguments != 1) {
      if (numberOfArgumentsString != null) {
        throw new RuntimeException("You can't specify both 'numberOfArguments' and 'numberOfArgumentsString'");
      }
    }
    Class<?> type = PrimitiveToWrapperUtil.getWrapperClassOrSelf(field.getType());
    builder.type(type);

    boolean isFlag = type.getSimpleName().toLowerCase().equals("boolean");
    int nbArgs;
    if (numberOfArgumentsString != null) {
      throw new UnsupportedOperationException("Not supported yet.");
    } else {
      nbArgs = isFlag ? 0 : numberOfArguments;
    }

    if (description != null) builder.desc(description);
    if (valueSeparator != 0) builder.valueSeparator(valueSeparator);
    if (type.isArray()) {
      builder.optionalArg(optionalArg);
    }
    if (!isFlag) {
      builder.hasArg(true);
      builder.numberOfArgs(nbArgs);
    }
    return builder.build();
  }


  public void setOptionsFromAnnotations(OptionsAccessor optionsAccessor, Object instance, Class<?> clazz) {
    for (Field field : clazz.getDeclaredFields()) {
      Option optionAnnotation = field.getAnnotation(Option.class);
      if (optionAnnotation == null || (field.getModifiers() & Modifier.STATIC) != 0) continue;

      Object optionValue = optionsAccessor.getOptionValue(optionAnnotation, field);
      if (optionValue == null) {
        if (optionAnnotation.optional()) {
          // TODO throw exception
        } else {
          continue;
        }
      }
      if ((field.getModifiers() & Modifier.PUBLIC) == 0) {
        field.setAccessible(true);
      }
      try {
        field.set(instance, optionValue);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }

    }
  }

  public OptionsAccessor parse(Options options, String[] args) {
    DefaultParser parser = new DefaultParser();
    try {
      CommandLine commandLine = parser.parse(options, args, stopAtNonOption);
      return new OptionsAccessor(commandLine, options);
    } catch (ParseException pe) {
      writer.println("error: " + pe.getMessage());
      usage(options);
      return null;
    }
  }

  public void usageFromInstance(Object instance) {
    Options options = getOptionsFromInstance(instance.getClass());
    usage(options);
  }

  private void usage(Options options) {
    formatter.printHelp(writer, width, usage, header, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, footer);
    writer.flush();
  }
}
