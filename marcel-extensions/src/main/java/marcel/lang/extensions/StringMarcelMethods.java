package marcel.lang.extensions;

import marcel.lang.IntRange;
import marcel.lang.primitives.collections.lists.CharacterArrayList;
import marcel.lang.primitives.collections.lists.CharacterList;
import marcel.lang.primitives.iterators.CharacterIterator;
import marcel.lang.primitives.iterators.IntIterator;
import marcel.lang.util.function.CharacterPredicate;

import java.util.NoSuchElementException;

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

  public static Character find(CharSequence self, CharacterPredicate predicate)  {
    char c;
    for (int i = 0; i < self.length(); i++) {
      c = self.charAt(i);
      if (predicate.test(c)) return c;
    }
    return null;
  }

  // TODO add findLast and findLastPrimitive in collection and primitive collections
  public static Character findLast(CharSequence self, CharacterPredicate predicate)  {
    char c;
    for (int i = self.length() - 1; i >= 0; i--) {
      c = self.charAt(i);
      if (predicate.test(c)) return c;
    }
    return null;
  }

  public static char findCharacter(CharSequence self, CharacterPredicate predicate)  {
    char c;
    for (int i = 0; i < self.length(); i++) {
      c = self.charAt(i);
      if (predicate.test(c)) return c;
    }
    throw new NoSuchElementException();
  }

  public static char findLastCharacter(CharSequence self, CharacterPredicate predicate)  {
    char c;
    for (int i = self.length() - 1; i >= 0; i--) {
      c = self.charAt(i);
      if (predicate.test(c)) return c;
    }
    throw new NoSuchElementException();
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

  public static int lastIndexOf(String self, char c) {
    return self.lastIndexOf(c);
  }

  public static int indexOf(CharSequence self, CharacterPredicate predicate) {
    for (int i = 0; i < self.length(); i++) {
      if (predicate.test(self.charAt(i))) return i;
    }
    return -1;
  }

  public static int lastIndexOf(CharSequence self, CharacterPredicate predicate) {
    for (int i = self.length() - 1; i >= 0; i--) {
      if (predicate.test(self.charAt(i))) return i;
    }
    return -1;
  }

  public static String reversed(CharSequence self) {
    return new StringBuilder(self).reverse().toString();
  }
}
