package marcel.lang.primitives.collections.lists;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CharListTest {


  @Test
  void testSublist() {
    CharList list = new CharArrayList("Hello world");

    assertEquals("Hello", list.subList(0, 5).toString());
    assertEquals("world", list.subList(6, 11).toString());
  }
}
