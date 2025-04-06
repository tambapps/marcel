package marcel.io.clargs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArityTest {

  @MethodSource
  @ParameterizedTest
  public void test(String a, Class<?> clazz, List<Integer> oks, List<Integer> kos) {
    OptionArity arity = OptionArity.of(a);
    assertEquals(clazz, arity.getClass(), "Expected arity '%s' to be of class %s".formatted(a, clazz.getSimpleName()));
    for (int i : oks) {
      assertTrue(arity.respects(i), "Arity " + i + " should be respected");
    }
    for (int i : kos) {
      assertFalse(arity.respects(i), "Arity " + i + " should not be respected");
    }
  }
  private static Stream<Arguments> test() {
    return Stream.of(
        // test constant
        Arguments.of("125", OptionArity.ConstantArity.class, List.of(125), List.of(124, 126, 127)),
        // test between
        Arguments.of("5..8", OptionArity.Between.class, List.of(5, 6, 7, 8), List.of(1, 2, 3 ,4, 9, 10, 11)),
        // test at least
        Arguments.of("4..*", OptionArity.AtLeast.class, List.of(4, 5, 6, 7, 8), List.of(1, 2, 3)),
        Arguments.of("4+", OptionArity.AtLeast.class, List.of(4, 5, 6, 7, 8), List.of(1, 2, 3)),
        // test at most
        Arguments.of("*..8", OptionArity.AtMost.class, List.of(5, 6, 7, 8), List.of(9, 10, 11)),
        // any
        Arguments.of("*", OptionArity.Any.class, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ,11), List.of())
    );
  }

}
