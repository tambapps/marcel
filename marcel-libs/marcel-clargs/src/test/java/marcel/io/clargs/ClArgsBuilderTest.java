package marcel.io.clargs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClArgsBuilderTest {

  private final ClArgsBuilder clArgsBuilder = new ClArgsBuilder();

  @Test
  public void testParse() {
    CliScript script = new CliScript();

    clArgsBuilder.parseFromInstance(script, new String[]{"-n", "10"});
    assertEquals(10, script.getN());
  }
}
