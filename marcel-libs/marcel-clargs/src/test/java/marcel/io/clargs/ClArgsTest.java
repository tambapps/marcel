package marcel.io.clargs;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClArgsTest {

  private final ClArgs clArgs = new ClArgs();

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
