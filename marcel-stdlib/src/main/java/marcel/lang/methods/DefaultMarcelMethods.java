package marcel.lang.methods;

import marcel.lang.IntRange;
import marcel.lang.primitives.collections.lists.CharacterArrayList;
import marcel.lang.primitives.collections.lists.CharacterList;
import marcel.lang.primitives.collections.lists.DoubleArrayList;
import marcel.lang.primitives.collections.lists.DoubleList;
import marcel.lang.primitives.collections.lists.FloatArrayList;
import marcel.lang.primitives.collections.lists.FloatList;
import marcel.lang.primitives.collections.lists.IntArrayList;
import marcel.lang.primitives.collections.lists.IntList;
import marcel.lang.primitives.collections.lists.LongArrayList;
import marcel.lang.primitives.collections.lists.LongList;
import marcel.lang.primitives.collections.sets.DoubleSet;
import marcel.lang.primitives.collections.sets.FloatSet;
import marcel.lang.primitives.collections.sets.IntSet;
import marcel.lang.primitives.collections.sets.LongSet;
import marcel.lang.primitives.iterators.IntIterator;
import marcel.lang.util.function.ToFloatFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "deprecation"})
public final class DefaultMarcelMethods {

  /**
   * Returns all matched groups
   *
   * @param self the matcher
   * @return all matched groups
   */
  public static List<String> groups(Matcher self) {
    if (!self.find()) return Collections.emptyList();
    int count = self.groupCount();
    List<String> groups = new ArrayList<>(count);
    for (int i = 0; i <= count; i++) {
      groups.add(self.group(i));
    }
    return groups;
  }

  /**
   * Returns all defined matched groups
   *
   * @param self the matcher
   * @return all matched groups
   */
  public static List<String> definedGroups(Matcher self) {
    if (!self.find()) return Collections.emptyList();
    int count = self.groupCount();
    List<String> groups = new ArrayList<>(count);
    for (int i = 1; i <= count; i++) {
      groups.add(self.group(i));
    }
    return groups;
  }

