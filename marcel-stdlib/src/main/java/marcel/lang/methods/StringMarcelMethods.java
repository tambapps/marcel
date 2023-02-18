package marcel.lang.methods;

import marcel.lang.IntRange;
import marcel.lang.primitives.collections.lists.CharacterArrayList;
import marcel.lang.primitives.collections.lists.CharacterList;
import marcel.lang.primitives.iterators.IntIterator;

@SuppressWarnings({"unused", "deprecation"})
public class StringMarcelMethods {

  public static char getAt(String self, int i) {
    return self.charAt(i);
  }

  public static CharacterList toCharList(String self) {
    return CharacterArrayList.wrap(self.toCharArray());
  }

  public static int toInt(String self) {
    return Integer.parseInt(self);
  }
  public static long toLong(String self) {
    return Long.parseLong(self);
  }
  public static float toFloat(String self) {
    return Float.parseFloat(self);
  }
  public static double toDouble(String self) {
    return Double.parseDouble(self);
  }


  public static CharacterList getAt(String self, IntRange range) {
    CharacterList characters = new CharacterArrayList();
    IntIterator iterator = range.iterator();
    while (iterator.hasNext()) characters.add(self.charAt(iterator.nextInt()));
    return characters;
  }

  public static int getLength(String self) {
    return self.length();
  }


  public static boolean contains(CharSequence self, char c) {
    for (int i = 0; i < self.length(); i++) {
      if (self.charAt(i) == c) return true;
    }
    return false;
  }
}
