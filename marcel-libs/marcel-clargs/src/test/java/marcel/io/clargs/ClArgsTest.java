package marcel.io.clargs;

import marcel.lang.lambda.Lambda1;
import marcel.util.primitives.collections.lists.FloatArrayList;
import marcel.util.primitives.collections.lists.FloatList;
import marcel.util.primitives.collections.lists.IntArrayList;
import marcel.util.primitives.collections.lists.IntList;
import marcel.util.primitives.collections.lists.LongArrayList;
import marcel.util.primitives.collections.lists.LongList;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClArgsTest {

  private final ClArgs clArgs = new ClArgs();

  @Test
  public void testInt() {
    class MyScript {
      @Option
      private int n;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-n", "10",
    });
    assertEquals(10, script.n);
  }

  @Test
  public void testString() {
    class MyScript {
      @Option
      private String s;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-s", "string",
    });
    assertEquals("string", script.s);
  }

  @Test
  public void testFile() {
    class MyScript {
      @Option
      private File path;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-path", "/some/path",
    });
    assertEquals(new File("/some/path"), script.path);
  }

  @Test
  public void testJoinedArgs() {
    class MyScript {
      @Arguments
      private String joinedArgs;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "arg1", "arg2", "arg3"
    });
    assertEquals("arg1 arg2 arg3", script.joinedArgs);
  }

  @Test
  public void testHelpOption() {
    class MyScript {
      @HelpOption
      private boolean help;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{});
    assertFalse(script.help);

    clArgs.parseFromInstance(script, new String[]{
        "-h"
    });
    assertTrue(script.help);
    script.help = false;

    clArgs.parseFromInstance(script, new String[]{
        "--help"
    });
    assertTrue(script.help);
  }

  @Test
  public void testArgs() {
    class MyScript {
      @Arguments
      private List<String> joinedArgs;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "arg1", "arg2", "arg3"
    });
    assertEquals(List.of("arg1", "arg2", "arg3"), script.joinedArgs);
  }

  @Test
  public void testLongArgs() {
    class MyScript {
      @Arguments
      private LongList args;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "123", "456", "789"
    });
    assertEquals(LongArrayList.of(123L, 456L, 789L), script.args);
  }

  @Test
  public void testFlag() {
    class MyScript {
      @Option(longName = "help", shortName = "h")
      private boolean help;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{});
    assertFalse(script.help);

    clArgs.parseFromInstance(script, new String[]{
        "-h",
    });
    assertTrue(script.help);
  }

  @Test
  public void testDefaultValue() {
    class MyScript {
      @Option(longName = "unspecified", shortName = "u")
      private String unspecified = "default";

    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{});
    assertEquals("default", script.unspecified);

    clArgs.parseFromInstance(script, new String[]{"-u", "specified"});
    assertEquals("specified", script.unspecified);
  }

  @Test
  public void testNames() {
    class MyScript {
      @Option(longName = "foo", shortName = "f")
      private String foo;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-f", "bar",
    });
    assertEquals("bar", script.foo);

    clArgs.parseFromInstance(script, new String[]{
        "--foo", "zoo",
    });
    assertEquals("zoo", script.foo);
  }

  @Test
  public void testMultipleOptions() {
    class MyScript {
      @Option(shortName = "m", arity = "*")
      private List<String> multipleOptions;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-m", "one", "-m", "2", "-m", "three"
    });
    assertEquals(List.of("one", "2", "three"), script.multipleOptions);
  }

  @Test
  public void testElementsType() {
    class MyScript {
      @Option(shortName = "n", arity = "*", elementsType = BigDecimal.class)
      private List<BigDecimal> numbers;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-n", "1", "-n", "2", "-n", "2344444"
    });
    assertEquals(List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(2344444)), script.numbers);
  }

  @Test
  public void testArgumentsElementsType() {
    class MyScript {
      @Arguments(elementsType = BigDecimal.class)
      private List<BigDecimal> numbers;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "1", "2", "2344444"
    });
    assertEquals(List.of(BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(2344444)), script.numbers);
  }

  @Test
  public void testArgsSeparatorOptions() {
    class MyScript {
      @Option(shortName = "m", valueSeparator = ",", arity = "*")
      private List<String> multipleOptions;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-m", "one,2,three"
    });
    assertEquals(List.of("one", "2", "three"), script.multipleOptions);
  }

  @Test
  public void testMultipleFloatOptions() {
    class MyScript {
      @Option(shortName = "m", arity = "*")
      private FloatList multipleOptions;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-m", "1", "-m", "2.2", "-m", "3.33"
    });
    assertEquals(FloatArrayList.of(1f, 2.2f, 3.33f
    ), script.multipleOptions);
  }

  @Test
  public void testArityAtLeast() {
    class MyScript {
      @Option(shortName = "m", arity = "3+")
      private FloatList multipleOptions;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-m", "1", "-m", "2.2", "-m", "3.33"
    });
    assertEquals(FloatArrayList.of(1f, 2.2f, 3.33f
    ), script.multipleOptions);

    OptionParserException exception = assertThrows(OptionParserException.class, () -> clArgs.parseFromInstance(new MyScript(), new String[]{
        "-m", "1", "-m", "2.2"
    }));
    assertTrue(exception.getMessage().contains("Expected at least 3 value(s) for option m"));
  }

  @Test
  public void testArityAtMost() {
    class MyScript {
      @Option(shortName = "m", arity = "*..3")
      private FloatList multipleOptions;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-m", "1", "-m", "2.2", "-m", "3.33"
    });
    assertEquals(FloatArrayList.of(1f, 2.2f, 3.33f
    ), script.multipleOptions);

    OptionParserException exception = assertThrows(OptionParserException.class, () -> clArgs.parseFromInstance(new MyScript(), new String[]{
        "-m", "1", "-m", "2.2", "-m", "3.33", "-m", "4444"
    }));
    assertTrue(exception.getMessage().contains("Expected at most 3 value(s) for option m"));
  }

  static class OptValidator implements Lambda1<Integer, Object> {

    @Override
    public Object invoke(Integer arg0) {
      if (arg0 < 5 || arg0 > 8) {
        throw new IllegalArgumentException("arg0 must be between 5 and 8");
      }
      return null;
    }
  }

  @Test
  public void testValidator() {
    class MyScript {
      @Option(shortName = "o", validator = OptValidator.class)
      private int opt;
    }

    OptionParserException exception = assertThrows(OptionParserException.class, () -> clArgs.parseFromInstance(new MyScript(), new String[]{
        "-o", "1"
    }));
    assertTrue(exception.getMessage().contains("Invalid option o"));

    MyScript script = new MyScript();
    clArgs.parseFromInstance(script, new String[]{
        "-o", "5"
    });
    assertEquals(5, script.opt);
  }

  static class PlusOneConverter implements Lambda1<String, Integer> {

    @Override
    public Integer invoke(String arg0) {
      return Integer.parseInt(arg0) + 1;
    }
  }

  @Test
  public void testConverter() {
    class MyScript {
      @Option(shortName = "o", converter = PlusOneConverter.class)
      private int opt;
    }
    MyScript script = new MyScript();
    clArgs.parseFromInstance(script, new String[]{
        "-o", "5"
    });
    assertEquals(6, script.opt);
  }

  @Test
  public void testArgsConverter() {
    class MyScript {
      @Arguments(converter = PlusOneConverter.class)
      private IntList args;
    }
    MyScript script = new MyScript();
    clArgs.parseFromInstance(script, new String[]{
        "1", "3", "5"
    });
    assertEquals(new IntArrayList(new int[] {2, 4, 6}), script.args);
  }

  @Test
  public void testEnum() {
    class MyScript {
      @Option(shortName = "m")
      private Month month;
    }
    MyScript script = new MyScript();
    clArgs.parseFromInstance(script, new String[]{
        "-m", "MARCH"
    });
    assertEquals(Month.MARCH, script.month);
  }

  @Test
  public void testNotEnoughOptions() {
    class MyScript {
      @Option(shortName = "m", arity = "3+")
      private List<String> multipleOptions;
    }
    MyScript script = new MyScript();

    OptionParserException exception = assertThrows(OptionParserException.class, () -> clArgs.parseFromInstance(script, new String[]{
        "-m", "one", "-m", "2"
    }));
    assertTrue(exception.getMessage().contains("at least 3"));
  }

  @Test
  public void testParseFromInstance() {
    CliScript script = new CliScript();

    clArgs.parseFromInstance(script, new String[]{
        "-n", "10",
        "-s", "5",
        "--foo", "bar",
        "-h",
        "-path", "/some/path",
        "arg1", "arg2", "arg3"
    });
    assertEquals(10, script.getN());
    assertEquals(Integer.valueOf(5), script.getSize());
    assertEquals("bar", script.getFoo());
    assertTrue(script.isHelp());
    assertEquals("default", script.getUnspecified());
    assertEquals(new File("/some/path"), script.getPath());
    assertEquals(List.of("arg1", "arg2", "arg3"), script.getArgs());
    assertEquals("arg1 arg2 arg3", script.getJoinedArgs());
  }


  @Test
  public void testMissingRequiredOption() {
    CliScript script = new CliScript();
    OptionParserException exception = assertThrows(OptionParserException.class, () -> clArgs.parseFromInstance(script, new String[]{}));
    assertTrue(exception.getMessage().contains("is required"));
  }
}