  /**
   * Find the first element matching the given predicate
   *
   * @param self      the collection
   * @param predicate the predicate
   * @param <T>       the Collection elements type
   * @return the first element matching the given predicate or null
   */
  public static <T> T find(Collection<T> self, Predicate<T> predicate) {
    for (T e : self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  /**
   * Converts a list into a set
   *
   * @param self the list
   * @param <T>  the type of the list
   * @return a set
   */
  public static <T> Set<T> toSet(List<T> self) {
    Iterator<T> iterator = self.iterator();
    Set<T> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }

  /**
   * Map the list into an IntList
   *
   * @param list    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return an IntList
   */
  public static <T> IntList mapToInt(List<T> list, ToIntFunction<T> lambda1) {
    IntList intList = new IntArrayList(list.size());
    for (int i = 0; i < list.size(); i++) {
      intList.add(lambda1.applyAsInt(list.get(i)));
    }
    return intList;
  }

  /**
   * Map the list into a LongList
   *
   * @param list    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return a LongList
   */
  public static <T> LongList mapToLong(List<T> list, ToLongFunction<T> lambda1) {
    LongList longList = new LongArrayList(list.size());
    for (int i = 0; i < list.size(); i++) {
      longList.add(lambda1.applyAsLong(list.get(i)));
    }
    return longList;
  }

  /**
   * Map the list into a FloatList
   *
   * @param list    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return a FloatList
   */
  public static <T> FloatList mapToFloat(List<T> list, ToFloatFunction<T> lambda1) {
    FloatList floatList = new FloatArrayList(list.size());
    for (int i = 0; i < list.size(); i++) {
      floatList.add(lambda1.applyAsFloat(list.get(i)));
    }
    return floatList;
  }

  /**
   * Map the list into a DoubleList
   *
   * @param list    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return a DoubleList
   */
  public static <T> DoubleList mapToDouble(List<T> list, ToDoubleFunction<T> lambda1) {
    DoubleList floatList = new DoubleArrayList(list.size());
    for (int i = 0; i < list.size(); i++) {
      floatList.add(lambda1.applyAsDouble(list.get(i)));
    }
    return floatList;
  }

  // getLength

  /**
   * Returns the size of the Collection
   *
   * @param self the collection
   * @return the size of the collection
   */
  public static int getLength(Collection<?> self) {
    return self.size();
  }

  /**
   * Joins the elements of the collection to a string
   *
   * @param self      the collection
   * @param separator the separator
   * @return a string
   */
  public static String join(Collection<?> self, String separator) {
    return self.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  /**
   * Sort the list using the given keyExtractor as the comparator
   *
   * @param self         the list to sort
   * @param keyExtractor the key extractor
   * @param <T>          the type of the list
   * @param <U>          the comparable type
   */
  public static <T, U extends Comparable<U>> void sort(List<T> self, Function<T, U> keyExtractor) {
    self.sort(Comparator.comparing(keyExtractor));
  }

  /**
   * Reverse a list
   *
   * @param list the list
   */
  public static void reverse(List<?> list) {
    Collections.reverse(list);
  }

  /**
   * Add an element to the list
   *
   * @param self  the list
   * @param value the value to add
   * @param <T>   the type of the list
   * @return whether the value was added or not
   */
  public static <T> boolean leftShift(Collection<T> self, T value) {
    return self.add(value);
  }

  /**
   * Add an element to the list
   *
   * @param self  the list
   * @param value the value to add
   * @return whether the value was added or not
   */
  public static boolean leftShift(IntList self, int value) {
    return self.add(value);
  }

  /**
   * Add an element to the list
   *
   * @param self  the list
   * @param value the value to add
   * @return whether the value was added or not
   */
  public static boolean leftShift(LongList self, long value) {
    return self.add(value);
  }

  /**
   * Add an element to the list
   *
   * @param self  the list
   * @param value the value to add
   * @return whether the value was added or not
   */
  public static boolean leftShift(FloatList self, float value) {
    return self.add(value);
  }

  /**
   * Add an element to the list
   *
   * @param self  the list
   * @param value the value to add
   * @return whether the value was added or not
   */
  public static boolean leftShift(DoubleList self, double value) {
    return self.add(value);
  }

  /**
   * Add an element to the set
   *
   * @param self  the set
   * @param value the value to add
   * @return whether the value was added or not
   */
  public static boolean leftShift(IntSet self, int value) {
    return self.add(value);
  }

  /**
   * Add an element to the set
   *
   * @param self  the set
   * @param value the value to add
   * @return whether the value was added or not
   */
  public static boolean leftShift(LongSet self, long value) {
    return self.add(value);
  }

  /**
   * Add an element to the list
   *
   * @param self  the list
   * @param value the value to add
   * @return whether the value was added or not
   */
  public static boolean leftShift(FloatSet self, float value) {
    return self.add(value);
  }

  /**
   * Add an element to the list
   *
   * @param self  the list
   * @param value the value to add
   * @return whether the value was added or not
   */
  public static boolean leftShift(DoubleSet self, double value) {
    return self.add(value);
  }

  // last

  /**
   * Get the last element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @param <T>  the type of the list
   * @return the last element of the list
   */
  public static <T> T getLast(List<T> self) {
    return self.get(self.size() - 1);
  }

  /**
   * Get the last element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @return the last element of the list
   */
  public static int getLast(IntList self) {
    return self.getInt(self.size() - 1);
  }

  /**
   * Get the last element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @return the last element of the list
   */
  public static long getLast(LongList self) {
    return self.getLong(self.size() - 1);
  }

  /**
   * Get the last element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @return the last element of the list
   */
  public static float getLast(FloatList self) {
    return self.getFloat(self.size() - 1);
  }

  /**
   * Get the last element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @return the last element of the list
   */
  public static double getLast(DoubleList self) {
    return self.getDouble(self.size() - 1);
  }

  // lastOrNull

  /**
   * Get the last element of the list or null if the list is empty
   *
   * @param self the list
   * @param <T>  the type of the list
   * @return the last element of the list
   */
  public static <T> T getLastOrNull(List<T> self) {
    if (self.isEmpty()) return null;
    return self.get(self.size() - 1);
  }


  // first

  /**
   * Get the first element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @param <T>  the type of the list
   * @return the first element of the list
   */
  public static <T> T getFirst(List<T> self) {
    return self.get(self.size() - 1);
  }

  /**
   * Get the first element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @return the first element of the list
   */
  public static int getFirst(IntList self) {
    return self.getInt(self.size() - 1);
  }

  /**
   * Get the first element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @return the first element of the list
   */
  public static long getFirst(LongList self) {
    return self.getLong(self.size() - 1);
  }

  /**
   * Get the first element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @return the first element of the list
   */
  public static float getFirst(FloatList self) {
    return self.getFloat(self.size() - 1);
  }

  /**
   * Get the first element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @return the first element of the list
   */
  public static double getFirst(DoubleList self) {
    return self.getDouble(self.size() - 1);
  }

  // setLast

  /**
   * Sets the last element of the list. This method wil throw an exception if the list is empty
   *
   * @param self  the list
   * @param value the value to set
   */
  public static void setLast(IntList self, int value) {
    self.set(self.size() - 1, value);
  }


  /**
   * Sets the last element of the list. This method wil throw an exception if the list is empty
   *
   * @param self  the list
   * @param value the value to set
   */
  public static void setLast(LongList self, long value) {
    self.set(self.size() - 1, value);
  }


  /**
   * Sets the last element of the list. This method wil throw an exception if the list is empty
   *
   * @param self  the list
   * @param value the value to set
   */
  public static void setLast(FloatList self, float value) {
    self.set(self.size() - 1, value);
  }


  /**
   * Sets the last element of the list. This method wil throw an exception if the list is empty
   *
   * @param self  the list
   * @param value the value to set
   */
  public static void setLast(DoubleList self, double value) {
    self.set(self.size() - 1, value);
  }


  /**
   * Sets the last element of the list. This method wil throw an exception if the list is empty
   *
   * @param self  the list
   * @param value the value to set
   * @param <T>   the type of the list
   */
  public static <T> void setLast(List<T> self, T value) {
    self.set(self.size() - 1, value);
  }

  // getAt

  /**
   * Get the element at the specified index
   *
   * @param self  the list
   * @param index the index
   * @param <T>   the type of the list
   * @return the element at the specified index
   */
  public static <T> T getAt(List<T> self, int index) {
    return self.get(index);
  }

  /**
   * Get the element at the specified index
   *
   * @param self  the list
   * @param index the index
   * @return the element at the specified index
   */
  public static int getAt(IntList self, int index) {
    return self.getInt(index);
  }

  /**
   * Get the element at the specified index
   *
   * @param self  the list
   * @param index the index
   * @return the element at the specified index
   */
  public static long getAt(LongList self, int index) {
    return self.getLong(index);
  }

  /**
   * Get the element at the specified index
   *
   * @param self  the list
   * @param index the index
   * @return the element at the specified index
   */
  public static float getAt(FloatList self, int index) {
    return self.getFloat(index);
  }

  /**
   * Get the element at the specified index
   *
   * @param self  the list
   * @param index the index
   * @return the element at the specified index
   */
  public static double getAt(DoubleList self, int index) {
    return self.getDouble(index);
  }


  // getAt range

  /**
   * Get the elements at the specified indexes from the range. The order of elements returned respects the order of the range
   *
   * @param self  the list
   * @param range the range
   * @param <T>   the type of the list
   * @return the elements at the specified indexes from the ranges
   */
  public static <T> List<T> getAt(List<T> self, IntRange range) {
    List<T> subList = new ArrayList<>();
    IntIterator iterator = range.iterator();
    while (iterator.hasNext()) subList.add(self.get(iterator.nextInt()));
    return subList;
  }

  /**
   * Get the elements at the specified indexes from the range. The order of elements returned respects the order of the range
   *
   * @param self  the list
   * @param range the range
   * @return the elements at the specified indexes from the ranges
   */
  public static IntList getAt(IntList self, IntRange range) {
    IntList subList = new IntArrayList();
    IntIterator iterator = range.iterator();
    while (iterator.hasNext()) subList.add(self.get(iterator.nextInt()));
    return subList;
  }

  /**
   * Get the elements at the specified indexes from the range. The order of elements returned respects the order of the range
   *
   * @param self  the list
   * @param range the range
   * @return the elements at the specified indexes from the ranges
   */
  public static LongList getAt(LongList self, IntRange range) {
    LongList subList = new LongArrayList();
    IntIterator iterator = range.iterator();
    while (iterator.hasNext()) subList.add(self.get(iterator.nextInt()));
    return subList;
  }

  /**
   * Get the elements at the specified indexes from the range. The order of elements returned respects the order of the range
   *
   * @param self  the list
   * @param range the range
   * @return the elements at the specified indexes from the ranges
   */
  public static FloatList getAt(FloatList self, IntRange range) {
    FloatList subList = new FloatArrayList();
    IntIterator iterator = range.iterator();
    while (iterator.hasNext()) subList.add(self.get(iterator.nextInt()));
    return subList;
  }

  /**
   * Get the elements at the specified indexes from the range. The order of elements returned respects the order of the range
   *
   * @param self  the list
   * @param range the range
   * @return the elements at the specified indexes from the ranges
   */
  public static DoubleList getAt(DoubleList self, IntRange range) {
    DoubleList subList = new DoubleArrayList();
    IntIterator iterator = range.iterator();
    while (iterator.hasNext()) subList.add(self.get(iterator.nextInt()));
    return subList;
  }

  /**
   * Get the elements at the specified indexes from the range. The order of elements returned respects the order of the range
   *
   * @param self  the list
   * @param range the range
   * @return the elements at the specified indexes from the ranges
   */
  public static CharacterList getAt(CharacterList self, IntRange range) {
    CharacterList subList = new CharacterArrayList();
    IntIterator iterator = range.iterator();
    while (iterator.hasNext()) subList.add(self.get(iterator.nextInt()));
    return subList;
  }

  // getAtSafe

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the list
   * @param index the index
   * @return the element at the specified index
   */
  public static Character getAtSafe(String self, int index) {
    return index >= 0 && index < self.length() ? self.charAt(index) : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the list
   * @param index the index
   * @return the element at the specified index
   */
  public static <T> T getAtSafe(List<T> self, int index) {
    return index >= 0 && index < self.size() ? self.get(index) : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the list
   * @param index the index
   * @return the element at the specified index
   */
  public static Integer getAtSafe(IntList self, int index) {
    return index >= 0 && index < self.size() ? self.get(index) : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the list
   * @param index the index
   * @return the element at the specified index
   */
  public static Long getAtSafe(LongList self, int index) {
    return index >= 0 && index < self.size() ? self.get(index) : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the list
   * @param index the index
   * @return the element at the specified index
   */
  public static Float getAtSafe(FloatList self, int index) {
    return index >= 0 && index < self.size() ? self.get(index) : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the list
   * @param index the index
   * @return the element at the specified index
   */
  public static Double getAtSafe(DoubleList self, int index) {
    return index >= 0 && index < self.size() ? self.get(index) : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the array
   * @param index the index
   * @return the element at the specified index
   */
  public static <T> T getAtSafe(T[] self, int index) {
    return index >= 0 && index < self.length ? self[index] : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the array
   * @param index the index
   * @return the element at the specified index
   */
  public static Integer getAtSafe(int[] self, int index) {
    return index >= 0 && index < self.length ? self[index] : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the array
   * @param index the index
   * @return the element at the specified index
   */
  public static Long getAtSafe(long[] self, int index) {
    return index >= 0 && index < self.length ? self[index] : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the array
   * @param index the index
   * @return the element at the specified index
   */
  public static Float getAtSafe(float[] self, int index) {
    return index >= 0 && index < self.length ? self[index] : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the array
   * @param index the index
   * @return the element at the specified index
   */
  public static Double getAtSafe(double[] self, int index) {
    return index >= 0 && index < self.length ? self[index] : null;
  }

  // putAt

  /**
   * Set the element at the specified index
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   * @param <T>   the type of the list
   */
  public static <T> void putAt(List<T> self, int index, T value) {
    self.set(index, value);
  }

  /**
   * Set the element at the specified index
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAt(IntList self, int index, int value) {
    self.set(index, value);
  }

  /**
   * Set the element at the specified index
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAt(LongList self, int index, long value) {
    self.set(index, value);
  }

  /**
   * Set the element at the specified index
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAt(FloatList self, int index, float value) {
    self.set(index, value);
  }

  /**
   * Set the element at the specified index
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAt(DoubleList self, int index, double value) {
    self.set(index, value);
  }

}
