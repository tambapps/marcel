package marcel.lang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntRangeTest {

  @Test
  public void testIncr() {
    IntRange range = IntRanges.ofIncr(0, 10);
    assertEquals(0, range.getFrom());
    assertEquals(10, range.getTo());
    assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), range.toList());

    range = IntRanges.ofIncrToExclusive(0, 10);
    assertEquals(0, range.getFrom());
    assertEquals(9, range.getTo());
    assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), range.toList());

    range = IntRanges.ofIncrFromExclusive(0, 10);
    assertEquals(1, range.getFrom());
    assertEquals(10, range.getTo());
    assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), range.toList());

    range = IntRanges.ofIncrExclusive(0, 10);
    assertEquals(1, range.getFrom());
    assertEquals(9, range.getTo());
    assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9), range.toList());
  }

  @Test
  public void testDecr() {
    IntRange range = IntRanges.ofDecr(10, 0);
    assertEquals(10, range.getFrom());
    assertEquals(0, range.getTo());
    assertEquals(Arrays.asList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0), range.toList());

    range = IntRanges.ofDecrToExclusive(10, 0);
    assertEquals(10, range.getFrom());
    assertEquals(1, range.getTo());
    assertEquals(Arrays.asList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1), range.toList());

    range = IntRanges.ofDecrFromExclusive(10, 0);
    assertEquals(9, range.getFrom());
    assertEquals(0, range.getTo());
    assertEquals(Arrays.asList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0), range.toList());

    range = IntRanges.ofDecrExclusive(10, 0);
    assertEquals(9, range.getFrom());
    assertEquals(1, range.getTo());
    assertEquals(Arrays.asList(9, 8, 7, 6, 5, 4, 3, 2, 1), range.toList());
  }


  @MethodSource
  @ParameterizedTest
  public void testContains(int from, int to, List<Integer> tests) {
    IntRange intRange = IntRanges.of(from, to);
    for (int i : tests) {
      assertTrue(intRange.contains(i));
    }
  }

  private static Stream<Arguments> testContains() {
    return Stream.of(
        Arguments.of(1, 5, List.of(1, 2, 3, 4, 5)),
        Arguments.of(5, 1, List.of(1, 2, 3, 4, 5))
    );
  }

  @MethodSource
  @ParameterizedTest
  public void testNotContains(int from, int to, List<Integer> tests) {
    IntRange intRange = IntRanges.of(from, to);
    for (int i : tests) {
      assertFalse(intRange.contains(i));
    }
  }

  private static Stream<Arguments> testNotContains() {
    return Stream.of(
        Arguments.of(1, 5, List.of(0, -1, 6, 7, 8)),
        Arguments.of(5, 1, List.of(0, -1, 6, 7, 8))
    );
  }
}
