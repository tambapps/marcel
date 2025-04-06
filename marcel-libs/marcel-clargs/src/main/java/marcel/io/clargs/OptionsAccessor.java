package marcel.io.clargs;

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
import org.apache.commons.cli.Options;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptionsAccessor {

  private static final Map<Class<?>, Collector<?, ?, ?>> COLLECTION_COLLECTORS = Map.ofEntries(
      // lists
      Map.entry(List.class, Collectors.toList()),
      Map.entry(IntList.class, Collector.of(() -> (IntList) new IntArrayList(), (list, element) -> list.add(Integer.parseInt(element.toString())), IntList::leftShift)),
      Map.entry(LongList.class, Collector.of(() -> (LongList) new LongArrayList(), (list, element) -> list.add(Long.parseLong(element.toString())), LongList::leftShift)),
      Map.entry(FloatList.class, Collector.of(() -> (FloatList) new FloatArrayList(), (list, element) -> list.add(Float.parseFloat(element.toString())), FloatList::leftShift)),
      Map.entry(DoubleList.class, Collector.of(() -> (DoubleList) new DoubleArrayList(), (list, element) -> list.add(Double.parseDouble(element.toString())), DoubleList::leftShift)),
      Map.entry(CharList.class, Collector.of(() -> (CharList) new CharArrayList(), (list, element) -> list.add(element.toString().charAt(0)), CharList::leftShift)),
      // sets
      Map.entry(Set.class, Collectors.toSet()),
      Map.entry(IntSet.class, Collector.of(() -> (IntSet) new IntOpenHashSet(), (set, element) -> set.add(Integer.parseInt(element.toString())), IntSet::leftShift)),
      Map.entry(LongSet.class, Collector.of(() -> (LongSet) new LongOpenHashSet(), (set, element) -> set.add(Long.parseLong(element.toString())), LongSet::leftShift)),
      Map.entry(FloatSet.class, Collector.of(() -> (FloatSet) new FloatOpenHashSet(), (set, element) -> set.add(Float.parseFloat(element.toString())), FloatSet::leftShift)),
      Map.entry(DoubleSet.class, Collector.of(() -> (DoubleSet) new DoubleOpenHashSet(), (set, element) -> set.add(Double.parseDouble(element.toString())), DoubleSet::leftShift)),
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
  }

  public List<String> getArguments() {
    return commandLine.getArgList();
  }

  public Object getArguments(Class<?> expectedType) {
    if (expectedType == String.class) {
      return String.join(" ", getArguments());
    }
    Collector collector = COLLECTION_COLLECTORS.get(expectedType);
    if (collector == null) {
      throw new OptionParserException("Unsupported type " + expectedType + " for arguments");
    }
    return commandLine.getArgList().stream().collect(collector);
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
      return (optionValues != null ? Arrays.stream(optionValues) : Stream.<String>empty())
          .map(optionValue -> convertOptionValue(cliOption, optionAnnotation, field, optionValue))
          .collect(collector);
    }
    String optionValue = commandLine.getOptionValue(cliOption);
    if (optionValue == null && (cliOption.getType().equals(boolean.class) || cliOption.getType().equals(Boolean.class))) {
      optionValue = String.valueOf(commandLine.hasOption(cliOption.getOpt()));
      cliOption.setConverter(Boolean::parseBoolean);
    }
    if (optionValue == null) {
      return null;
    }
    return convertOptionValue(cliOption, optionAnnotation, field, optionValue);
  }

  private Object convertOptionValue(org.apache.commons.cli.Option cliOption,
                                    Option optionAnnotation, Field field, String  optionValue) {
    try {
      return cliOption.getConverter().apply(optionValue);
    } catch (NumberFormatException e) {
      throw new OptionParserException(
          "Invalid option %s: invalid number".formatted(getOptionDisplayedName(optionAnnotation, field)), e);
    } catch (Throwable e) {
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

  static String getOptionDisplayedName(Option option, Field field) {
    if (!option.longName().isEmpty()) {
      return option.longName();
    }
    if (!option.shortName().isEmpty()) {
      return option.shortName();
    }
    return field.getName();
  }
}
