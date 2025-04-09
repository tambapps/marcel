package marcel.io.clargs;

import lombok.Getter;
import lombok.Setter;
import marcel.lang.Script;
import marcel.lang.compile.NullDefaultValue;
import marcel.lang.lambda.Lambda1;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Converter;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static marcel.io.clargs.OptionsAccessor.getOptionDisplayedName;
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
    init(instance, args, System::exit, usage, header, footer, stopAtNonOption, width);
  }

  public static void init(
      Script instance,
      String[] args,
      Consumer<Integer> exit,
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
      boolean help = builder.parseFromInstance(instance, args);
      if (help) {
        builder.usageFromInstance(instance);
        exit.accept(0);
      }
    } catch (OptionParserException e) {
      System.out.println(e.getMessage());
      builder.usageFromInstance(instance);
      exit.accept(1);
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
  private int width = 100;

  public boolean parseFromInstance(Object instance, List<String> args) {
    return parseFromInstance(instance, args.toArray(String[]::new));
  }

  public boolean parseFromInstance(Object instance, String[] args) {
    Objects.requireNonNull(instance, "instance must not be null");
    Class<?> clazz = instance.getClass();
    Options options = getOptionsFromInstance(clazz, instance);
    try {
      OptionsAccessor optionsAccessor = parse(options, args);
      return setOptionsFromAnnotations(optionsAccessor, instance, clazz);
    } catch (ParseException pe) {
      writer.println("error: " + pe.getMessage());
      usage(options, findArgumentsFromInstance(instance));
      return false;
    }
  }

  private Options getOptionsFromInstance(Class<?> clazz, Object instance) {
    Options options = new Options();
    for (Field field : clazz.getDeclaredFields()) {
      Option optionAnnotation = field.getAnnotation(Option.class);
      if (optionAnnotation != null) {
        options.addOption(toCliOption(instance, optionAnnotation, field));
      }
      HelpOption helpOptionAnnotation = field.getAnnotation(HelpOption.class);
      if (helpOptionAnnotation != null) {
        options.addOption(toCliOption(helpOptionAnnotation, field));
      }
    }
    return options;
  }

  // first arg nullable
  private org.apache.commons.cli.Option toCliOption(Object instance, Option annotation, Field field) {
    String description = annotation.description();
    char valueSeparator = 0;
    if (!annotation.valueSeparator().isEmpty()) valueSeparator = annotation.valueSeparator().charAt(0);
    boolean optionalArg = annotation.required();

    String longName = annotation.longName().isEmpty() ? field.getName() : annotation.longName();
    String shortName = annotation.shortName().isEmpty() ? null : annotation.shortName();

    org.apache.commons.cli.Option.Builder builder = org.apache.commons.cli.Option.builder(shortName != null ? shortName : longName);
    if (shortName != null) {
      // yes, because if only the longName was specified, the longName is the main option name as passed in the builder
      builder.longOpt(longName);
    }

    Class<?> converterClass = annotation.converter() != Void.class ? annotation.converter() : null;
    if (converterClass != null) {
      Lambda1 lambda1 = instantiateLambda(converterClass);
      Converter<?, ?> converter = lambda1::apply;
      builder.converter(converter);
    }
    Class<?> type = ReflectionUtils.getObjectClass(field.getType());
    builder.type(type);

    if (instance != null && (!hasDefaultValue(instance, field) || !annotation.required())) {
      description += ". default: " + ReflectionUtils.getFieldValue(instance, field);
    }
    if (description != null) builder.desc(description);
    if (valueSeparator != 0) builder.valueSeparator(valueSeparator);
    if (type.isArray()) {
      builder.optionalArg(optionalArg);
    }
    boolean isFlag = type.getSimpleName().toLowerCase().equals("boolean");
    if (!isFlag) {
      OptionArity arity = OptionArity.of(annotation.arity());
      if (arity.isConstant()) {
        builder.numberOfArgs(arity.getN());
      } else {
        // we'll manually check the number of args when extracting args
        builder.numberOfArgs(UNLIMITED_VALUES);
      }
    }
    return builder.build();
  }
  private org.apache.commons.cli.Option toCliOption(HelpOption annotation, Field field) {
    if (field.getType() != boolean.class) {
      throw new OptionParserException("Help option field " + field.getName() + " should be of boolean type");
    }
    return org.apache.commons.cli.Option.builder(annotation.shortName())
        .longOpt(annotation.longName())
        .type(boolean.class)
        .desc(annotation.description())
        .build();
  }

  private Lambda1<?, ?> instantiateLambda(Class<?> converterClass) {
    if (!Lambda1.class.isAssignableFrom(converterClass)) {
      throw new OptionParserException("Invalid lambda. Expected a lambda with 1 argument");
    }
    return ReflectionUtils.newInstance(converterClass);
  }


  public boolean setOptionsFromAnnotations(OptionsAccessor optionsAccessor, Object instance, Class<?> clazz) {
    boolean help = false;
    for (Field field : clazz.getDeclaredFields()) {
      Option optionAnnotation = field.getAnnotation(Option.class);
      HelpOption helpOptionAnnotation = field.getAnnotation(HelpOption.class);
      Arguments argumentsAnnotation = field.getAnnotation(Arguments.class);
      if (optionAnnotation != null) {
        setOptionsFromAnnotation(optionsAccessor, instance, field, optionAnnotation);
      } else if (argumentsAnnotation != null) {
        setFieldValue(field, instance, optionsAccessor.getArguments(field.getType()));
      } else if (helpOptionAnnotation != null) {
        help = optionsAccessor.getOptionValue(helpOptionAnnotation);
        setFieldValue(field, instance, help);
      }
    }
    return help;
  }

  private void setOptionsFromAnnotation(OptionsAccessor optionsAccessor, Object instance, Field field, Option optionAnnotation) {
    Object optionValue = optionsAccessor.getOptionValue(optionAnnotation, field);
    if (optionValue == null) {
      if (optionAnnotation.required() && hasDefaultValue(instance, field)) {
        throw new OptionParserException("Option %s is required".formatted(getOptionDisplayedName(optionAnnotation, field)));
      } else {
        return;
      }
    }
    if (optionValue instanceof Collection<?> optionValues) {
      // verifying the number of arguments
      OptionArity arity = OptionArity.of(optionAnnotation.arity());
      if (!arity.respects(optionValues.size())) {
        throw new OptionParserException("Expected %s value(s) for option %s".formatted(arity, getOptionDisplayedName(optionAnnotation, field)));
      }
    }
    if (optionAnnotation.validator() != Void.class) {
      Lambda1 lambda1 = instantiateLambda(optionAnnotation.validator());
      try {
        lambda1.apply(optionValue);
      } catch (IllegalArgumentException iae) {
        throw new OptionParserException("Invalid option %s: %s".formatted(getOptionDisplayedName(optionAnnotation, field), iae.getMessage()), iae);
      }
    }
    setFieldValue(field, instance, optionValue);
  }

  private boolean hasDefaultValue(Object instance, Field field) {
    Object value = ReflectionUtils.getFieldValue(instance, field);
    if (value == null) {
      return true;
    }
    if (value instanceof Number n) {
      return n.intValue() == 0;
    } else if (value instanceof Boolean b) {
      return !b;
    } else if (value instanceof Character c) {
      return c == '\0';
    }
    return false;
  }

  public OptionsAccessor parse(Options options, String[] args) throws ParseException {
    DefaultParser parser = new DefaultParser();
    CommandLine commandLine = parser.parse(options, args, stopAtNonOption);
    return new OptionsAccessor(commandLine, options);
  }

  private void setFieldValue(Field field, Object instance, Object value) {
    try {
      ReflectionUtils.setFieldValue(field, instance, value);
    } catch (IllegalArgumentException e) {
      String message = value != null ? "ERROR: Tried to set incompatible value '%s' of type %s to field %s of type %s".formatted(value, value.getClass().getSimpleName(), field.getName(), field.getType().getSimpleName())
          : "ERROR: Tried to set incompatible value %s to field %s of type %s".formatted(value, field.getName(), field.getType().getSimpleName());
      throw new OptionParserException(message, e);
    }
  }

  public void usageFromInstance(Object instance) {
    Options options = getOptionsFromInstance(instance.getClass(), instance);
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
