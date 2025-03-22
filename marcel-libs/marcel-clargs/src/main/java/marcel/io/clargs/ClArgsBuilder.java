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
      CliOption cliOption = toCliOption(optionAnnotation, field);
      options.addOption(cliOption);
    }
    return options;
  }

  private CliOption toCliOption(Option annotation, Field field) {
    String description = annotation.description();
    String defaultValue = annotation.defaultValue();
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

    if (optionalArg && !type.isArray()) { // TODO check this. Why?
      throw new RuntimeException("Attempted to set optional argument for non array type");
    }
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
    return new CliOption(builder.build(), defaultValue);
  }


  public void setOptionsFromAnnotations(OptionsAccessor optionsAccessor, Object instance, Class<?> clazz) {
    for (Field field : clazz.getDeclaredFields()) {
      Option optionAnnotation = field.getAnnotation(Option.class);
      if (optionAnnotation == null || (field.getModifiers() & Modifier.STATIC) != 0) continue;

      CliOption option = optionsAccessor.getOption(optionAnnotation, field);
      if (option == null) continue;
      if ((field.getModifiers() & Modifier.PUBLIC) == 0) {
        field.setAccessible(true);
      }
      setFieldValue(field, option, instance);
    }
  }

  @SneakyThrows
  private void setFieldValue(Field field, CliOption option, Object instance) {
    if (option.getConverter() != null) {
      // TODO wrap exception in runtime exception and provide info about context in which it happened
      field.set(instance, option.getConverter().apply(option.getValue()));
      return;
    }
    Object value;
    if (field.getType() == boolean.class) {
      value = Boolean.parseBoolean(option.getValue());
    } else if (field.getType() == int.class) {
      value = Integer.parseInt(option.getValue());
    } else if (field.getType() == long.class) {
      value = Long.parseLong(option.getValue());
    } else if (field.getType() == short.class) {
      value = Short.parseShort(option.getValue());
    } else if (field.getType() == byte.class) {
      value = Byte.parseByte(option.getValue());
    } else if (field.getType() == char.class) {
      value = option.getValue().charAt(0);
    } else if (field.getType() == double.class) {
      value = Double.parseDouble(option.getValue());
    } else if (field.getType() == float.class) {
      value = Float.parseFloat(option.getValue());
    } else if (field.getType() == void.class) {
      value = option.getValue();
    } else if (field.getType() == String.class) {
      value = option.getValue();
    } else if (field.getType().isArray()) {
      throw new UnsupportedOperationException("Not supported yet.");
    } else {
      throw new UnsupportedOperationException("Unsupported type: " + field.getType());
    }
    field.set(instance, value);
  }

  public OptionsAccessor parse(Options options, String[] args) {
    DefaultParser parser = new DefaultParser();
    try {
      CommandLine commandLine = parser.parse(options, args, stopAtNonOption);
      return new OptionsAccessor(commandLine);
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
