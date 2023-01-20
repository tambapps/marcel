package marcel.lang;

import it.unimi.dsi.fastutil.ints.IntLists;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

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


}
