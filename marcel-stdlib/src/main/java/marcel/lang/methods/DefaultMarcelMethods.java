package marcel.lang.methods;

import marcel.lang.IntRange;
import marcel.lang.primitives.collections.*;
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
import marcel.lang.primitives.collections.sets.CharacterOpenHashSet;
import marcel.lang.primitives.collections.sets.CharacterSet;
import marcel.lang.primitives.collections.sets.DoubleOpenHashSet;
import marcel.lang.primitives.collections.sets.DoubleSet;
import marcel.lang.primitives.collections.sets.FloatOpenHashSet;
import marcel.lang.primitives.collections.sets.FloatSet;
import marcel.lang.primitives.collections.sets.IntOpenHashSet;
import marcel.lang.primitives.collections.sets.IntSet;
import marcel.lang.primitives.collections.sets.LongOpenHashSet;
import marcel.lang.primitives.collections.sets.LongSet;
import marcel.lang.primitives.iterators.IntIterator;
import marcel.lang.util.MapWithDefault;
import marcel.lang.util.function.*;

import java.util.*;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
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

  public static <T> void swap(List l, int i, int j) {
    l.set(i, l.set(j, l.get(i)));
  }

  public static <T> void swap(T[] l, int i, int j) {
    T temp = l[i];
    l[i] = l[j];
    l[j] = temp;
  }

  public static void swap(int[] l, int i, int j) {
    int temp = l[i];
    l[i] = l[j];
    l[j] = temp;
  }

  public static void swap(long[] l, int i, int j) {
    long temp = l[i];
    l[i] = l[j];
    l[j] = temp;
  }

  public static void swap(float[] l, int i, int j) {
    float temp = l[i];
    l[i] = l[j];
    l[j] = temp;
  }

  public static void swap(double[] l, int i, int j) {
    double temp = l[i];
    l[i] = l[j];
    l[j] = temp;
  }

  public static void swap(char[] l, int i, int j) {
    char temp = l[i];
    l[i] = l[j];
    l[j] = temp;
  }

  public static <T> List<T> unique(Collection<T> self, Function<T, ?> keyExtractor) {
    Set<Object> set = new HashSet<>();
    List<T> list = new ArrayList<>();
    for (T o : self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static <T> Set<T> unique(Collection<T> self) {
    return new HashSet<>(self);
  }

  public static <T> Collection<T> unique(T[] self, Function<T, ?> keyExtractor) {
    Set<Object> set = new HashSet<>();
    List<T> list = new ArrayList<>();
    for (T o : self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static <T> Set<T> unique(T[] self) {
    return new HashSet<>(Arrays.asList(self));
  }

  public static IntCollection unique(int[] self, IntFunction<?> keyExtractor) {
    Set<Object> set = new HashSet<>();
    IntList list = new IntArrayList();
    for (int o : self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static IntSet unique(int[] self) {
    return new IntOpenHashSet(IntArrayList.wrap(self));
  }

  public static LongCollection unique(long[] self, LongFunction<?> keyExtractor) {
    Set<Object> set = new HashSet<>();
    LongList list = new LongArrayList();
    for (long o : self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static LongSet unique(long[] self) {
    return new LongOpenHashSet(LongArrayList.wrap(self));
  }

  public static FloatCollection unique(float[] self, FloatFunction<?> keyExtractor) {
    Set<Object> set = new HashSet<>();
    FloatList list = new FloatArrayList();
    for (float o : self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static FloatSet unique(float[] self) {
    return new FloatOpenHashSet(FloatArrayList.wrap(self));
  }

  public static DoubleCollection unique(double[] self, DoubleFunction<?> keyExtractor) {
    Set<Object> set = new HashSet<>();
    DoubleList list = new DoubleArrayList();
    for (double o : self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static DoubleSet unique(double[] self) {
    return new DoubleOpenHashSet(DoubleArrayList.wrap(self));
  }

  public static CharacterCollection unique(char[] self, CharacterFunction<?> keyExtractor) {
    Set<Object> set = new HashSet<>();
    CharacterList list = new CharacterArrayList();
    for (char o : self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static CharacterSet unique(char[] self) {
    return new CharacterOpenHashSet(CharacterArrayList.wrap(self));
  }

  public static <T> boolean replace(Collection<T> collection, T element, T replacement) {
    if (collection.remove(element)) {
      return collection.add(replacement);
    }
    throw new NoSuchElementException();
  }

  public static void shuffle(List<?> self) {
    Collections.shuffle(self);
  }

  public static void shuffle(List<?> self, Random rnd) {
    Collections.shuffle(self, rnd);
  }

  public static <T> void shuffle(T[] arr) {
    shuffle(arr, new Random());
  }
  public static <T> void shuffle(T[] arr, Random rnd) {
    for (int i=arr.length; i>1; i--)
      swap(arr, i-1, rnd.nextInt(i));
  }

  public static <T> void shuffle(int[] arr) {
    shuffle(arr, new Random());
  }

  public static <T> void shuffle(int[] arr, Random rnd) {
    for (int i=arr.length; i>1; i--)
      swap(arr, i-1, rnd.nextInt(i));
  }

  public static <T> void shuffle(long[] arr) {
    shuffle(arr, new Random());
  }

  public static <T> void shuffle(long[] arr, Random rnd) {
    for (int i=arr.length; i>1; i--)
      swap(arr, i-1, rnd.nextInt(i));
  }

  public static <T> void shuffle(float[] arr) {
    shuffle(arr, new Random());
  }

  public static <T> void shuffle(float[] arr, Random rnd) {
    for (int i=arr.length; i>1; i--)
      swap(arr, i-1, rnd.nextInt(i));
  }

  public static <T> void shuffle(double[] arr) {
    shuffle(arr, new Random());
  }

  public static <T> void shuffle(double[] arr, Random rnd) {
    for (int i=arr.length; i>1; i--)
      swap(arr, i-1, rnd.nextInt(i));
  }

  public static <T> void shuffle(char[] arr) {
    shuffle(arr, new Random());
  }

  public static <T> void shuffle(char[] arr, Random rnd) {
    for (int i=arr.length; i>1; i--)
      swap(arr, i-1, rnd.nextInt(i));
  }

  /**
   * Returns true if all the elements match the given predicate
   *
   * @param self      the collection
   * @param predicate the predicate
   * @param <T>       the Collection elements type
   * @return true if all the elements match the given predicate
   */
  public static <T> boolean all(Collection<T> self, Predicate<T> predicate) {
    for (T e : self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static <T> boolean all(T[] self, Predicate<T> predicate) {
    for (T e : self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean all(int[] self, IntPredicate predicate) {
    for (int e : self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean all(long[] self, LongPredicate predicate) {
    for (long e : self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean all(float[] self, FloatPredicate predicate) {
    for (float e : self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean all(double[] self, DoublePredicate predicate) {
    for (double e : self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean all(char[] self, CharacterPredicate predicate) {
    for (char e : self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  /**
   * Returns true if at least one element matches the given predicate
   *
   * @param self      the collection
   * @param predicate the predicate
   * @param <T>       the Collection elements type
   * @return true if all the elements match the given predicate
   */
  public static <T> boolean any(Collection<T> self, Predicate<T> predicate) {
    for (T e : self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static <T> boolean any(T[] self, Predicate<T> predicate) {
    for (T e : self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static boolean any(int[] self, IntPredicate predicate) {
    for (int e : self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static boolean any(long[] self, LongPredicate predicate) {
    for (long e : self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static boolean any(float[] self, FloatPredicate predicate) {
    for (float e : self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static boolean any(double[] self, DoublePredicate predicate) {
    for (double e : self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static boolean any(char[] self, CharacterPredicate predicate) {
    for (char e : self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  /**
   * Returns true if none of the elements match the given predicate
   *
   * @param self      the collection
   * @param predicate the predicate
   * @param <T>       the Collection elements type
   * @return true if none of the elements match the given predicate
   */
  public static <T> boolean none(Collection<T> self, Predicate<T> predicate) {
    for (T e : self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static <T> boolean none(T[] self, Predicate<T> predicate) {
    for (T e : self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean none(int[] self, IntPredicate predicate) {
    for (int e : self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean none(long[] self, LongPredicate predicate) {
    for (long e : self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean none(float[] self, FloatPredicate predicate) {
    for (float e : self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean none(double[] self, DoublePredicate predicate) {
    for (double e : self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean none(char[] self, CharacterPredicate predicate) {
    for (char e : self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static <T, U extends Comparable<? super U>> T max(Collection<T> self, Function<T, U> keyExtractor) {
    return self.stream()
        .max(Comparator.comparing(keyExtractor))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T, U extends Comparable<? super U>> T max(T[] self, Function<T, U> keyExtractor) {
    return Arrays.stream(self)
        .max(Comparator.comparing(keyExtractor))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T extends Comparable<? super T>> T max(Collection<T> self) {
    return self.stream()
        .max(Comparator.comparing(Function.identity()))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T extends Comparable<? super T>> T max(T[] self) {
    return Arrays.stream(self)
        .max(Comparator.comparing(Function.identity()))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T, U extends Comparable<? super U>> T min(Collection<T> self, Function<T, U> keyExtractor) {
    return self.stream()
        .min(Comparator.comparing(keyExtractor))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T, U extends Comparable<? super U>> T min(T[] self, Function<T, U> keyExtractor) {
    return Arrays.stream(self)
        .min(Comparator.comparing(keyExtractor))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T extends Comparable<? super T>> T min(Collection<T> self) {
    return self.stream()
        .min(Comparator.comparing(Function.identity()))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T extends Comparable<? super T>> T min(T[] self) {
    return Arrays.stream(self)
        .min(Comparator.comparing(Function.identity()))
        .orElseThrow(NoSuchElementException::new);
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

  public static <T> T find(T[] self, Predicate<T> predicate) {
    for (T e : self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  public static Integer find(int[] self, IntPredicate predicate) {
    for (int e : self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  public static Long find(long[] self, LongPredicate predicate) {
    for (long e : self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  public static Float find(float[] self, FloatPredicate predicate) {
    for (float e : self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  public static Double find(double[] self, DoublePredicate predicate) {
    for (double e : self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  public static Character find(char[] self, CharacterPredicate predicate) {
    for (char e : self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }


  /**
   * Count the number of times the provided predicate matched on an element of this collection
   * @param self self
   * @param predicate predicate
   * @return the number of times the provided predicate matched on an element of this collection
   * @param <T> the type of the list
   */
  public static <T> int count(Collection<T> self, Predicate<T> predicate) {
    int count = 0;
    for (T e : self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static <T> int count(T[] self, Predicate<T> predicate) {
    int count = 0;
    for (T e : self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static int count(int[] self, IntPredicate predicate) {
    int count = 0;
    for (int e : self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static int count(long[] self, LongPredicate predicate) {
    int count = 0;
    for (long e : self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static int count(float[] self, FloatPredicate predicate) {
    int count = 0;
    for (float e : self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static int count(double[] self, DoublePredicate predicate) {
    int count = 0;
    for (double e : self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static int count(char[] self, CharacterPredicate predicate) {
    int count = 0;
    for (char e : self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  /**
   * Find all elements matching the given predicate
   *
   * @param self      the collection
   * @param predicate the predicate
   * @param <T>       the Collection elements type
   * @return all elements matching the given predicate
   */
  public static <T> List<T> findAll(Collection<T> self, Predicate<T> predicate) {
    List<T> list = new ArrayList<>(self.size());
    for (T e : self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static <T> List<T> findAll(T[] self, Predicate<T> predicate) {
    List<T> list = new ArrayList<>(self.length);
    for (T e : self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static IntList findAll(int[] self, IntPredicate predicate) {
    IntList list = new IntArrayList(self.length);
    for (int e : self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static LongList findAll(long[] self, LongPredicate predicate) {
    LongList list = new LongArrayList(self.length);
    for (long e : self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static FloatList findAll(float[] self, FloatPredicate predicate) {
    FloatList list = new FloatArrayList(self.length);
    for (float e : self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static DoubleList findAll(double[] self, DoublePredicate predicate) {
    DoubleList list = new DoubleArrayList(self.length);
    for (double e : self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static CharacterList findAll(char[] self, CharacterPredicate predicate) {
    CharacterList list = new CharacterArrayList(self.length);
    for (char e : self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static <T> T reduce(Collection<T> collection, BiFunction<T, T, T> function) {
    Iterator<T> iterator = collection.iterator();
    if (!iterator.hasNext()) throw new NoSuchElementException();

    T result = iterator.next();
    while (iterator.hasNext()) {
      result = function.apply(result, iterator.next());
    }
    return result;
  }

  public static <T, U> U reduce(Collection<T> collection, U seed, BiFunction<U, T, U> function) {
    U u = seed;
    for (T t : collection) {
      u = function.apply(u, t);
    }
    return u;
  }

  /**
   * Converts a collection into a set
   *
   * @param self the collection
   * @param <T>  the type of the collection
   * @return a set
   */
  public static <T> Set<T> toSet(Collection<T> self) {
    Iterator<T> iterator = self.iterator();
    Set<T> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }

  public static IntSet toIntSet(Collection<?> self) {
    Iterator<?> iterator = self.iterator();
    IntSet set = new IntOpenHashSet();
    while (iterator.hasNext()) {
      set.add(((Number)iterator.next()).intValue());
    }
    return set;
  }

  public static LongSet toLongSet(Collection<?> self) {
    Iterator<?> iterator = self.iterator();
    LongSet set = new LongOpenHashSet();
    while (iterator.hasNext()) {
      set.add(((Number)iterator.next()).longValue());
    }
    return set;
  }

  public static FloatSet toFloatSet(Collection<?> self) {
    Iterator<?> iterator = self.iterator();
    FloatSet set = new FloatOpenHashSet();
    while (iterator.hasNext()) {
      set.add(((Number)iterator.next()).floatValue());
    }
    return set;
  }

  public static DoubleSet toDoubleSet(Collection<?> self) {
    Iterator<?> iterator = self.iterator();
    DoubleSet set = new DoubleOpenHashSet();
    while (iterator.hasNext()) {
      set.add(((Number)iterator.next()).doubleValue());
    }
    return set;
  }

  public static CharacterSet toCharacterSet(Collection<?> self) {
    Iterator<?> iterator = self.iterator();
    CharacterSet set = new CharacterOpenHashSet();
    while (iterator.hasNext()) {
      set.add((Character) iterator.next());
    }
    return set;
  }

  public static <T> Set<T> toSet(T[] self) {
    Set<T> set = new HashSet<>();
    for (T e : self) {
      set.add(e);
    }
    return set;
  }

  public static IntSet toSet(int[] self) {
    IntSet set = new IntOpenHashSet();
    for (int e : self) {
      set.add(e);
    }
    return set;
  }

  public static LongSet toSet(long[] self) {
    LongSet set = new LongOpenHashSet();
    for (long e : self) {
      set.add(e);
    }
    return set;
  }

  public static FloatSet toSet(float[] self) {
    FloatSet set = new FloatOpenHashSet();
    for (float e : self) {
      set.add(e);
    }
    return set;
  }

  public static DoubleSet toSet(double[] self) {
    DoubleSet set = new DoubleOpenHashSet();
    for (double e : self) {
      set.add(e);
    }
    return set;
  }

  public static CharacterSet toSet(char[] self) {
    CharacterSet set = new CharacterOpenHashSet();
    for (char e : self) {
      set.add(e);
    }
    return set;
  }

  /**
   * Converts a collection into a set
   *
   * @param self the collection
   * @param <T>  the type of the collection
   * @return a set
   */
  public static <T> List<T> toList(Collection<T> self) {
    Iterator<T> iterator = self.iterator();
    List<T> set = new ArrayList<>(self.size());
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }

  public static <T> List<T> toList(T[] self) {
    List<T> set = new ArrayList<>(self.length);
    for (T e : self) {
      set.add(e);
    }
    return set;
  }



  public static <T> IntList toList(int[] self) {
    IntList set = new IntArrayList();
    for (int e : self) {
      set.add(e);
    }
    return set;
  }

  public static <T> LongList toList(long[] self) {
    LongList set = new LongArrayList();
    for (long e : self) {
      set.add(e);
    }
    return set;
  }

  public static <T> FloatList toList(float[] self) {
    FloatList set = new FloatArrayList();
    for (float e : self) {
      set.add(e);
    }
    return set;
  }

  public static <T> DoubleList toList(double[] self) {
    DoubleList set = new DoubleArrayList();
    for (double e : self) {
      set.add(e);
    }
    return set;
  }

  public static <T> CharacterList toList(char[] self) {
    CharacterList set = new CharacterArrayList();
    for (char e : self) {
      set.add(e);
    }
    return set;
  }

  public static IntList toIntList(Collection<?> self) {
    Iterator<?> iterator = self.iterator();
    IntList list = new IntArrayList();
    while (iterator.hasNext()) {
      list.add(((Number)iterator.next()).intValue());
    }
    return list;
  }

  public static LongList toLongList(Collection<?> self) {
    Iterator<?> iterator = self.iterator();
    LongList set = new LongArrayList();
    while (iterator.hasNext()) {
      set.add(((Number)iterator.next()).longValue());
    }
    return set;
  }

  public static FloatList toFloatList(Collection<?> self) {
    Iterator<?> iterator = self.iterator();
    FloatList list = new FloatArrayList();
    while (iterator.hasNext()) {
      list.add(((Number)iterator.next()).floatValue());
    }
    return list;
  }

  public static DoubleList toDoubleList(Collection<?> self) {
    Iterator<?> iterator = self.iterator();
    DoubleList list = new DoubleArrayList();
    while (iterator.hasNext()) {
      list.add(((Number)iterator.next()).doubleValue());
    }
    return list;
  }

  public static CharacterList toCharacterList(Collection<?> self) {
    Iterator<?> iterator = self.iterator();
    CharacterList list = new CharacterArrayList();
    while (iterator.hasNext()) {
      list.add((Character) iterator.next());
    }
    return list;
  }

  /**
   * Map the list into an IntList
   *
   * @param collection    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return an IntList
   */
  public static <T> IntList mapToInt(Collection<T> collection, ToIntFunction<T> lambda1) {
    IntList intList = new IntArrayList(collection.size());
    for (T t : collection) {
      intList.add(lambda1.applyAsInt(t));
    }
    return intList;
  }

  /**
   * Map list elements to another type using the provided lambda
   *
   *
   * @param list    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @param <U>     the type of the returned list
   * @return an IntList
   */
  public static <T, U> List<U> map(Collection<T> list, Function<T, U> lambda1) {
    List<U> uList = new ArrayList<>(list.size());
    for (T t : list) {
      uList.add(lambda1.apply(t));
    }
    return uList;
  }

  public static <T, U> List<U> map(T[] array, Function<T, U> lambda1) {
    List<U> uList = new ArrayList<>(array.length);
    for (T t : array) {
      uList.add(lambda1.apply(t));
    }
    return uList;
  }

  public static <T> IntList mapToInt(T[] list, ToIntFunction<T> lambda1) {
    IntList intList = new IntArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      intList.add(lambda1.applyAsInt(list[i]));
    }
    return intList;
  }

  public static IntList mapToInt(long[] list, ToIntFunction<Long> lambda1) {
    IntList intList = new IntArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      intList.add(lambda1.applyAsInt(list[i]));
    }
    return intList;
  }

  public static IntList mapToInt(float[] list, ToIntFunction<Float> lambda1) {
    IntList intList = new IntArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      intList.add(lambda1.applyAsInt(list[i]));
    }
    return intList;
  }

  public static IntList mapToInt(double[] list, ToIntFunction<Double> lambda1) {
    IntList intList = new IntArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      intList.add(lambda1.applyAsInt(list[i]));
    }
    return intList;
  }

  public static IntList mapToInt(char[] list, ToIntFunction<Character> lambda1) {
    IntList intList = new IntArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      intList.add(lambda1.applyAsInt(list[i]));
    }
    return intList;
  }

  /**
   * Map the list into a LongList
   *
   * @param collection    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return a LongList
   */
  public static <T> LongList mapToLong(Collection<T> collection, ToLongFunction<T> lambda1) {
    LongList longList = new LongArrayList(collection.size());
    for (T t : collection) {
      longList.add(lambda1.applyAsLong(t));
    }
    return longList;
  }

  public static <T> LongList mapToLong(T[] list, ToLongFunction<T> lambda1) {
    LongList longList = new LongArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      longList.add(lambda1.applyAsLong(list[i]));
    }
    return longList;
  }

  public static LongList mapToLong(int[] list, ToLongFunction<Integer> lambda1) {
    LongList longList = new LongArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      longList.add(lambda1.applyAsLong(list[i]));
    }
    return longList;
  }

  public static LongList mapToLong(float[] list, ToLongFunction<Float> lambda1) {
    LongList longList = new LongArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      longList.add(lambda1.applyAsLong(list[i]));
    }
    return longList;
  }

  public static LongList mapToLong(double[] list, ToLongFunction<Double> lambda1) {
    LongList longList = new LongArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      longList.add(lambda1.applyAsLong(list[i]));
    }
    return longList;
  }

  public static LongList mapToLong(char[] list, ToLongFunction<Character> lambda1) {
    LongList longList = new LongArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      longList.add(lambda1.applyAsLong(list[i]));
    }
    return longList;
  }

  /**
   * Map the list into a FloatList
   *
   * @param collection    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return a FloatList
   */
  public static <T> FloatList mapToFloat(Collection<T> collection, ToFloatFunction<T> lambda1) {
    FloatList floatList = new FloatArrayList(collection.size());
    for (T t : collection) {
      floatList.add(lambda1.applyAsFloat(t));
    }
    return floatList;
  }

  public static <T> FloatList mapToFloat(T[] list, ToFloatFunction<T> lambda1) {
    FloatList floatList = new FloatArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      floatList.add(lambda1.applyAsFloat(list[i]));
    }
    return floatList;
  }

  public static FloatList mapToFloat(int[] list, ToFloatFunction<Integer> lambda1) {
    FloatList floatList = new FloatArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      floatList.add(lambda1.applyAsFloat(list[i]));
    }
    return floatList;
  }

  public static FloatList mapToFloat(long[] list, ToFloatFunction<Long> lambda1) {
    FloatList floatList = new FloatArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      floatList.add(lambda1.applyAsFloat(list[i]));
    }
    return floatList;
  }

  public static FloatList mapToFloat(double[] list, ToFloatFunction<Double> lambda1) {
    FloatList floatList = new FloatArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      floatList.add(lambda1.applyAsFloat(list[i]));
    }
    return floatList;
  }

  public static FloatList mapToFloat(char[] list, ToFloatFunction<Character> lambda1) {
    FloatList floatList = new FloatArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      floatList.add(lambda1.applyAsFloat(list[i]));
    }
    return floatList;
  }

  /**
   * Map the list into a DoubleList
   *
   * @param collection    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return a DoubleList
   */
  public static <T> DoubleList mapToDouble(List<T> collection, ToDoubleFunction<T> lambda1) {
    DoubleList floatList = new DoubleArrayList(collection.size());
    for (T t : collection) {
      floatList.add(lambda1.applyAsDouble(t));
    }
    return floatList;
  }

  public static <T> DoubleList mapToDouble(T[] list, ToDoubleFunction<T> lambda1) {
    DoubleList floatList = new DoubleArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      floatList.add(lambda1.applyAsDouble(list[i]));
    }
    return floatList;
  }

  public static <T> DoubleList mapToDouble(int[] list, ToDoubleFunction<Integer> lambda1) {
    DoubleList floatList = new DoubleArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      floatList.add(lambda1.applyAsDouble(list[i]));
    }
    return floatList;
  }

  public static <T> DoubleList mapToDouble(long[] list, ToDoubleFunction<Long> lambda1) {
    DoubleList floatList = new DoubleArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      floatList.add(lambda1.applyAsDouble(list[i]));
    }
    return floatList;
  }

  public static <T> DoubleList mapToDouble(float[] list, ToDoubleFunction<Float> lambda1) {
    DoubleList floatList = new DoubleArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      floatList.add(lambda1.applyAsDouble(list[i]));
    }
    return floatList;
  }

  public static <T> DoubleList mapToDouble(char[] list, ToDoubleFunction<Character> lambda1) {
    DoubleList floatList = new DoubleArrayList(list.length);
    for (int i = 0; i < list.length; i++) {
      floatList.add(lambda1.applyAsDouble(list[i]));
    }
    return floatList;
  }

  // TODO mapToCharacter

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

  public static String join(Collection<?> self) {
    return join(self, "");
  }

  public static <T> String join(T[] self) {
    return join(self, "");
  }

  public static <T> String join(T[] self, String separator) {
    return Arrays.stream(self)
        .map(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T> String join(int[] self) {
    return join(self, "");
  }

  public static <T> String join(int[] self, String separator) {
    return Arrays.stream(self)
        .mapToObj(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T> String join(long[] self) {
    return join(self, "");
  }

  public static <T> String join(long[] self, String separator) {
    return Arrays.stream(self)
        .mapToObj(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T> String join(float[] self) {
    return join(self, "");
  }

  public static <T> String join(float[] self, String separator) {
    return toList(self).stream()
        .map(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T> String join(double[] self) {
    return join(self, "");
  }

  public static <T> String join(double[] self, String separator) {
    return Arrays.stream(self)
        .mapToObj(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T> String join(char[] self) {
    return join(self, "");
  }

  public static <T> String join(char[] self, String separator) {
    return toList(self).stream()
        .map(String::valueOf)
        .collect(Collectors.joining(separator));
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
  public static <T, U extends Comparable<U>> void sortBy(List<T> self, Function<T, U> keyExtractor) {
    self.sort(Comparator.comparing(keyExtractor));
  }

  public static <T, U extends Comparable<U>> void sort(List<T> self) {
    Object[] a = self.toArray();
    Arrays.sort(a);
    ListIterator<T> i = self.listIterator();
    for (Object e : a) {
      i.next();
      i.set((T) e);
    }
  }

  public static <T, U extends Comparable<U>> void sortBy(T[] self, Function<T, U> keyExtractor) {
    Arrays.sort(self, Comparator.comparing(keyExtractor));
  }

  public static <T, U extends Comparable<U>> void sort(T[] self) {
    Arrays.sort(self);
  }

  public static void sort(int[] self) {
    Arrays.sort(self);
  }

  public static void sort(long[] self) {
    Arrays.sort(self);
  }

  public static void sort(float[] self) {
    Arrays.sort(self);
  }

  public static void sort(double[] self) {
    Arrays.sort(self);
  }

  public static void sort(char[] self) {
    Arrays.sort(self);
  }


  /**
   * Reverse a list
   *
   * @param list the list
   */
  public static void reverse(List<?> list) {
    Collections.reverse(list);
  }

  public static <T> void reverse(T[] self) {
    for (int i = 0; i < self.length / 2; i++) {
      swap(self, i, self.length - 1 - i);
    }
  }

  public static <T> void reverse(int[] self) {
    for (int i = 0; i < self.length / 2; i++) {
      swap(self, i, self.length - 1 - i);
    }
  }

  public static <T> void reverse(long[] self) {
    for (int i = 0; i < self.length / 2; i++) {
      swap(self, i, self.length - 1 - i);
    }
  }

  public static <T> void reverse(float[] self) {
    for (int i = 0; i < self.length / 2; i++) {
      swap(self, i, self.length - 1 - i);
    }
  }

  public static <T> void reverse(double[] self) {
    for (int i = 0; i < self.length / 2; i++) {
      swap(self, i, self.length - 1 - i);
    }
  }

  public static <T> void reverse(char[] self) {
    for (int i = 0; i < self.length / 2; i++) {
      swap(self, i, self.length - 1 - i);
    }
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
   * Get the last element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @param <T>  the type of the list
   * @return the last element of the list
   */
  public static <T> T getLast(List<T> self) {
    return self.get(self.size() - 1);
  }

  public static <T> T getLast(T[] self) {
    return self[self.length - 1];
  }

  public static int getLast(int[] self) {
    return self[self.length - 1];
  }

  public static long getLast(long[] self) {
    return self[self.length - 1];
  }

  public static float getLast(float[] self) {
    return self[self.length - 1];
  }

  public static double getLast(double[] self) {
    return self[self.length - 1];
  }

  public static char getLast(char[] self) {
    return self[self.length - 1];
  }

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

  public static <T> T getLastOrNull(T[] self) {
    return self.length > 0 ? self[self.length - 1]: null;
  }

  public static Integer getLastOrNull(int[] self) {
    return self.length > 0 ? self[self.length - 1]: null;
  }

  public static Long getLastOrNull(long[] self) {
    return self.length > 0 ? self[self.length - 1]: null;
  }

  public static Float getLastOrNull(float[] self) {
    return self.length > 0 ? self[self.length - 1]: null;
  }

  public static Double getLastOrNull(double[] self) {
    return self.length > 0 ? self[self.length - 1]: null;
  }

  public static Character getLastOrNull(char[] self) {
    return self.length > 0 ? self[self.length - 1]: null;
  }

  /**
   * Get the first element of the list. This method will throw an exception if the list is empty
   *
   * @param self the list
   * @param <T>  the type of the list
   * @return the first element of the list
   */
  public static <T> T getFirst(List<T> self) {
    return self.get(0);
  }

  public static <T> T getFirst(T[] self) {
    return self[0];
  }

  public static int getFirst(int[] self) {
    return self[0];
  }

  public static long getFirst(long[] self) {
    return self[0];
  }

  public static float getFirst(float[] self) {
    return self[0];
  }

  public static double getFirst(double[] self) {
    return self[0];
  }

  public static char getFirst(char[] self) {
    return self[0];
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

  public static <T> void setLast(T[] self, T value) {
    self[self.length - 1] = value;
  }

  public static void setLast(int[] self, int value) {
    self[self.length - 1] = value;
  }

  public static void setLast(long[] self, long value) {
    self[self.length - 1] = value;
  }

  public static void setLast(float[] self, float value) {
    self[self.length - 1] = value;
  }

  public static void setLast(double[] self, double value) {
    self[self.length - 1] = value;
  }

  public static void setLast(char[] self, char value) {
    self[self.length - 1] = value;
  }

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

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param self  the array
   * @param index the index
   * @return the element at the specified index
   */
  public static Character getAtSafe(char[] self, int index) {
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
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(int[] self, int index, int value) {
    if (index >= 0 && index < self.length) {
      self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(float[] self, int index, float value) {
    if (index >= 0 && index < self.length) {
      self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(double[] self, int index, double value) {
    if (index >= 0 && index < self.length) {
      self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(long[] self, int index, long value) {
    if (index >= 0 && index < self.length) {
      self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(char[] self, int index, char value) {
    if (index >= 0 && index < self.length) {
      self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(short[] self, int index, short value) {
    if (index >= 0 && index < self.length) {
      self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(byte[] self, int index, byte value) {
    if (index >= 0 && index < self.length) {
      self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(boolean[] self, int index, boolean value) {
    if (index >= 0 && index < self.length) {
      self[index] = value;
    }
  }

  // TODO document this
  public static <T, U> Map<T, U> withDefault(Map<T, U> self, Function<Object, U> defaultFunction) {
    return withDefault(self, false, defaultFunction);
  }

  public static <T, U> Map<T, U> withDefault(Map<T, U> self, boolean insert, Function<Object, U> defaultFunction) {
    return MapWithDefault.newInstance(self, defaultFunction, insert);
  }

  /**
   * Get the element at the specified key
   *
   * @param self  the map
   * @param key the key
   * @param <T>   the type of the map values
   * @return the element at the specified key
   */
  public static <T> T getAt(Map<?, T> self, Object key) {
    return self.get(key);
  }

  /**
   * Set the element at the specified key
   *
   * @param self  the map
   * @param key the key
   * @param value the value
   * @param <T>   the type of the map keys
   * @param <U>   the type of the map values
   * @return the element at the specified index
   */
  public static <T, U> U putAt(Map<T, U> self, T key, U value) {
    return self.put(key, value);
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param a the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static <T> Object[] plus(T[] a, T[] b) {
    Object[] sum = new Object[a.length + b.length];
    System.arraycopy(a, 0, sum, 0, a.length);
    System.arraycopy(b, 0, sum, a.length, b.length);
    return sum;
  }

  public static int[] plus(int[] a, int[] b) {
    int[] sum = new int[a.length + b.length];
    System.arraycopy(a, 0, sum, 0, a.length);
    System.arraycopy(b, 0, sum, a.length, b.length);
    return sum;
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param a the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static long[] plus(long[] a, long[] b) {
    long[] sum = new long[a.length + b.length];
    System.arraycopy(a, 0, sum, 0, a.length);
    System.arraycopy(b, 0, sum, a.length, b.length);
    return sum;
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param a the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static float[] plus(float[] a, float[] b) {
    float[] sum = new float[a.length + b.length];
    System.arraycopy(a, 0, sum, 0, a.length);
    System.arraycopy(b, 0, sum, a.length, b.length);
    return sum;
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param a the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static double[] plus(double[] a, double[] b) {
    double[] sum = new double[a.length + b.length];
    System.arraycopy(a, 0, sum, 0, a.length);
    System.arraycopy(b, 0, sum, a.length, b.length);
    return sum;
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param a the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static boolean[] plus(boolean[] a, boolean[] b) {
    boolean[] sum = new boolean[a.length + b.length];
    System.arraycopy(a, 0, sum, 0, a.length);
    System.arraycopy(b, 0, sum, a.length, b.length);
    return sum;
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param a the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static char[] plus(char[] a, char[] b) {
    char[] sum = new char[a.length + b.length];
    System.arraycopy(a, 0, sum, 0, a.length);
    System.arraycopy(b, 0, sum, a.length, b.length);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first one then the content of the second
   *
   * @param a the first collection
   * @param b the second collection
   * @return a new array containing the content of the first one then the content of the second
   */
  public static <T> List<T> plus(List<T> a, Collection<T> b) {
    List<T> sum = new ArrayList<>(a.size() + b.size());
    sum.addAll(a);
    sum.addAll(b);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first one then the content of the second
   *
   * @param a the first collection
   * @param b the second collection
   * @return a new array containing the content of the first one then the content of the second
   */
  public static <T> Set<T> plus(Set<T> a, Collection<T> b) {
    Set<T> sum = new HashSet<>(a.size() + b.size());
    sum.addAll(a);
    sum.addAll(b);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first one then the content of the second
   *
   * @param a the first collection
   * @param b the second collection
   * @return a new array containing the content of the first one then the content of the second
   */
  public static <T> Queue<T> plus(Queue<T> a, Collection<T> b) {
    Queue<T> sum = new LinkedList<>();
    sum.addAll(a);
    sum.addAll(b);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first without the content of the second
   *
   * @param a the first collection
   * @param b the second collection
   * @return a new collection containing the content of the first without the content of the second
   */
  public static <T> List<T> minus(List<T> a, Collection<T> b) {
    List<T> sum = new ArrayList<>(a);
    sum.removeAll(b);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first without the content of the second
   *
   * @param a the first collection
   * @param b the second collection
   * @return a new collection containing the content of the first without the content of the second
   */
  public static <T> Set<T> minus(Set<T> a, Collection<T> b) {
    Set<T> sum = new HashSet<>(a);
    sum.removeAll(b);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first without the content of the second
   *
   * @param a the first collection
   * @param b the second collection
   * @return a new collection containing the content of the first without the content of the second
   */
  public static <T> Queue<T> minus(Queue<T> a, Collection<T> b) {
    Queue<T> sum = new LinkedList<>(a);
    sum.removeAll(b);
    return sum;
  }

  public static <T, U> Map<T, U> plus(Map<T, U> a, Map<T, U> b) {
    Map<T, U> map = new HashMap<>(a);
    map.putAll(b);
    return map;
  }

  public static <T, U> Map<T, U> minus(Map<T, U> a, Map<T, U> b) {
    Map<T, U> map = new HashMap<>(a);
    b.forEach(map::remove);
    return map;
  }

  /**
   * Returns an unmodifiable view of the given collection
   * @param self the collection
   * @return an unmodifiable view of the given collection
   * @param <T> the generic type
   */
  public static <T> List<T> asUnmodifiable(List<T> self) {
    return Collections.unmodifiableList(self);
  }

  public static <T> Set<T> asUnmodifiable(Set<T> self) {
    return Collections.unmodifiableSet(self);
  }

  public static <K,V> Map<K,V> asUnmodifiable(Map<K,V> self) {
    return Collections.unmodifiableMap(self);
  }

  public static <T> List<T> asUnmodifiable(T[] self) {
    return Collections.unmodifiableList(Arrays.asList(self));
  }

  /**
   * Collates this iterable into sub-lists of length <code>size</code>.
   * Example:
   * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4, 5, 6, 7 ]
   * def coll = list.collate( 3 )
   * assert coll == [ [ 1, 2, 3 ], [ 4, 5, 6 ], [ 7 ] ]</pre>
   *
   * @param self          an Iterable
   * @param size          the length of each sub-list in the returned list
   * @return a List containing the data collated into sub-lists
   * @since 2.4.0
   */
  public static <T> List<List<T>> collate(Iterable<T> self, int size) {
    return collate(self, size, true);
  }

  /**
   * @deprecated use the Iterable variant instead
   * @see #collate(Iterable, int)
   * @since 1.8.6
   */
  @Deprecated
  public static <T> List<List<T>> collate( List<T> self, int size ) {
    return collate((Iterable<T>) self, size) ;
  }

  /**
   * Collates this iterable into sub-lists of length <code>size</code> stepping through the code <code>step</code>
   * elements for each subList.
   * Example:
   * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4 ]
   * def coll = list.collate( 3, 1 )
   * assert coll == [ [ 1, 2, 3 ], [ 2, 3, 4 ], [ 3, 4 ], [ 4 ] ]</pre>
   *
   * @param self          an Iterable
   * @param size          the length of each sub-list in the returned list
   * @param step          the number of elements to step through for each sub-list
   * @return a List containing the data collated into sub-lists
   * @since 2.4.0
   */
  public static <T> List<List<T>> collate(Iterable<T> self, int size, int step) {
    return collate(self, size, step, true);
  }

  /**
   * @deprecated use the Iterable variant instead
   * @see #collate(Iterable, int, int)
   * @since 1.8.6
   */
  @Deprecated
  public static <T> List<List<T>> collate( List<T> self, int size, int step ) {
    return collate((Iterable<T>) self, size, step) ;
  }

  /**
   * Collates this iterable into sub-lists of length <code>size</code>. Any remaining elements in
   * the iterable after the subdivision will be dropped if <code>keepRemainder</code> is false.
   * Example:
   * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4, 5, 6, 7 ]
   * def coll = list.collate( 3, false )
   * assert coll == [ [ 1, 2, 3 ], [ 4, 5, 6 ] ]</pre>
   *
   * @param self          an Iterable
   * @param size          the length of each sub-list in the returned list
   * @param keepRemainder if true, any remaining elements are returned as sub-lists.  Otherwise they are discarded
   * @return a List containing the data collated into sub-lists
   * @since 2.4.0
   */
  public static <T> List<List<T>> collate(Iterable<T> self, int size, boolean keepRemainder) {
    return collate(self, size, size, keepRemainder);
  }


  /**
   * Collates this iterable into sub-lists of length <code>size</code> stepping through the code <code>step</code>
   * elements for each sub-list.  Any remaining elements in the iterable after the subdivision will be dropped if
   * <code>keepRemainder</code> is false.
   * Example:
   * <pre class="groovyTestCase">
   * def list = [ 1, 2, 3, 4 ]
   * assert list.collate( 2, 2, true  ) == [ [ 1, 2 ], [ 3, 4 ] ]
   * assert list.collate( 3, 1, true  ) == [ [ 1, 2, 3 ], [ 2, 3, 4 ], [ 3, 4 ], [ 4 ] ]
   * assert list.collate( 3, 1, false ) == [ [ 1, 2, 3 ], [ 2, 3, 4 ] ]
   * </pre>
   *
   * @param self          an Iterable
   * @param size          the length of each sub-list in the returned list
   * @param step          the number of elements to step through for each sub-list
   * @param keepRemainder if true, any remaining elements are returned as sub-lists.  Otherwise they are discarded
   * @return a List containing the data collated into sub-lists
   */
  public static <T> List<List<T>> collate(Iterable<T> self, int size, int step, boolean keepRemainder) {
    List<T> selfList = new ArrayList<>();
    self.forEach(selfList::add);
    List<List<T>> answer = new ArrayList<List<T>>();
    if (size <= 0 || selfList.isEmpty()) {
      answer.add(selfList);
    } else {
      for (int pos = 0; pos < selfList.size() && pos > -1; pos += step) {
        if (!keepRemainder && pos > selfList.size() - size) {
          break ;
        }
        List<T> element = new ArrayList<T>() ;
        for (int offs = pos; offs < pos + size && offs < selfList.size(); offs++) {
          element.add(selfList.get(offs));
        }
        answer.add( element ) ;
      }
    }
    return answer ;
  }

  public static List<List<Object>> combinations(Collection<? extends Collection<?>> collections) {
    List<List<Object>> combos = new ArrayList<>();
    for (Collection<?> collection : collections) {
      if (combos.isEmpty()) {
        for (Object object : collection) {
          List<Object> list = new ArrayList<>();
          list.add(object);
          combos.add(list);
        }
      } else {
        List<List<Object>> next = new ArrayList<>(); // each list plus each item
        for (Object object : collection) {
          for (List<?> combo : combos) {
            List<Object> list = new ArrayList<>(combo);
            list.add(object);
            next.add(list);
          }
        }
        combos = next;
      }
      if (combos.isEmpty())
        break;
    }
    return combos;
  }
}
