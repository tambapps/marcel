package marcel.lang.extensions;

import marcel.lang.IntRange;
import marcel.lang.compile.ExtensionClass;
import marcel.util.primitives.collections.lists.CharArrayList;
import marcel.util.primitives.collections.lists.CharList;
import marcel.util.primitives.collections.sets.CharOpenHashSet;
import marcel.util.primitives.collections.sets.CharSet;
import marcel.util.primitives.iterators.IntIterator;
import marcel.util.function.CharPredicate;

import java.util.NoSuchElementException;

// TODO convert to marcel
@ExtensionClass
@SuppressWarnings({"unused"})
public class StringMarcelMethods {

  public static String plus(String $self, String other) {
    return $self + other;
  }

  public static String plus(String $self, Object other) {
    return $self + other;
  }

  public static char getAt(CharSequence $self, int i) {
    return $self.charAt(i);
  }

  public static String getAt(CharSequence $self, IntRange r) {
    StringBuilder builder = new StringBuilder();
    r.forEach((int i) -> builder.append($self.charAt(i)));
    return builder.toString();
  }

  public static String getAtSafe(CharSequence $self, IntRange r) {
    StringBuilder builder = new StringBuilder();
    r.forEach((int i) -> {
      if (i >= 0 && i < $self.length()) {
        builder.append($self.charAt(i));
      }
    });
    return builder.toString();
  }

  public static CharList toList(String $self) {
    return CharArrayList.wrap($self.toCharArray());
  }

  public static CharSet toSet(String $self) {
    return new CharOpenHashSet($self.toCharArray());
  }

  public static int toInt(String $self) {
    return Integer.parseInt($self);
  }
  public static long toLong(String $self) {
    return Long.parseLong($self);
  }
  public static float toFloat(String $self) {
    return Float.parseFloat($self);
  }
  public static double toDouble(String $self) {
    return Double.parseDouble($self);
  }

  public static Character find(CharSequence $self, CharPredicate predicate)  {
    char c;
    for (int i = 0; i < $self.length(); i++) {
      c = $self.charAt(i);
      if (predicate.test(c)) return c;
    }
    return null;
  }

  public static char findChar(CharSequence $self, CharPredicate predicate)  {
    char c;
    for (int i = 0; i < $self.length(); i++) {
      c = $self.charAt(i);
      if (predicate.test(c)) return c;
    }
    throw new NoSuchElementException();
  }

  public static Character findLast(CharSequence $self, CharPredicate predicate)  {
    char c;
    for (int i = $self.length() - 1; i >= 0; i--) {
      c = $self.charAt(i);
      if (predicate.test(c)) return c;
    }
    return null;
  }

  public static char findLastChar(CharSequence $self, CharPredicate predicate)  {
    char c;
    for (int i = $self.length() - 1; i >= 0; i--) {
      c = $self.charAt(i);
      if (predicate.test(c)) return c;
    }
    throw new NoSuchElementException();
  }

  public static CharList getAt(String $self, IntRange range) {
    CharList characters = new CharArrayList();
    IntIterator iterator = range.iterator();
    while (iterator.hasNext()) characters.add($self.charAt(iterator.nextInt()));
    return characters;
  }

  public static boolean contains(CharSequence $self, char c) {
    for (int i = 0; i < $self.length(); i++) {
      if ($self.charAt(i) == c) return true;
    }
    return false;
  }

  public static int lastIndexOf(String $self, char c) {
    return $self.lastIndexOf(c);
  }

  public static int indexOf(CharSequence $self, CharPredicate predicate) {
    for (int i = 0; i < $self.length(); i++) {
      if (predicate.test($self.charAt(i))) return i;
    }
    return -1;
  }

  public static int lastIndexOf(CharSequence $self, CharPredicate predicate) {
    for (int i = $self.length() - 1; i >= 0; i--) {
      if (predicate.test($self.charAt(i))) return i;
    }
    return -1;
  }

  public static String reversed(CharSequence $self) {
    return new StringBuilder($self).reverse().toString();
  }

  public static int count(CharSequence $self, CharPredicate predicate) {
    int count = 0;
    for (int i = 0; i < $self.length(); i++) {
      if (predicate.test($self.charAt(i))) count++;
    }
    return count;
  }

  public static boolean all(CharSequence $self, CharPredicate predicate) {
    for (int i = 0; i < $self.length(); i++) {
      if (!predicate.test($self.charAt(i))) return false;
    }
    return true;
  }

  public static boolean none(CharSequence $self, CharPredicate predicate) {
    for (int i = 0; i < $self.length(); i++) {
      if (predicate.test($self.charAt(i))) return false;
    }
    return true;
  }

  public static boolean any(CharSequence $self, CharPredicate predicate) {
    for (int i = 0; i < $self.length(); i++) {
      if (predicate.test($self.charAt(i))) return true;
    }
    return false;
  }

  public static String fmt(String $self, Object... args) {
    return String.format($self, args);
  }
}
