package marcel.io.clargs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClArgsBuilderTest {

  private final ClArgsBuilder clArgsBuilder = new ClArgsBuilder();

  @Test
  public void testParse() {
    CliScript script = new CliScript();

    clArgsBuilder.parseFromInstance(script, new String[]{"-n", "10", "-s", "5", "--foo", "bar", "-h"});
    assertEquals(10, script.getN());
    assertEquals(Integer.valueOf(5), script.getSize());
    assertEquals("bar", script.getFoo());
    assertTrue(script.isHelp());
    assertEquals("default", script.getUnspecified());
  }
}
