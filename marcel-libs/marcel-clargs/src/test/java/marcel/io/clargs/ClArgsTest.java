package marcel.io.clargs;

import marcel.util.primitives.collections.lists.FloatArrayList;
import marcel.util.primitives.collections.lists.FloatList;
import marcel.util.primitives.collections.lists.LongArrayList;
import marcel.util.primitives.collections.lists.LongList;
import org.junit.jupiter.api.Test;

import java.io.File;
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
      @Option(longName = "unspecified", shortName = "u", optional = true)
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
      @Option(shortName = "m", numberOfArguments = "*")
      private List<String> multipleOptions;
    }
    MyScript script = new MyScript();

    clArgs.parseFromInstance(script, new String[]{
        "-m", "one", "-m", "2", "-m", "three"
    });
    assertEquals(List.of("one", "2", "three"), script.multipleOptions);
  }

  @Test
  public void testMultipleFloatOptions() {
    class MyScript {
      @Option(shortName = "m", numberOfArguments = "*")
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
  public void testNotEnoughOptions() {
    class MyScript {
      @Option(shortName = "m", numberOfArguments = "3+")
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
