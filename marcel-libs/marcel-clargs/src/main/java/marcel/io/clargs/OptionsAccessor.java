package marcel.io.clargs;

import lombok.SneakyThrows;
import marcel.lang.lambda.Lambda1;
import marcel.lang.methods.DefaultMarcelMethods;
import marcel.util.primitives.collections.lists.CharArrayList;
import marcel.util.primitives.collections.lists.CharList;
import marcel.util.primitives.collections.lists.DoubleArrayList;
import marcel.util.primitives.collections.lists.DoubleList;
import marcel.util.primitives.collections.lists.FloatArrayList;
import marcel.util.primitives.collections.lists.FloatList;
import marcel.util.primitives.collections.lists.IntArrayList;
import marcel.util.primitives.collections.lists.IntList;
import marcel.util.primitives.collections.lists.LongArrayList;
import marcel.util.primitives.collections.lists.LongList;
import marcel.util.primitives.collections.sets.CharOpenHashSet;
import marcel.util.primitives.collections.sets.CharSet;
import marcel.util.primitives.collections.sets.DoubleOpenHashSet;
import marcel.util.primitives.collections.sets.DoubleSet;
import marcel.util.primitives.collections.sets.FloatOpenHashSet;
import marcel.util.primitives.collections.sets.FloatSet;
import marcel.util.primitives.collections.sets.IntOpenHashSet;
import marcel.util.primitives.collections.sets.IntSet;
import marcel.util.primitives.collections.sets.LongOpenHashSet;
import marcel.util.primitives.collections.sets.LongSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Converter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.TypeHandler;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptionsAccessor {

  private static final Map<Class<?>, Collector<?, ?, ?>> COLLECTION_COLLECTORS = Map.ofEntries(
      // lists
      Map.entry(List.class, Collectors.toList()),
      Map.entry(IntList.class, Collector.of(() -> (IntList) new IntArrayList(), (list, element) -> list.add(Parsers.parseInt(element.toString())), IntList::leftShift)),
      Map.entry(LongList.class, Collector.of(() -> (LongList) new LongArrayList(), (list, element) -> list.add(Parsers.parseLong(element.toString())), LongList::leftShift)),
      Map.entry(FloatList.class, Collector.of(() -> (FloatList) new FloatArrayList(), (list, element) -> list.add(Parsers.parseFloat(element.toString())), FloatList::leftShift)),
      Map.entry(DoubleList.class, Collector.of(() -> (DoubleList) new DoubleArrayList(), (list, element) -> list.add(Parsers.parseDouble(element.toString())), DoubleList::leftShift)),
      Map.entry(CharList.class, Collector.of(() -> (CharList) new CharArrayList(), (list, element) -> list.add(element.toString().charAt(0)), CharList::leftShift)),
      // sets
      Map.entry(Set.class, Collectors.toSet()),
      Map.entry(IntSet.class, Collector.of(() -> (IntSet) new IntOpenHashSet(), (set, element) -> set.add(Parsers.parseInt(element.toString())), IntSet::leftShift)),
      Map.entry(LongSet.class, Collector.of(() -> (LongSet) new LongOpenHashSet(), (set, element) -> set.add(Parsers.parseLong(element.toString())), LongSet::leftShift)),
      Map.entry(FloatSet.class, Collector.of(() -> (FloatSet) new FloatOpenHashSet(), (set, element) -> set.add(Parsers.parseFloat(element.toString())), FloatSet::leftShift)),
      Map.entry(DoubleSet.class, Collector.of(() -> (DoubleSet) new DoubleOpenHashSet(), (set, element) -> set.add(Parsers.parseDouble(element.toString())), DoubleSet::leftShift)),
      Map.entry(CharSet.class, Collector.of(() -> (CharSet) new CharOpenHashSet(), (set, element) -> set.add(element.toString().charAt(0)), CharSet::leftShift))
  );

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
    initCliOptionConverters();
  }

  public Object getArguments(Class<?> expectedType, Arguments arguments,
                             // nullable
                             Converter converter) {
    if (expectedType == String.class) {
      return String.join(" ", commandLine.getArgList());
    }
    Collector collector = COLLECTION_COLLECTORS.get(expectedType);
    if (collector == null) {
      throw new OptionParserException("Unsupported type " + expectedType + " for arguments");
    }
    Function<String, ?> function;
    if (converter != null) {
      function = (arg) -> convertArgument(arg, converter);
    } else if (arguments.elementsType() == Void.class || arguments.elementsType() == String.class) {
      function = Function.identity();
    } else {
      function = (arg) -> convertArgument(arg, TypeHandler.getDefault().getConverter(arguments.elementsType()));
    }
    return commandLine.getArgList()
        .stream()
        .map(function)
        .collect(collector);
  }

  @SneakyThrows
  private Object convertArgument(String arg, Converter converter) {
    try {
      return converter.apply(arg);
    } catch (NumberFormatException e) {
      throw new OptionParserException(
          "Invalid argument '%s': invalid number".formatted(arg), e);
    } catch (IllegalArgumentException e) {
      throw new OptionParserException(
          "Malformed argument '%s': %s".formatted(arg, e.getMessage()), e);
    }
  }

  public boolean getOptionValue(HelpOption optionAnnotation) {
    return parsedOptions.stream().anyMatch((opt) -> opt.getOpt().equals(optionAnnotation.shortName()));
  }

  public Object getOptionValue(Option optionAnnotation, Field field) {
    Optional<org.apache.commons.cli.Option> optOption = findOption(optionAnnotation, field, parsedOptions)
        .or(() -> findOption(optionAnnotation, field, allOptions));
    if (optOption.isEmpty()) {
      return null;
    }
    org.apache.commons.cli.Option cliOption = optOption.get();

    // handling collection
    if (Collection.class.isAssignableFrom(field.getType())) {
      Collector collector = COLLECTION_COLLECTORS.get(field.getType());
      if (collector == null) {
        throw new OptionParserException("Unsupported collection type " + field.getType().getSimpleName() + " for option " + getOptionDisplayedName(optionAnnotation, field));
      }
      String[] optionValues = commandLine.getOptionValues(cliOption);
      Stream<String> optionValuesStream = optionValues != null ? Arrays.stream(optionValues) : Stream.<String>empty();
      if (optionAnnotation.elementsType() != Void.class) {
        TypeHandler typeHandler = TypeHandler.getDefault();
        return optionValuesStream.map(optionValue -> doConvert(typeHandler.getConverter(optionAnnotation.elementsType()), optionValue, optionAnnotation, field))
            .collect(collector);
      } else {
        return optionValuesStream
            .map(optionValue -> convertOptionValue(cliOption, optionAnnotation, field, optionValue))
            .collect(collector);
      }
    } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
      return commandLine.hasOption(cliOption.getOpt());
    }
    String optionValue = commandLine.getOptionValue(cliOption);
    if (optionValue == null) {
      return null;
    }
    return convertOptionValue(cliOption, optionAnnotation, field, optionValue);
  }

  private Object convertOptionValue(org.apache.commons.cli.Option cliOption,
                                    Option optionAnnotation, Field field, String optionValue) {
    return doConvert(cliOption.getConverter(), optionValue, optionAnnotation, field);
  }

  @SneakyThrows
  private Object doConvert(Converter<?, ?> converter, String optionValue, Option optionAnnotation, Field field) {
    try {
      return converter.apply(optionValue);
    } catch (NumberFormatException e) {
      throw new OptionParserException(
          "Invalid option %s: invalid number".formatted(getOptionDisplayedName(optionAnnotation, field)), e);
    } catch (IllegalArgumentException e) {
      throw new OptionParserException(
          "Malformed option %s: %s".formatted(getOptionDisplayedName(optionAnnotation, field), e.getMessage()), e);
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

  private void initCliOptionConverters() {
    for (org.apache.commons.cli.Option cliOption : DefaultMarcelMethods.plus(allOptions, parsedOptions)) {
      Class<?> type = (Class<?>) cliOption.getType();
      if (type.isEnum()) {
        cliOption.setConverter(value -> Enum.valueOf((Class<Enum>) type, value));
      }
    }
  }

  static String getOptionDisplayedName(Option option, Field field) {
    if (!option.longName().isEmpty()) {
      return option.longName();
    }
    return field.getName();
  }
}
