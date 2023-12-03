package marcel.lang.primitives.collections.lists;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CharacterListTest {


  @Test
  void testSublist() {
    CharacterList list = new CharacterArrayList("Hello world");

    assertEquals("Hello", list.subList(0, 5).toString());
    assertEquals("world", list.subList(6, 11).toString());
  }
}
