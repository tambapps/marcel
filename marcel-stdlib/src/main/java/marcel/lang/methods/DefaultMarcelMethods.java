package marcel.lang.methods;

import marcel.lang.IntRange;
import marcel.lang.compile.ExtensionClass;
import marcel.util.primitives.collections.CharCollection;
import marcel.util.primitives.collections.DoubleCollection;
import marcel.util.primitives.collections.FloatCollection;
import marcel.util.primitives.collections.IntCollection;
import marcel.util.primitives.collections.LongCollection;
import marcel.util.primitives.collections.lists.CharArrayList;
import marcel.util.primitives.collections.lists.CharList;
import marcel.util.primitives.collections.lists.DoubleArrayList;
import marcel.util.primitives.collections.lists.DoubleList;
import marcel.util.primitives.collections.lists.FloatArrayList;
import marcel.util.primitives.collections.lists.FloatList;
import marcel.util.primitives.collections.lists.IntArrayList;
import marcel.util.primitives.collections.lists.IntList;
import marcel.util.primitives.collections.lists.LongArrayList;
import marcel.util.primitives.collections.lists.LongList;
import marcel.util.primitives.collections.sets.CharOpenHashSet;
import marcel.util.primitives.collections.sets.CharSet;
import marcel.util.primitives.collections.sets.DoubleOpenHashSet;
import marcel.util.primitives.collections.sets.DoubleSet;
import marcel.util.primitives.collections.sets.FloatOpenHashSet;
import marcel.util.primitives.collections.sets.FloatSet;
import marcel.util.primitives.collections.sets.IntOpenHashSet;
import marcel.util.primitives.collections.sets.IntSet;
import marcel.util.primitives.collections.sets.LongOpenHashSet;
import marcel.util.primitives.collections.sets.LongSet;
import marcel.util.primitives.iterators.IntIterator;
import marcel.lang.runtime.MapWithDefault;
import marcel.util.function.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * This class contains methods that are considered as extension methods.
 * Extension methods are methods that can be called as if they were instance methods
 * of the class of their first parameter.
 * 
 * For example, a method:
 * String foo(Integer bar, String zoo)
 * 
 * Can be called like:
 * Integer.foo(zoo) -> String
 * 
 * The first parameter MUST be named "$self" to be considered as an extension method.
 */
@NullMarked
@ExtensionClass
@SuppressWarnings({"unused"})
public final class DefaultMarcelMethods {

  /**
   * Returns all matched groups
   *
   * @param $self the matcher
   * @return all matched groups
   */
  public static List<String> groups(Matcher $self) {
    if (!$self.find()) return Collections.emptyList();
    int count = $self.groupCount();
    List<String> groups = new ArrayList<>(count);
    for (int i = 0; i <= count; i++) {
      groups.add($self.group(i));
    }
    return groups;
  }

  /**
   * Returns all defined matched groups
   *
   * @param $self the matcher
   * @return all matched groups
   */
  public static List<String> definedGroups(Matcher $self) {
    if (!$self.find()) return Collections.emptyList();
    int count = $self.groupCount();
    List<String> groups = new ArrayList<>(count);
    for (int i = 1; i <= count; i++) {
      groups.add($self.group(i));
    }
    return groups;
  }

  /**
   * Swaps the elements at the specified positions in the list
   *
   * @param $self the list
   * @param i the index of the first element to swap
   * @param j the index of the second element to swap
   * @param <T> the type of elements in the list
   */
  public static <T> void swap(List $self, int i, int j) {
    $self.set(i, $self.set(j, $self.get(i)));
  }

  /**
   * Swaps the elements at the specified positions in the array
   *
   * @param $self the array
   * @param i the index of the first element to swap
   * @param j the index of the second element to swap
   * @param <T> the type of elements in the array
   */
  public static <T> void swap(T[] $self, int i, int j) {
    T temp = $self[i];
    $self[i] = $self[j];
    $self[j] = temp;
  }

  /**
   * Swaps the elements at the specified positions in the int array
   *
   * @param $self the int array
   * @param i the index of the first element to swap
   * @param j the index of the second element to swap
   */
  public static void swap(int[] $self, int i, int j) {
    int temp = $self[i];
    $self[i] = $self[j];
    $self[j] = temp;
  }

  /**
   * Swaps the elements at the specified positions in the long array
   *
   * @param $self the long array
   * @param i the index of the first element to swap
   * @param j the index of the second element to swap
   */
  public static void swap(long[] $self, int i, int j) {
    long temp = $self[i];
    $self[i] = $self[j];
    $self[j] = temp;
  }

  /**
   * Swaps the elements at the specified positions in the float array
   *
   * @param $self the float array
   * @param i the index of the first element to swap
   * @param j the index of the second element to swap
   */
  public static void swap(float[] $self, int i, int j) {
    float temp = $self[i];
    $self[i] = $self[j];
    $self[j] = temp;
  }

  /**
   * Swaps the elements at the specified positions in the double array
   *
   * @param $self the double array
   * @param i the index of the first element to swap
   * @param j the index of the second element to swap
   */
  public static void swap(double[] $self, int i, int j) {
    double temp = $self[i];
    $self[i] = $self[j];
    $self[j] = temp;
  }

  /**
   * Swaps the elements at the specified positions in the char array
   *
   * @param $self the char array
   * @param i the index of the first element to swap
   * @param j the index of the second element to swap
   */
  public static void swap(char[] $self, int i, int j) {
    char temp = $self[i];
    $self[i] = $self[j];
    $self[j] = temp;
  }

  /**
   * Adds an element to a sorted list while maintaining the sort order
   *
   * @param $self the sorted list
   * @param element the element to add
   * @param <T> the type of elements in the list, must implement Comparable
   */
  public static <T extends Comparable<T>> void sortedAdd(List<T> $self, T element) {
    int i = 0;
    while (i < $self.size() && $self.get(i).compareTo(element) < 0) {
      i++;
    }
    $self.add(i, element);
  }

  /**
   * Returns a list of unique elements based on the key extractor function
   *
   * @param $self the collection to filter
   * @param keyExtractor function to extract keys for uniqueness comparison
   * @param <T> the type of elements in the collection
   * @return a list containing only unique elements
   */
  public static <T> List<T> unique(Collection<T> $self, Function<T, ?> keyExtractor) {
    Set<Object> set = new HashSet<>($self.size());
    List<T> list = new ArrayList<>();
    for (T o : $self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  /**
   * Returns a set of all unique elements in the collection
   *
   * @param $self the collection to convert to a set
   * @param <T> the type of elements in the collection
   * @return a set containing all unique elements from the collection
   */
  public static <T> Set<T> unique(Collection<T> $self) {
    return new HashSet<>($self);
  }

  /**
   * Returns a collection of unique elements from an array based on the key extractor function
   *
   * @param $self the array to filter
   * @param keyExtractor function to extract keys for uniqueness comparison
   * @param <T> the type of elements in the array
   * @return a collection containing only unique elements
   */
  public static <T> Collection<T> unique(T[] $self, Function<T, ?> keyExtractor) {
    Set<Object> set = new HashSet<>($self.length);
    List<T> list = new ArrayList<>($self.length);
    for (T o : $self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  /**
   * Returns a set of all unique elements in the array
   *
   * @param $self the array to convert to a set
   * @param <T> the type of elements in the array
   * @return a set containing all unique elements from the array
   */
  public static <T> Set<T> unique(T[] $self) {
    return new HashSet<>(Arrays.asList($self));
  }

  public static IntCollection unique(int[] $self, IntFunction<?> keyExtractor) {
    Set<Object> set = new HashSet<>($self.length);
    IntList list = new IntArrayList($self.length);
    for (int o : $self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static IntSet unique(int[] $self) {
    return new IntOpenHashSet(IntArrayList.wrap($self));
  }

  public static LongCollection unique(long[] $self, LongFunction<?> keyExtractor) {
    Set<Object> set = new HashSet<>($self.length);
    LongList list = new LongArrayList($self.length);
    for (long o : $self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static LongSet unique(long[] $self) {
    return new LongOpenHashSet(LongArrayList.wrap($self));
  }

  public static FloatCollection unique(float[] $self, FloatFunction<?> keyExtractor) {
    Set<Object> set = new HashSet<>($self.length);
    FloatList list = new FloatArrayList($self.length);
    for (float o : $self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static FloatSet unique(float[] $self) {
    return new FloatOpenHashSet(FloatArrayList.wrap($self));
  }

  public static DoubleCollection unique(double[] $self, DoubleFunction<?> keyExtractor) {
    Set<Object> set = new HashSet<>($self.length);
    DoubleList list = new DoubleArrayList($self.length);
    for (double o : $self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static DoubleSet unique(double[] $self) {
    return new DoubleOpenHashSet(DoubleArrayList.wrap($self));
  }

  public static CharCollection unique(char[] $self, CharFunction<?> keyExtractor) {
    Set<Object> set = new HashSet<>($self.length);
    CharList list = new CharArrayList($self.length);
    for (char o : $self) {
      if (set.add(keyExtractor.apply(o))) {
        list.add(o);
      }
    }
    return list;
  }

  public static CharSet unique(char[] $self) {
    return new CharOpenHashSet(CharArrayList.wrap($self));
  }

  /**
   * Replaces an element in a collection with another element
   *
   * @param $self the collection
   * @param element the element to replace
   * @param replacement the replacement element
   * @param <T> the type of elements in the collection
   * @return true if the element was found and replaced, false otherwise
   */
  public static <T> boolean replace(Collection<T> $self, T element, T replacement) {
    if ($self.remove(element)) {
      return $self.add(replacement);
    }
    throw new NoSuchElementException();
  }

  /**
   * Randomly permutes the specified list using a default source of randomness
   *
   * @param $self the list to be shuffled
   */
  public static void shuffle(List<?> $self) {
    Collections.shuffle($self);
  }

  /**
   * Randomly permutes the specified list using the provided random generator
   *
   * @param $self the list to be shuffled
   * @param rnd the source of randomness to use to shuffle the list
   */
  public static void shuffle(List<?> $self, Random rnd) {
    Collections.shuffle($self, rnd);
  }

  /**
   * Randomly permutes the specified array using a default source of randomness
   *
   * @param $self the array to be shuffled
   * @param <T> the type of elements in the array
   */
  public static <T> void shuffle(T[] $self) {
    shuffle($self, new Random());
  }

  /**
   * Randomly permutes the specified array using the provided random generator
   *
   * @param $self the array to be shuffled
   * @param rnd the source of randomness to use to shuffle the array
   * @param <T> the type of elements in the array
   */
  public static <T> void shuffle(T[] $self, Random rnd) {
    for (int i=$self.length; i>1; i--)
      swap($self, i-1, rnd.nextInt(i));
  }

  public static <T> void shuffle(int[] $self) {
    shuffle($self, new Random());
  }

  public static <T> void shuffle(int[] $self, Random rnd) {
    for (int i=$self.length; i>1; i--)
      swap($self, i-1, rnd.nextInt(i));
  }

  public static <T> void shuffle(long[] $self) {
    shuffle($self, new Random());
  }

  public static <T> void shuffle(long[] $self, Random rnd) {
    for (int i=$self.length; i>1; i--)
      swap($self, i-1, rnd.nextInt(i));
  }

  public static <T> void shuffle(float[] $self) {
    shuffle($self, new Random());
  }

  public static <T> void shuffle(float[] $self, Random rnd) {
    for (int i=$self.length; i>1; i--)
      swap($self, i-1, rnd.nextInt(i));
  }

  public static <T> void shuffle(double[] $self) {
    shuffle($self, new Random());
  }

  public static <T> void shuffle(double[] $self, Random rnd) {
    for (int i=$self.length; i>1; i--)
      swap($self, i-1, rnd.nextInt(i));
  }

  public static <T> void shuffle(char[] $self) {
    shuffle($self, new Random());
  }

  public static <T> void shuffle(char[] $self, Random rnd) {
    for (int i=$self.length; i>1; i--)
      swap($self, i-1, rnd.nextInt(i));
  }

  /**
   * Returns true if all the elements match the given predicate
   *
   * @param $self      the collection
   * @param predicate the predicate
   * @param <T>       the Collection elements type
   * @return true if all the elements match the given predicate
   */
  public static <T> boolean all(Collection<T> $self, Predicate<T> predicate) {
    for (T e : $self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static <T> boolean all(T[] $self, Predicate<T> predicate) {
    for (T e : $self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean all(int[] $self, IntPredicate predicate) {
    for (int e : $self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean all(long[] $self, LongPredicate predicate) {
    for (long e : $self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean all(float[] $self, FloatPredicate predicate) {
    for (float e : $self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean all(double[] $self, DoublePredicate predicate) {
    for (double e : $self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean all(char[] $self, CharPredicate predicate) {
    for (char e : $self) {
      if (!predicate.test(e)) return false;
    }
    return true;
  }

  /**
   * Returns true if at least one element matches the given predicate
   *
   * @param $self      the collection
   * @param predicate the predicate
   * @param <T>       the Collection elements type
   * @return true if all the elements match the given predicate
   */
  public static <T> boolean any(Collection<T> $self, Predicate<T> predicate) {
    for (T e : $self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static <T> boolean any(T[] $self, Predicate<T> predicate) {
    for (T e : $self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static boolean any(int[] $self, IntPredicate predicate) {
    for (int e : $self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static boolean any(long[] $self, LongPredicate predicate) {
    for (long e : $self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static boolean any(float[] $self, FloatPredicate predicate) {
    for (float e : $self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static boolean any(double[] $self, DoublePredicate predicate) {
    for (double e : $self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  public static boolean any(char[] $self, CharPredicate predicate) {
    for (char e : $self) {
      if (predicate.test(e)) return true;
    }
    return false;
  }

  /**
   * Returns true if none of the elements match the given predicate
   *
   * @param $self      the collection
   * @param predicate the predicate
   * @param <T>       the Collection elements type
   * @return true if none of the elements match the given predicate
   */
  public static <T> boolean none(Collection<T> $self, Predicate<T> predicate) {
    for (T e : $self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static <T> boolean none(T[] $self, Predicate<T> predicate) {
    for (T e : $self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean none(int[] $self, IntPredicate predicate) {
    for (int e : $self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean none(long[] $self, LongPredicate predicate) {
    for (long e : $self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean none(float[] $self, FloatPredicate predicate) {
    for (float e : $self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean none(double[] $self, DoublePredicate predicate) {
    for (double e : $self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static boolean none(char[] $self, CharPredicate predicate) {
    for (char e : $self) {
      if (predicate.test(e)) return false;
    }
    return true;
  }

  public static <T, U extends Comparable<? super U>> T max(Collection<T> $self, Function<T, U> keyExtractor) {
    return $self.stream()
        .max(Comparator.comparing(keyExtractor))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T, U extends Comparable<? super U>> T max(T[] $self, Function<T, U> keyExtractor) {
    return Arrays.stream($self)
        .max(Comparator.comparing(keyExtractor))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T extends Comparable<? super T>> T max(Collection<T> $self) {
    return $self.stream()
        .max(Comparator.comparing(Function.identity()))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T extends Comparable<? super T>> T max(T[] $self) {
    return Arrays.stream($self)
        .max(Comparator.comparing(Function.identity()))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T, U extends Comparable<? super U>> T min(Collection<T> $self, Function<T, U> keyExtractor) {
    return $self.stream()
        .min(Comparator.comparing(keyExtractor))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T, U extends Comparable<? super U>> T min(T[] $self, Function<T, U> keyExtractor) {
    return Arrays.stream($self)
        .min(Comparator.comparing(keyExtractor))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T extends Comparable<? super T>> T min(Collection<T> $self) {
    return $self.stream()
        .min(Comparator.comparing(Function.identity()))
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T extends Comparable<? super T>> T min(T[] $self) {
    return Arrays.stream($self)
        .min(Comparator.comparing(Function.identity()))
        .orElseThrow(NoSuchElementException::new);
  }

  /**
   * Find the first element matching the given predicate
   *
   * @param $self      the collection
   * @param predicate the predicate
   * @param <T>       the Collection elements type
   * @return the first element matching the given predicate or null
   */
  @Nullable
  public static <T> T find(Collection<T> $self, Predicate<T> predicate) {
    for (T e : $self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  /**
   * Find the first element matching the given predicate in an array
   *
   * @param $self the array
   * @param predicate the predicate
   * @param <T> the array elements type
   * @return the first element matching the given predicate or null
   */
  @Nullable
  public static <T> T find(T[] $self, Predicate<T> predicate) {
    for (T e : $self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  /**
   * Find the first element matching the given predicate in an int array
   *
   * @param $self the int array
   * @param predicate the predicate
   * @return the first element matching the given predicate or null
   */
  @Nullable
  public static Integer find(int[] $self, IntPredicate predicate) {
    for (int e : $self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  @Nullable
  public static Long find(long[] $self, LongPredicate predicate) {
    for (long e : $self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  @Nullable
  public static Float find(float[] $self, FloatPredicate predicate) {
    for (float e : $self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  @Nullable
  public static Double find(double[] $self, DoublePredicate predicate) {
    for (double e : $self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }

  @Nullable
  public static Character find(char[] $self, CharPredicate predicate) {
    for (char e : $self) {
      if (predicate.test(e)) return e;
    }
    return null;
  }


  /**
   * Count the number of times the provided predicate matched on an element of this collection
   * @param $self $self
   * @param predicate predicate
   * @return the number of times the provided predicate matched on an element of this collection
   * @param <T> the type of the list
   */
  public static <T> int count(Collection<T> $self, Predicate<T> predicate) {
    int count = 0;
    for (T e : $self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static <T> int count(T[] $self, Predicate<T> predicate) {
    int count = 0;
    for (T e : $self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static int count(int[] $self, IntPredicate predicate) {
    int count = 0;
    for (int e : $self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static int count(long[] $self, LongPredicate predicate) {
    int count = 0;
    for (long e : $self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static int count(float[] $self, FloatPredicate predicate) {
    int count = 0;
    for (float e : $self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static int count(double[] $self, DoublePredicate predicate) {
    int count = 0;
    for (double e : $self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  public static int count(char[] $self, CharPredicate predicate) {
    int count = 0;
    for (char e : $self) {
      if (predicate.test(e)) count++;
    }
    return count;
  }

  /**
   * Find all elements matching the given predicate
   *
   * @param $self      the collection
   * @param predicate the predicate
   * @param <T>       the Collection elements type
   * @return all elements matching the given predicate
   */
  public static <T> List<T> findAll(Collection<T> $self, Predicate<T> predicate) {
    List<T> list = new ArrayList<>($self.size());
    for (T e : $self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static <T> List<T> findAll(T[] $self, Predicate<T> predicate) {
    List<T> list = new ArrayList<>($self.length);
    for (T e : $self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static IntList findAll(int[] $self, IntPredicate predicate) {
    IntList list = new IntArrayList($self.length);
    for (int e : $self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static LongList findAll(long[] $self, LongPredicate predicate) {
    LongList list = new LongArrayList($self.length);
    for (long e : $self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static FloatList findAll(float[] $self, FloatPredicate predicate) {
    FloatList list = new FloatArrayList($self.length);
    for (float e : $self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static DoubleList findAll(double[] $self, DoublePredicate predicate) {
    DoubleList list = new DoubleArrayList($self.length);
    for (double e : $self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static CharList findAll(char[] $self, CharPredicate predicate) {
    CharList list = new CharArrayList($self.length);
    for (char e : $self) {
      if (predicate.test(e)) list.add(e);
    }
    return list;
  }

  public static <T> T reduce(Collection<T> $self, BiFunction<T, T, T> function) {
    Iterator<T> iterator = $self.iterator();
    if (!iterator.hasNext()) throw new NoSuchElementException();

    T result = iterator.next();
    while (iterator.hasNext()) {
      result = function.apply(result, iterator.next());
    }
    return result;
  }

  public static <T, U> U reduce(Collection<T> $self, U seed, BiFunction<U, T, U> function) {
    U u = seed;
    for (T t : $self) {
      u = function.apply(u, t);
    }
    return u;
  }

  /**
   * Converts a collection into a set
   *
   * @param $self the collection
   * @param <T>  the type of the collection
   * @return a set
   */
  public static <T> Set<T> toSet(Collection<T> $self) {
    Iterator<T> iterator = $self.iterator();
    Set<T> set = new HashSet<>($self.size());
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }

  public static IntSet toIntSet(Collection<?> $self) {
    Iterator<?> iterator = $self.iterator();
    IntSet set = new IntOpenHashSet($self.size());
    while (iterator.hasNext()) {
      set.add(((Number)iterator.next()).intValue());
    }
    return set;
  }

  public static LongSet toLongSet(Collection<?> $self) {
    Iterator<?> iterator = $self.iterator();
    LongSet set = new LongOpenHashSet($self.size());
    while (iterator.hasNext()) {
      set.add(((Number)iterator.next()).longValue());
    }
    return set;
  }

  public static FloatSet toFloatSet(Collection<?> $self) {
    Iterator<?> iterator = $self.iterator();
    FloatSet set = new FloatOpenHashSet($self.size());
    while (iterator.hasNext()) {
      set.add(((Number)iterator.next()).floatValue());
    }
    return set;
  }

  public static DoubleSet toDoubleSet(Collection<?> $self) {
    Iterator<?> iterator = $self.iterator();
    DoubleSet set = new DoubleOpenHashSet($self.size());
    while (iterator.hasNext()) {
      set.add(((Number)iterator.next()).doubleValue());
    }
    return set;
  }

  public static CharSet toCharSet(Collection<?> $self) {
    Iterator<?> iterator = $self.iterator();
    CharSet set = new CharOpenHashSet($self.size());
    while (iterator.hasNext()) {
      set.add((Character) iterator.next());
    }
    return set;
  }

  public static <T> Set<T> toSet(T[] $self) {
    Set<T> set = new HashSet<>($self.length);
    for (T e : $self) {
      set.add(e);
    }
    return set;
  }

  public static IntSet toSet(int[] $self) {
    IntSet set = new IntOpenHashSet($self.length);
    for (int e : $self) {
      set.add(e);
    }
    return set;
  }

  public static LongSet toSet(long[] $self) {
    LongSet set = new LongOpenHashSet($self.length);
    for (long e : $self) {
      set.add(e);
    }
    return set;
  }

  public static FloatSet toSet(float[] $self) {
    FloatSet set = new FloatOpenHashSet($self.length);
    for (float e : $self) {
      set.add(e);
    }
    return set;
  }

  public static DoubleSet toSet(double[] $self) {
    DoubleSet set = new DoubleOpenHashSet($self.length);
    for (double e : $self) {
      set.add(e);
    }
    return set;
  }

  public static CharSet toSet(char[] $self) {
    CharSet set = new CharOpenHashSet($self.length);
    for (char e : $self) {
      set.add(e);
    }
    return set;
  }

  /**
   * Converts a collection into a set
   *
   * @param $self the collection
   * @param <T>  the type of the collection
   * @return a set
   */
  public static <T> List<T> toList(Collection<T> $self) {
    Iterator<T> iterator = $self.iterator();
    List<T> set = new ArrayList<>($self.size());
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }

  /**
   * Converts an array to a List
   *
   * @param $self the array to convert
   * @param <T> the type of elements in the array
   * @return a new List containing all elements from the array
   */
  public static <T> List<T> toList(T[] $self) {
    List<T> set = new ArrayList<>($self.length);
    for (T e : $self) {
      set.add(e);
    }
    return set;
  }

  public static int getLastIndex(List<?> $self) {
    if ($self.isEmpty()) throw new NoSuchElementException();
    return $self.size() - 1;
  }

  public static <T> IntList toList(int[] $self) {
    IntList set = new IntArrayList($self.length);
    for (int e : $self) {
      set.add(e);
    }
    return set;
  }

  public static <T> LongList toList(long[] $self) {
    LongList set = new LongArrayList($self.length);
    for (long e : $self) {
      set.add(e);
    }
    return set;
  }

  public static <T> FloatList toList(float[] $self) {
    FloatList set = new FloatArrayList($self.length);
    for (float e : $self) {
      set.add(e);
    }
    return set;
  }

  public static <T> DoubleList toList(double[] $self) {
    DoubleList set = new DoubleArrayList($self.length);
    for (double e : $self) {
      set.add(e);
    }
    return set;
  }

  public static <T> CharList toList(char[] $self) {
    CharList set = new CharArrayList($self.length);
    for (char e : $self) {
      set.add(e);
    }
    return set;
  }

  public static IntList toIntList(Collection<?> $self) {
    Iterator<?> iterator = $self.iterator();
    IntList list = new IntArrayList($self.size());
    while (iterator.hasNext()) {
      list.add(((Number)iterator.next()).intValue());
    }
    return list;
  }

  public static LongList toLongList(Collection<?> $self) {
    Iterator<?> iterator = $self.iterator();
    LongList set = new LongArrayList($self.size());
    while (iterator.hasNext()) {
      set.add(((Number)iterator.next()).longValue());
    }
    return set;
  }

  public static FloatList toFloatList(Collection<?> $self) {
    Iterator<?> iterator = $self.iterator();
    FloatList list = new FloatArrayList($self.size());
    while (iterator.hasNext()) {
      list.add(((Number)iterator.next()).floatValue());
    }
    return list;
  }

  public static DoubleList toDoubleList(Collection<?> $self) {
    Iterator<?> iterator = $self.iterator();
    DoubleList list = new DoubleArrayList($self.size());
    while (iterator.hasNext()) {
      list.add(((Number)iterator.next()).doubleValue());
    }
    return list;
  }

  public static CharList toCharList(Collection<?> $self) {
    Iterator<?> iterator = $self.iterator();
    CharList list = new CharArrayList($self.size());
    while (iterator.hasNext()) {
      list.add((Character) iterator.next());
    }
    return list;
  }

  /**
   * Map the list into an IntList
   *
   * @param $self    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return an IntList
   */
  public static <T> IntList mapToInt(Collection<T> $self, ToIntFunction<T> lambda1) {
    IntList intList = new IntArrayList($self.size());
    for (T t : $self) {
      intList.add(lambda1.applyAsInt(t));
    }
    return intList;
  }

  /**
   * Map list elements to another type using the provided lambda
   *
   *
   * @param $self    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @param <U>     the type of the returned list
   * @return an IntList
   */
  public static <T, U> List<U> map(Collection<T> $self, Function<T, U> lambda1) {
    List<U> uList = new ArrayList<>($self.size());
    for (T t : $self) {
      uList.add(lambda1.apply(t));
    }
    return uList;
  }

  public static <T, U> List<U> map(T[] $self, Function<T, U> lambda1) {
    List<U> uList = new ArrayList<>($self.length);
    for (T t : $self) {
      uList.add(lambda1.apply(t));
    }
    return uList;
  }

  public static <T> IntList mapToInt(T[] $self, ToIntFunction<T> lambda1) {
    IntList intList = new IntArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      intList.add(lambda1.applyAsInt($self[i]));
    }
    return intList;
  }

  public static IntList mapToInt(long[] $self, ToIntFunction<Long> lambda1) {
    IntList intList = new IntArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      intList.add(lambda1.applyAsInt($self[i]));
    }
    return intList;
  }

  public static IntList mapToInt(float[] $self, ToIntFunction<Float> lambda1) {
    IntList intList = new IntArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      intList.add(lambda1.applyAsInt($self[i]));
    }
    return intList;
  }

  public static IntList mapToInt(double[] $self, ToIntFunction<Double> lambda1) {
    IntList intList = new IntArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      intList.add(lambda1.applyAsInt($self[i]));
    }
    return intList;
  }

  public static IntList mapToInt(char[] $self, ToIntFunction<Character> lambda1) {
    IntList intList = new IntArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      intList.add(lambda1.applyAsInt($self[i]));
    }
    return intList;
  }

  /**
   * Map the list into a LongList
   *
   * @param $self    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return a LongList
   */
  public static <T> LongList mapToLong(Collection<T> $self, ToLongFunction<T> lambda1) {
    LongList longList = new LongArrayList($self.size());
    for (T t : $self) {
      longList.add(lambda1.applyAsLong(t));
    }
    return longList;
  }

  public static <T> LongList mapToLong(T[] $self, ToLongFunction<T> lambda1) {
    LongList longList = new LongArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      longList.add(lambda1.applyAsLong($self[i]));
    }
    return longList;
  }

  public static LongList mapToLong(int[] $self, ToLongFunction<Integer> lambda1) {
    LongList longList = new LongArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      longList.add(lambda1.applyAsLong($self[i]));
    }
    return longList;
  }

  public static LongList mapToLong(float[] $self, ToLongFunction<Float> lambda1) {
    LongList longList = new LongArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      longList.add(lambda1.applyAsLong($self[i]));
    }
    return longList;
  }

  public static LongList mapToLong(double[] $self, ToLongFunction<Double> lambda1) {
    LongList longList = new LongArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      longList.add(lambda1.applyAsLong($self[i]));
    }
    return longList;
  }

  public static LongList mapToLong(char[] $self, ToLongFunction<Character> lambda1) {
    LongList longList = new LongArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      longList.add(lambda1.applyAsLong($self[i]));
    }
    return longList;
  }

  /**
   * Map the list into a FloatList
   *
   * @param $self    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return a FloatList
   */
  public static <T> FloatList mapToFloat(Collection<T> $self, ToFloatFunction<T> lambda1) {
    FloatList floatList = new FloatArrayList($self.size());
    for (T t : $self) {
      floatList.add(lambda1.applyAsFloat(t));
    }
    return floatList;
  }

  public static <T> FloatList mapToFloat(T[] $self, ToFloatFunction<T> lambda1) {
    FloatList floatList = new FloatArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsFloat($self[i]));
    }
    return floatList;
  }

  public static FloatList mapToFloat(int[] $self, ToFloatFunction<Integer> lambda1) {
    FloatList floatList = new FloatArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsFloat($self[i]));
    }
    return floatList;
  }

  public static FloatList mapToFloat(long[] $self, ToFloatFunction<Long> lambda1) {
    FloatList floatList = new FloatArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsFloat($self[i]));
    }
    return floatList;
  }

  public static FloatList mapToFloat(double[] $self, ToFloatFunction<Double> lambda1) {
    FloatList floatList = new FloatArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsFloat($self[i]));
    }
    return floatList;
  }

  public static FloatList mapToFloat(char[] $self, ToFloatFunction<Character> lambda1) {
    FloatList floatList = new FloatArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsFloat($self[i]));
    }
    return floatList;
  }

  /**
   * Map the list into a DoubleList
   *
   * @param $self    the list
   * @param lambda1 the mapping function
   * @param <T>     the type of the list
   * @return a DoubleList
   */
  public static <T> DoubleList mapToDouble(List<T> $self, ToDoubleFunction<T> lambda1) {
    DoubleList floatList = new DoubleArrayList($self.size());
    for (T t : $self) {
      floatList.add(lambda1.applyAsDouble(t));
    }
    return floatList;
  }

  public static <T> DoubleList mapToDouble(T[] $self, ToDoubleFunction<T> lambda1) {
    DoubleList floatList = new DoubleArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsDouble($self[i]));
    }
    return floatList;
  }

  public static <T> DoubleList mapToDouble(int[] $self, ToDoubleFunction<Integer> lambda1) {
    DoubleList floatList = new DoubleArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsDouble($self[i]));
    }
    return floatList;
  }

  public static <T> DoubleList mapToDouble(long[] $self, ToDoubleFunction<Long> lambda1) {
    DoubleList floatList = new DoubleArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsDouble($self[i]));
    }
    return floatList;
  }

  public static <T> DoubleList mapToDouble(float[] $self, ToDoubleFunction<Float> lambda1) {
    DoubleList floatList = new DoubleArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsDouble($self[i]));
    }
    return floatList;
  }

  public static <T> DoubleList mapToDouble(char[] $self, ToDoubleFunction<Character> lambda1) {
    DoubleList floatList = new DoubleArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsDouble($self[i]));
    }
    return floatList;
  }

  public static <T> CharList mapToChar(List<T> $self, ToCharFunction<T> lambda1) {
    CharList floatList = new CharArrayList($self.size());
    for (T t : $self) {
      floatList.add(lambda1.applyAsChar(t));
    }
    return floatList;
  }

  public static <T> CharList mapToChar(T[] $self, ToCharFunction<T> lambda1) {
    CharList floatList = new CharArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsChar($self[i]));
    }
    return floatList;
  }

  public static <T> CharList mapToChar(int[] $self, ToCharFunction<Integer> lambda1) {
    CharList floatList = new CharArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsChar($self[i]));
    }
    return floatList;
  }

  public static <T> CharList mapToChar(long[] $self, ToCharFunction<Long> lambda1) {
    CharList floatList = new CharArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsChar($self[i]));
    }
    return floatList;
  }

  public static <T> CharList mapToChar(float[] $self, ToCharFunction<Float> lambda1) {
    CharList floatList = new CharArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsChar($self[i]));
    }
    return floatList;
  }

  public static <T> CharList mapToChar(char[] $self, ToCharFunction<Character> lambda1) {
    CharList floatList = new CharArrayList($self.length);
    for (int i = 0; i < $self.length; i++) {
      floatList.add(lambda1.applyAsChar($self[i]));
    }
    return floatList;
  }

  public static String join(Collection<?> $self) {
    return join($self, "");
  }

  public static <T> String join(T[] $self) {
    return join($self, "");
  }

  public static <T> String join(T[] $self, String separator) {
    return Arrays.stream($self)
        .map(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T> String join(int[] $self) {
    return join($self, "");
  }

  public static <T> String join(int[] $self, String separator) {
    return Arrays.stream($self)
        .mapToObj(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T> String join(long[] $self) {
    return join($self, "");
  }

  public static <T> String join(long[] $self, String separator) {
    return Arrays.stream($self)
        .mapToObj(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T> String join(float[] $self) {
    return join($self, "");
  }

  public static <T> String join(float[] $self, String separator) {
    return toList($self).stream()
        .map(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T> String join(double[] $self) {
    return join($self, "");
  }

  public static <T> String join(double[] $self, String separator) {
    return Arrays.stream($self)
        .mapToObj(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  public static <T> String join(char[] $self) {
    return join($self, "");
  }

  public static <T> String join(char[] $self, String separator) {
    return toList($self).stream()
        .map(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  /**
   * Joins the elements of the collection to a string
   *
   * @param $self      the collection
   * @param separator the separator
   * @return a string
   */
  public static String join(Collection<?> $self, String separator) {
    return $self.stream()
        .map(String::valueOf)
        .collect(Collectors.joining(separator));
  }

  /**
   * Sort the list using the given keyExtractor as the comparator
   *
   * @param $self         the list to sort
   * @param keyExtractor the key extractor
   * @param <T>          the type of the list
   * @param <U>          the comparable type
   */
  public static <T, U extends Comparable<U>> void sortBy(List<T> $self, Function<T, U> keyExtractor) {
    $self.sort(Comparator.comparing(keyExtractor));
  }

  public static <T extends Comparable<? super T>> void sort(List<T> $self) {
    Object[] a = $self.toArray();
    $self.sort(Comparator.naturalOrder());
  }

  public static <T, U extends Comparable<U>> void sortBy(T[] $self, Function<T, U> keyExtractor) {
    Arrays.sort($self, Comparator.comparing(keyExtractor));
  }

  public static <T, U extends Comparable<U>> void sort(T[] $self) {
    Arrays.sort($self);
  }

  public static void sort(int[] $self) {
    Arrays.sort($self);
  }

  public static void sort(long[] $self) {
    Arrays.sort($self);
  }

  public static void sort(float[] $self) {
    Arrays.sort($self);
  }

  public static void sort(double[] $self) {
    Arrays.sort($self);
  }

  public static void sort(char[] $self) {
    Arrays.sort($self);
  }


  /**
   * Reverse a list
   *
   * @param $self the list
   */
  public static void reverse(List<?> $self) {
    Collections.reverse($self);
  }

  public static <T> void reverse(T[] $self) {
    for (int i = 0; i < $self.length / 2; i++) {
      swap($self, i, $self.length - 1 - i);
    }
  }

  public static <T> void reverse(int[] $self) {
    for (int i = 0; i < $self.length / 2; i++) {
      swap($self, i, $self.length - 1 - i);
    }
  }

  public static <T> void reverse(long[] $self) {
    for (int i = 0; i < $self.length / 2; i++) {
      swap($self, i, $self.length - 1 - i);
    }
  }

  public static <T> void reverse(float[] $self) {
    for (int i = 0; i < $self.length / 2; i++) {
      swap($self, i, $self.length - 1 - i);
    }
  }

  public static <T> void reverse(double[] $self) {
    for (int i = 0; i < $self.length / 2; i++) {
      swap($self, i, $self.length - 1 - i);
    }
  }

  public static <T> void reverse(char[] $self) {
    for (int i = 0; i < $self.length / 2; i++) {
      swap($self, i, $self.length - 1 - i);
    }
  }

  /**
   * Add an element to the collection
   *
   * @param $self  the list
   * @param value the value to add
   * @param <T>   the type of the list
   * @return whether the value was added or not
   */
  public static <T> Collection<T> leftShift(Collection<T> $self, T value) {
    $self.add(value);
    return $self;
  }

  /**
   * Add an element to the list
   *
   * @param $self  the list
   * @param value the value to add
   * @param <T>   the type of the list
   * @return whether the value was added or not
   */
  public static <T> List<T> leftShift(List<T> $self, T value) {
    $self.add(value);
    return $self;
  }

  /**
   * Add an element to the set
   *
   * @param $self  the list
   * @param value the value to add
   * @param <T>   the type of the list
   * @return whether the value was added or not
   */
  public static <T> Set<T> leftShift(Set<T> $self, T value) {
    $self.add(value);
    return $self;
  }

  /**
   * Add an element to the set
   *
   * @param $self  the list
   * @param value the value to add
   * @param <T>   the type of the list
   * @return whether the value was added or not
   */
  public static <T> Queue<T> leftShift(Queue<T> $self, T value) {
    $self.add(value);
    return $self;
  }

  public static <T, U> Map<T, U> leftShift(Map<T, U> $self, Map<T, U> other) {
    $self.putAll(other);
    return $self;
  }

  /**
   * Get the last element of the list. This method will throw an exception if the list is empty
   *
   * @param $self the list
   * @param <T>  the type of the list
   * @return the last element of the list
   */
  public static <T> T getLast(List<T> $self) {
    return $self.get($self.size() - 1);
  }

  public static <T> T getLast(T[] $self) {
    return $self[$self.length - 1];
  }

  public static int getLast(int[] $self) {
    return $self[$self.length - 1];
  }

  public static long getLast(long[] $self) {
    return $self[$self.length - 1];
  }

  public static float getLast(float[] $self) {
    return $self[$self.length - 1];
  }

  public static double getLast(double[] $self) {
    return $self[$self.length - 1];
  }

  public static char getLast(char[] $self) {
    return $self[$self.length - 1];
  }

  /**
   * Get the last element of the list or null if the list is empty
   *
   * @param $self the list
   * @param <T>  the type of the list
   * @return the last element of the list
   */
  public static <T> T getLastOrNull(List<T> $self) {
    if ($self.isEmpty()) return null;
    return $self.get($self.size() - 1);
  }

  @Nullable
  public static <T> T getLastOrNull(T[] $self) {
    return $self.length > 0 ? $self[$self.length - 1]: null;
  }

  @Nullable
  public static Integer getLastOrNull(int[] $self) {
    return $self.length > 0 ? $self[$self.length - 1]: null;
  }

  @Nullable
  public static Long getLastOrNull(long[] $self) {
    return $self.length > 0 ? $self[$self.length - 1]: null;
  }

  @Nullable
  public static Float getLastOrNull(float[] $self) {
    return $self.length > 0 ? $self[$self.length - 1]: null;
  }

  @Nullable
  public static Double getLastOrNull(double[] $self) {
    return $self.length > 0 ? $self[$self.length - 1]: null;
  }

  @Nullable
  public static Character getLastOrNull(char[] $self) {
    return $self.length > 0 ? $self[$self.length - 1]: null;
  }

  /**
   * Get the first element of the list. This method will throw an exception if the list is empty
   *
   * @param $self the list
   * @param <T>  the type of the list
   * @return the first element of the list
   */
  public static <T> T getFirst(List<T> $self) {
    return $self.get(0);
  }

  public static <T> T getFirst(T[] $self) {
    return $self[0];
  }

  public static int getFirst(int[] $self) {
    return $self[0];
  }

  public static long getFirst(long[] $self) {
    return $self[0];
  }

  public static float getFirst(float[] $self) {
    return $self[0];
  }

  public static double getFirst(double[] $self) {
    return $self[0];
  }

  public static char getFirst(char[] $self) {
    return $self[0];
  }

  /**
   * Sets the last element of the list. This method wil throw an exception if the list is empty
   *
   * @param $self  the list
   * @param value the value to set
   * @param <T>   the type of the list
   */
  public static <T> void setLast(List<T> $self, T value) {
    $self.set($self.size() - 1, value);
  }

  public static <T> void setLast(T[] $self, T value) {
    $self[$self.length - 1] = value;
  }

  public static void setLast(int[] $self, int value) {
    $self[$self.length - 1] = value;
  }

  public static void setLast(long[] $self, long value) {
    $self[$self.length - 1] = value;
  }

  public static void setLast(float[] $self, float value) {
    $self[$self.length - 1] = value;
  }

  public static void setLast(double[] $self, double value) {
    $self[$self.length - 1] = value;
  }

  public static void setLast(char[] $self, char value) {
    $self[$self.length - 1] = value;
  }

  /**
   * Get the element at the specified index
   *
   * @param $self  the list
   * @param index the index
   * @param <T>   the type of the list
   * @return the element at the specified index
   */
  public static <T> T getAt(List<T> $self, int index) {
    return $self.get(index);
  }

  // getAt range

  /**
   * Get the elements at the specified indexes from the range. The order of elements returned respects the order of the range
   *
   * @param $self  the list
   * @param range the range
   * @param <T>   the type of the list
   * @return the elements at the specified indexes from the ranges
   */
  public static <T> List<T> getAt(List<T> $self, IntRange range) {
    List<T> subList = new ArrayList<>($self.size());
    IntIterator iterator = range.iterator();
    while (iterator.hasNext()) subList.add($self.get(iterator.nextInt()));
    return subList;
  }


  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param $self  the list
   * @param index the index
   * @return the element at the specified index
   */
  @Nullable
  public static <T> T getAtSafe(List<T> $self, int index) {
    return index >= 0 && index < $self.size() ? $self.get(index) : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param $self  the array
   * @param index the index
   * @return the element at the specified index
   */
  @Nullable
  public static <T> T getAtSafe(T[] $self, int index) {
    return index >= 0 && index < $self.length ? $self[index] : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param $self  the array
   * @param index the index
   * @return the element at the specified index
   */
  @Nullable
  public static Integer getAtSafe(int[] $self, int index) {
    return index >= 0 && index < $self.length ? $self[index] : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param $self  the array
   * @param index the index
   * @return the element at the specified index
   */
  @Nullable
  public static Long getAtSafe(long[] $self, int index) {
    return index >= 0 && index < $self.length ? $self[index] : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param $self  the array
   * @param index the index
   * @return the element at the specified index
   */
  @Nullable
  public static Float getAtSafe(float[] $self, int index) {
    return index >= 0 && index < $self.length ? $self[index] : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param $self  the array
   * @param index the index
   * @return the element at the specified index
   */
  @Nullable
  public static Double getAtSafe(double[] $self, int index) {
    return index >= 0 && index < $self.length ? $self[index] : null;
  }

  /**
   * Get the element at the specified index or null, if the index is out of bounds
   *
   * @param $self  the array
   * @param index the index
   * @return the element at the specified index
   */
  @Nullable
  public static Character getAtSafe(char[] $self, int index) {
    return index >= 0 && index < $self.length ? $self[index] : null;
  }

  // putAt

  /**
   * Set the element at the specified index
   *
   * @param $self  the list
   * @param index the index
   * @param value the value to set
   * @param <T>   the type of the list
   */
  public static <T> void putAt(List<T> $self, int index, T value) {
    $self.set(index, value);
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param $self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(int[] $self, int index, int value) {
    if (index >= 0 && index < $self.length) {
      $self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param $self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(float[] $self, int index, float value) {
    if (index >= 0 && index < $self.length) {
      $self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param $self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(double[] $self, int index, double value) {
    if (index >= 0 && index < $self.length) {
      $self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param $self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(long[] $self, int index, long value) {
    if (index >= 0 && index < $self.length) {
      $self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param $self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(char[] $self, int index, char value) {
    if (index >= 0 && index < $self.length) {
      $self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param $self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(short[] $self, int index, short value) {
    if (index >= 0 && index < $self.length) {
      $self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param $self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(byte[] $self, int index, byte value) {
    if (index >= 0 && index < $self.length) {
      $self[index] = value;
    }
  }

  /**
   * Set the element at the specified index, if the index is within the array's bounds
   *
   * @param $self  the list
   * @param index the index
   * @param value the value to set
   */
  public static void putAtSafe(boolean[] $self, int index, boolean value) {
    if (index >= 0 && index < $self.length) {
      $self[index] = value;
    }
  }

  public static <T, U> Map<T, U> withDefault(Map<T, U> $self, Function<Object, U> defaultFunction) {
    return withDefault($self, false, defaultFunction);
  }

  public static <T, U> Map<T, U> withDefault(Map<T, U> $self, boolean insert, Function<Object, U> defaultFunction) {
    return MapWithDefault.newInstance($self, defaultFunction, insert);
  }

  public static boolean isEmpty(Object[] $self) {
    return $self.length == 0;
  }

  public static boolean isEmpty(int[] $self) {
    return $self.length == 0;
  }

  public static boolean isEmpty(long[] $self) {
    return $self.length == 0;
  }

  public static boolean isEmpty(float[] $self) {
    return $self.length == 0;
  }

  public static boolean isEmpty(double[] $self) {
    return $self.length == 0;
  }

  public static boolean isEmpty(char[] $self) {
    return $self.length == 0;
  }

  public static boolean isEmpty(boolean[] $self) {
    return $self.length == 0;
  }

  public static boolean isEmpty(short[] $self) {
    return $self.length == 0;
  }

  public static boolean isEmpty(byte[] $self) {
    return $self.length == 0;
  }

  /**
   * Get the element at the specified key
   *
   * @param $self  the map
   * @param key the key
   * @param <T>   the type of the map values
   * @return the element at the specified key
   */
  public static <T> T getAt(Map<?, T> $self, Object key) {
    return $self.get(key);
  }

  /**
   * Set the element at the specified key
   *
   * @param $self  the map
   * @param key the key
   * @param value the value
   * @param <T>   the type of the map keys
   * @param <U>   the type of the map values
   * @return the element at the specified index
   */
  public static <T, U> U putAt(Map<T, U> $self, T key, U value) {
    return $self.put(key, value);
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param $self the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static <T> Object[] plus(T[] $self, T[] b) {
    Object[] sum = new Object[$self.length + b.length];
    System.arraycopy($self, 0, sum, 0, $self.length);
    System.arraycopy(b, 0, sum, $self.length, b.length);
    return sum;
  }

  public static int[] plus(int[] $self, int[] b) {
    int[] sum = new int[$self.length + b.length];
    System.arraycopy($self, 0, sum, 0, $self.length);
    System.arraycopy(b, 0, sum, $self.length, b.length);
    return sum;
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param $self the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static long[] plus(long[] $self, long[] b) {
    long[] sum = new long[$self.length + b.length];
    System.arraycopy($self, 0, sum, 0, $self.length);
    System.arraycopy(b, 0, sum, $self.length, b.length);
    return sum;
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param $self the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static float[] plus(float[] $self, float[] b) {
    float[] sum = new float[$self.length + b.length];
    System.arraycopy($self, 0, sum, 0, $self.length);
    System.arraycopy(b, 0, sum, $self.length, b.length);
    return sum;
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param $self the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static double[] plus(double[] $self, double[] b) {
    double[] sum = new double[$self.length + b.length];
    System.arraycopy($self, 0, sum, 0, $self.length);
    System.arraycopy(b, 0, sum, $self.length, b.length);
    return sum;
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param $self the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static boolean[] plus(boolean[] $self, boolean[] b) {
    boolean[] sum = new boolean[$self.length + b.length];
    System.arraycopy($self, 0, sum, 0, $self.length);
    System.arraycopy(b, 0, sum, $self.length, b.length);
    return sum;
  }

  /**
   * Returns a new array containing the content of the first one then the content of the second
   *
   * @param $self the first array
   * @param b the second array
   * @return a new array containing the content of the first one then the content of the second
   */
  public static char[] plus(char[] $self, char[] b) {
    char[] sum = new char[$self.length + b.length];
    System.arraycopy($self, 0, sum, 0, $self.length);
    System.arraycopy(b, 0, sum, $self.length, b.length);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first one then the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new array containing the content of the first one then the content of the second
   */
  public static <T> List<T> plus(List<T> $self, Collection<T> b) {
    List<T> sum = new ArrayList<>($self.size() + b.size());
    sum.addAll($self);
    sum.addAll(b);
    return sum;
  }

  /**
   * Returns a new List containing the content of the List then the content of the array
   *
   * @param $self the List
   * @param b the array
   * @return a new List containing the content of the List then the content of the array
   */
  public static <T> List<T> plus(List<T> $self, T[] b) {
    List<T> sum = new ArrayList<>($self.size() + b.length);
    sum.addAll($self);
    for (T t : b) {
      sum.add(t);
    }
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first one then the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new array containing the content of the first one then the content of the second
   */
  public static <T> Set<T> plus(Set<T> $self, Collection<T> b) {
    Set<T> sum = new HashSet<>($self.size() + b.size());
    sum.addAll($self);
    sum.addAll(b);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first one then the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new array containing the content of the first one then the content of the second
   */
  public static <T> Set<T> plus(Set<T> $self, T[] b) {
    Set<T> sum = new HashSet<>($self.size() + b.length);
    sum.addAll($self);
    for (T t : b) {
      sum.add(t);
    }
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first one then the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new array containing the content of the first one then the content of the second
   */
  public static <T> Queue<T> plus(Queue<T> $self, Collection<T> b) {
    Queue<T> sum = new LinkedList<>($self);
    sum.addAll(b);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first one then the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new array containing the content of the first one then the content of the second
   */
  public static <T> Queue<T> plus(Queue<T> $self, T[] b) {
    Queue<T> sum = new LinkedList<>($self);
    for (T t : b) {
      sum.add(t);
    }
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first without the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new collection containing the content of the first without the content of the second
   */
  public static <T> List<T> minus(List<T> $self, Collection<T> b) {
    List<T> sum = new ArrayList<>($self);
    sum.removeAll(b);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first without the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new collection containing the content of the first without the content of the second
   */
  public static <T> List<T> minus(List<T> $self, T[] b) {
    List<T> sum = new ArrayList<>($self);
    for (T t : b) {
      sum.remove(t);
    }
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first without the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new collection containing the content of the first without the content of the second
   */
  public static <T> Set<T> minus(Set<T> $self, Collection<T> b) {
    Set<T> sum = new HashSet<>($self);
    sum.removeAll(b);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first without the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new collection containing the content of the first without the content of the second
   */
  public static <T> Set<T> minus(Set<T> $self, T[] b) {
    Set<T> sum = new HashSet<>($self);
    for (T t : b) {
      sum.remove(t);
    }
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first without the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new collection containing the content of the first without the content of the second
   */
  public static <T> Queue<T> minus(Queue<T> $self, Collection<T> b) {
    Queue<T> sum = new LinkedList<>($self);
    sum.removeAll(b);
    return sum;
  }

  /**
   * Returns a new collection containing the content of the first without the content of the second
   *
   * @param $self the first collection
   * @param b the second collection
   * @return a new collection containing the content of the first without the content of the second
   */
  public static <T> Queue<T> minus(Queue<T> $self, T[] b) {
    Queue<T> sum = new LinkedList<>($self);
    for (T t : b) {
      sum.remove(t);
    }
    return sum;
  }

  public static <T, U> Map<T, U> plus(Map<T, U> $self, Map<T, U> b) {
    Map<T, U> map = new HashMap<>($self);
    map.putAll(b);
    return map;
  }

  public static <T, U> Map<T, U> minus(Map<T, U> $self, Map<T, U> b) {
    Map<T, U> map = new HashMap<>($self);
    b.forEach(map::remove);
    return map;
  }

  /**
   * Returns an unmodifiable view of the given collection
   * @param $self the collection
   * @return an unmodifiable view of the given collection
   * @param <T> the generic type
   */
  public static <T> List<T> asUnmodifiable(List<T> $self) {
    return Collections.unmodifiableList($self);
  }

  public static <T> Set<T> asUnmodifiable(Set<T> $self) {
    return Collections.unmodifiableSet($self);
  }

  public static <K,V> Map<K,V> asUnmodifiable(Map<K,V> $self) {
    return Collections.unmodifiableMap($self);
  }

  public static <T> List<T> asUnmodifiable(T[] $self) {
    return Collections.unmodifiableList(Arrays.asList($self));
  }

  public static <T, U> Map<U, List<T>> groupBy(Collection<T> $self, Function<T, U> keyExtractor) {
    return $self.stream()
        .collect(Collectors.groupingBy(keyExtractor));
  }

  /**
   * Groups elements of an array by a key extractor function
   * 
   * @param $self the array to group
   * @param keyExtractor the function extracting the key
   * @param <T> the type of array elements
   * @param <U> the type of the keys
   * @return a map where keys are the extracted values and values are lists of elements
   */
  public static <T, U> Map<U, List<T>> groupBy(T[] $self, Function<T, U> keyExtractor) {
    return Arrays.stream($self)
        .collect(Collectors.groupingBy(keyExtractor));
  }
  
  /**
   * Applies a function to each element in a collection and sums the results
   * 
   * @param $self the collection
   * @param mapper the function to apply to each element
   * @param <T> the type of elements in the collection
   * @return the sum of all mapped values
   */
  public static <T> int sumBy(Collection<T> $self, ToIntFunction<T> mapper) {
    int sum = 0;
    for (T item : $self) {
      sum += mapper.applyAsInt(item);
    }
    return sum;
  }
  
  /**
   * Applies a function to each element in an array and sums the results
   * 
   * @param $self the array
   * @param mapper the function to apply to each element
   * @param <T> the type of elements in the array
   * @return the sum of all mapped values
   */
  public static <T> int sumBy(T[] $self, ToIntFunction<T> mapper) {
    int sum = 0;
    for (T item : $self) {
      sum += mapper.applyAsInt(item);
    }
    return sum;
  }
  
  /**
   * Applies a function to each element in a collection and sums the results as long values
   * 
   * @param $self the collection
   * @param mapper the function to apply to each element
   * @param <T> the type of elements in the collection
   * @return the sum of all mapped values
   */
  public static <T> long sumByLong(Collection<T> $self, ToLongFunction<T> mapper) {
    long sum = 0;
    for (T item : $self) {
      sum += mapper.applyAsLong(item);
    }
    return sum;
  }
  
  /**
   * Applies a function to each element in an array and sums the results as long values
   * 
   * @param $self the array
   * @param mapper the function to apply to each element
   * @param <T> the type of elements in the array
   * @return the sum of all mapped values
   */
  public static <T> long sumByLong(T[] $self, ToLongFunction<T> mapper) {
    long sum = 0;
    for (T item : $self) {
      sum += mapper.applyAsLong(item);
    }
    return sum;
  }
  
  /**
   * Applies a function to each element in a collection and sums the results as double values
   * 
   * @param $self the collection
   * @param mapper the function to apply to each element
   * @param <T> the type of elements in the collection
   * @return the sum of all mapped values
   */
  public static <T> double sumByDouble(Collection<T> $self, ToDoubleFunction<T> mapper) {
    double sum = 0;
    for (T item : $self) {
      sum += mapper.applyAsDouble(item);
    }
    return sum;
  }
  
  /**
   * Applies a function to each element in an array and sums the results as double values
   * 
   * @param $self the array
   * @param mapper the function to apply to each element
   * @param <T> the type of elements in the array
   * @return the sum of all mapped values
   */
  public static <T> double sumByDouble(T[] $self, ToDoubleFunction<T> mapper) {
    double sum = 0;
    for (T item : $self) {
      sum += mapper.applyAsDouble(item);
    }
    return sum;
  }

  /**
   * Takes the first n elements from the collection
   * 
   * @param $self the collection
   * @param n the number of elements to take
   * @param <T> the type of elements in the collection
   * @return a new list containing the first n elements
   */
  public static <T> List<T> take(Collection<T> $self, int n) {
    if (n <= 0) return new ArrayList<>();
    List<T> result = new ArrayList<>(Math.min(n, $self.size()));
    int count = 0;
    for (T item : $self) {
      result.add(item);
      if (++count >= n) break;
    }
    return result;
  }
  
  /**
   * Takes the first n elements from the array
   * 
   * @param $self the array
   * @param n the number of elements to take
   * @param <T> the type of elements in the array
   * @return a new list containing the first n elements
   */
  /**
   * Returns a list containing the first n elements from the original array
   * 
   * @param $self the array
   * @param n the number of elements to take
   * @param <T> the type of elements in the array
   * @return a new list containing the first n elements
   */
  public static <T> List<T> take(T[] $self, int n) {
    if (n <= 0) return new ArrayList<>();
    List<T> result = new ArrayList<>(Math.min(n, $self.length));
    for (int i = 0; i < Math.min(n, $self.length); i++) {
      result.add($self[i]);
    }
    return result;
  }
  
  /**
   * Takes the last n elements from the collection
   * 
   * @param $self the collection
   * @param n the number of elements to take
   * @param <T> the type of elements in the collection
   * @return a new list containing the last n elements
   */
  /**
   * Returns a list containing the last n elements from the original list
   * 
   * @param $self the list
   * @param n the number of elements to take
   * @param <T> the type of elements in the list
   * @return a new list containing the last n elements
   */
  public static <T> List<T> takeLast(List<T> $self, int n) {
    if (n <= 0) return new ArrayList<>();
    int start = Math.max(0, $self.size() - n);
    return new ArrayList<>($self.subList(start, $self.size()));
  }
  
  /**
   * Takes the last n elements from the array
   * 
   * @param $self the array
   * @param n the number of elements to take
   * @param <T> the type of elements in the array
   * @return a new list containing the last n elements
   */
  /**
   * Returns a list containing the last n elements from the original array
   * 
   * @param $self the array
   * @param n the number of elements to take
   * @param <T> the type of elements in the array
   * @return a new list containing the last n elements
   */
  public static <T> List<T> takeLast(T[] $self, int n) {
    if (n <= 0) return new ArrayList<>();
    int start = Math.max(0, $self.length - n);
    List<T> result = new ArrayList<>(Math.min(n, $self.length));
    for (int i = start; i < $self.length; i++) {
      result.add($self[i]);
    }
    return result;
  }
  
  /**
   * Takes elements from the collection while the predicate returns true
   * 
   * @param $self the collection
   * @param predicate the predicate to test elements
   * @param <T> the type of elements in the collection
   * @return a new list containing elements until the first element that doesn't satisfy the predicate
   */
  public static <T> List<T> takeWhile(Collection<T> $self, Predicate<T> predicate) {
    List<T> result = new ArrayList<>();
    for (T item : $self) {
      if (!predicate.test(item)) break;
      result.add(item);
    }
    return result;
  }
  
  /**
   * Takes elements from the array while the predicate returns true
   * 
   * @param $self the array
   * @param predicate the predicate to test elements
   * @param <T> the type of elements in the array
   * @return a new list containing elements until the first element that doesn't satisfy the predicate
   */
  public static <T> List<T> takeWhile(T[] $self, Predicate<T> predicate) {
    List<T> result = new ArrayList<>();
    for (T item : $self) {
      if (!predicate.test(item)) break;
      result.add(item);
    }
    return result;
  }
  
  /**
   * Drops the first n elements from the collection
   * 
   * @param $self the collection
   * @param n the number of elements to drop
   * @param <T> the type of elements in the collection
   * @return a new list containing the remaining elements after dropping n elements
   */
  public static <T> List<T> drop(Collection<T> $self, int n) {
    if (n <= 0) return new ArrayList<>($self);
    List<T> result = new ArrayList<>(Math.max(0, $self.size() - n));
    int count = 0;
    for (T item : $self) {
      if (count++ >= n) {
        result.add(item);
      }
    }
    return result;
  }
  
  /**
   * Drops the first n elements from the array
   * 
   * @param $self the array
   * @param n the number of elements to drop
   * @param <T> the type of elements in the array
   * @return a new list containing the remaining elements after dropping n elements
   */
  /**
   * Returns a list containing all elements except the first n elements from the array
   * 
   * @param $self the array
   * @param n the number of elements to drop
   * @param <T> the type of elements in the array
   * @return a new list containing all elements except the first n elements
   */
  public static <T> List<T> drop(T[] $self, int n) {
    if (n <= 0) return Arrays.asList($self);
    List<T> result = new ArrayList<>(Math.max(0, $self.length - n));
    for (int i = n; i < $self.length; i++) {
      result.add($self[i]);
    }
    return result;
  }
  
  /**
   * Drops the last n elements from the collection
   * 
   * @param $self the collection
   * @param n the number of elements to drop
   * @param <T> the type of elements in the collection
   * @return a new list containing the remaining elements after dropping the last n elements
   */
  /**
   * Drops the last n elements from the list
   * 
   * @param $self the list
   * @param n the number of elements to drop
   * @param <T> the type of elements in the list
   * @return a new list containing the remaining elements after dropping the last n elements
   */
  public static <T> List<T> dropLast(List<T> $self, int n) {
    if (n <= 0) return new ArrayList<>($self);
    int newSize = Math.max(0, $self.size() - n);
    return new ArrayList<>($self.subList(0, newSize));
  }
  
  /**
   * Drops the last n elements from the array
   * 
   * @param $self the array
   * @param n the number of elements to drop
   * @param <T> the type of elements in the array
   * @return a new list containing the remaining elements after dropping the last n elements
   */
  public static <T> List<T> dropLast(T[] $self, int n) {
    if (n <= 0) return Arrays.asList($self);
    int newSize = Math.max(0, $self.length - n);
    List<T> result = new ArrayList<>(newSize);
    for (int i = 0; i < newSize; i++) {
      result.add($self[i]);
    }
    return result;
  }
  
  /**
   * Drops elements from the collection while the predicate returns true
   * 
   * @param $self the collection
   * @param predicate the predicate to test elements
   * @param <T> the type of elements in the collection
   * @return a new list containing elements after the first element that doesn't satisfy the predicate
   */
  public static <T> List<T> dropWhile(Collection<T> $self, Predicate<T> predicate) {
    List<T> result = new ArrayList<>();
    boolean dropping = true;
    for (T item : $self) {
      if (dropping && !predicate.test(item)) {
        dropping = false;
      }
      if (!dropping) {
        result.add(item);
      }
    }
    return result;
  }
  
  /**
   * Drops elements from the array while the predicate returns true
   * 
   * @param $self the array
   * @param predicate the predicate to test elements
   * @param <T> the type of elements in the array
   * @return a new list containing elements after the first element that doesn't satisfy the predicate
   */
  public static <T> List<T> dropWhile(T[] $self, Predicate<T> predicate) {
    List<T> result = new ArrayList<>();
    boolean dropping = true;
    for (T item : $self) {
      if (dropping && !predicate.test(item)) {
        dropping = false;
      }
      if (!dropping) {
        result.add(item);
      }
    }
    return result;
  }
  
  /**
   * Accumulates value starting with an initial value and applying an operation from left to right
   * 
   * @param $self the collection
   * @param initial the initial value
   * @param operation the operation to apply
   * @param <T> the type of elements in the collection
   * @param <R> the type of the accumulated result
   * @return the accumulated result
   */
  public static <T, R> R fold(Collection<T> $self, R initial, BiFunction<R, T, R> operation) {
    R result = initial;
    for (T item : $self) {
      result = operation.apply(result, item);
    }
    return result;
  }
  
  /**
   * Accumulates value starting with an initial value and applying an operation from left to right
   * 
   * @param $self the array
   * @param initial the initial value
   * @param operation the operation to apply
   * @param <T> the type of elements in the array
   * @param <R> the type of the accumulated result
   * @return the accumulated result
   */
  public static <T, R> R fold(T[] $self, R initial, BiFunction<R, T, R> operation) {
    R result = initial;
    for (T item : $self) {
      result = operation.apply(result, item);
    }
    return result;
  }
  
  /**
   * Accumulates value starting with an initial value and applying an operation from left to right
   * 
   * @param $self the array
   * @param initial the initial value
   * @param operation the operation to apply
   * @return the accumulated result
   */
  public static int fold(int[] $self, int initial, IntBinaryOperator operation) {
    int result = initial;
    for (int item : $self) {
      result = operation.applyAsInt(result, item);
    }
    return result;
  }
  
  /**
   * Accumulates value starting with an initial value and applying an operation from left to right
   *
   * @param $self the array
   * @param initial the initial value
   * @param operation the operation to apply
   * @return the accumulated result
   */
  public static long fold(long[] $self, long initial, LongBinaryOperator operation) {
    long result = initial;
    for (long item : $self) {
      result = operation.applyAsLong(result, item);
    }
    return result;
  }

  /**
   * Accumulates value starting with an initial value and applying an operation from left to right
   *
   * @param $self the array
   * @param initial the initial value
   * @param operation the operation to apply
   * @return the accumulated result
   */
  public static double fold(double[] $self, double initial, DoubleBinaryOperator operation) {
    double result = initial;
    for (double item : $self) {
      result = operation.applyAsDouble(result, item);
    }
    return result;
  }

  /**
   * Find the last element that matches the predicate
   * 
   * @param $self the collection
   * @param predicate the predicate to match
   * @param <T> the type of elements in the collection
   * @return the last element that matches the predicate or null if none found
   */
  /**
   * Find the last element matching the given predicate
   *
   * @param $self the collection
   * @param predicate the predicate
   * @param <T> the Collection elements type
   * @return the last element matching the given predicate or null
   */
  @Nullable
  public static <T> T findLast(Collection<T> $self, Predicate<T> predicate) {
    if ($self instanceof List) {
      List<T> list = (List<T>) $self;
      for (int i = list.size() - 1; i >= 0; i--) {
        T item = list.get(i);
        if (predicate.test(item)) {
          return item;
        }
      }
    } else {
      T result = null;
      for (T item : $self) {
        if (predicate.test(item)) {
          result = item;
        }
      }
      return result;
    }
    return null;
  }
  
  /**
   * Find the last element that matches the predicate
   * 
   * @param $self the array
   * @param predicate the predicate to match
   * @param <T> the type of elements in the array
   * @return the last element that matches the predicate or null if none found
   */
  /**
   * Find the last element matching the given predicate in an array
   *
   * @param $self the array
   * @param predicate the predicate
   * @param <T> the array elements type
   * @return the last element matching the given predicate or null
   */
  @Nullable
  public static <T> T findLast(T[] $self, Predicate<T> predicate) {
    for (int i = $self.length - 1; i >= 0; i--) {
      if (predicate.test($self[i])) {
        return $self[i];
      }
    }
    return null;
  }
  
  /**
   * Calculates the average of a collection of numbers
   * 
   * @param $self the collection
   * @return the average as a double
   */
  public static double average(Collection<? extends Number> $self) {
    if ($self.isEmpty()) {
      return 0.0;
    }
    double sum = 0.0;
    for (Number number : $self) {
      sum += number.doubleValue();
    }
    return sum / $self.size();
  }
  
  /**
   * Calculates the average of an array of numbers
   * 
   * @param $self the array
   * @return the average as a double
   */
  public static <T extends Number> double average(T[] $self) {
    if ($self.length == 0) {
      return 0.0;
    }
    double sum = 0.0;
    for (T number : $self) {
      sum += number.doubleValue();
    }
    return sum / $self.length;
  }
  
  /**
   * Calculates the average of an array of integers
   * 
   * @param $self the array
   * @return the average as a double
   */
  public static double average(int[] $self) {
    if ($self.length == 0) {
      return 0.0;
    }
    double sum = 0.0;
    for (int number : $self) {
      sum += number;
    }
    return sum / $self.length;
  }
  
  /**
   * Calculates the average of an array of longs
   * 
   * @param $self the array
   * @return the average as a double
   */
  public static double average(long[] $self) {
    if ($self.length == 0) {
      return 0.0;
    }
    double sum = 0.0;
    for (long number : $self) {
      sum += number;
    }
    return sum / $self.length;
  }
  
  /**
   * Calculates the average of an array of doubles
   * 
   * @param $self the array
   * @return the average as a double
   */
  public static double average(double[] $self) {
    if ($self.length == 0) {
      return 0.0;
    }
    double sum = 0.0;
    for (double number : $self) {
      sum += number;
    }
    return sum / $self.length;
  }
  
  /**
   * Calculates the average of an array of floats
   * 
   * @param $self the array
   * @return the average as a double
   */
  public static double average(float[] $self) {
    if ($self.length == 0) {
      return 0.0;
    }
    double sum = 0.0;
    for (float number : $self) {
      sum += number;
    }
    return sum / $self.length;
  }
  
  /**
   * Collates this iterable into sub-lists of length <code>size</code>.
   * Example:
   * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4, 5, 6, 7 ]
   * def coll = list.collate( 3 )
   * assert coll == [ [ 1, 2, 3 ], [ 4, 5, 6 ], [ 7 ] ]</pre>
   *
   * @param $self          an Iterable
   * @param size          the length of each sub-list in the returned list
   * @return a List containing the data collated into sub-lists
   * @since 2.4.0
   */
  public static <T> List<List<T>> collate(Iterable<T> $self, int size) {
    return collate($self, size, true);
  }

  /**
   * Collates this iterable into sub-lists of length <code>size</code> stepping through the code <code>step</code>
   * elements for each subList.
   * Example:
   * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4 ]
   * def coll = list.collate( 3, 1 )
   * assert coll == [ [ 1, 2, 3 ], [ 2, 3, 4 ], [ 3, 4 ], [ 4 ] ]</pre>
   *
   * @param $self          an Iterable
   * @param size          the length of each sub-list in the returned list
   * @param step          the number of elements to step through for each sub-list
   * @return a List containing the data collated into sub-lists
   * @since 2.4.0
   */
  public static <T> List<List<T>> collate(Iterable<T> $self, int size, int step) {
    return collate($self, size, step, true);
  }

  /**
   * Collates this iterable into sub-lists of length <code>size</code>. Any remaining elements in
   * the iterable after the subdivision will be dropped if <code>keepRemainder</code> is false.
   * Example:
   * <pre class="groovyTestCase">def list = [ 1, 2, 3, 4, 5, 6, 7 ]
   * def coll = list.collate( 3, false )
   * assert coll == [ [ 1, 2, 3 ], [ 4, 5, 6 ] ]</pre>
   *
   * @param $self          an Iterable
   * @param size          the length of each sub-list in the returned list
   * @param keepRemainder if true, any remaining elements are returned as sub-lists.  Otherwise they are discarded
   * @return a List containing the data collated into sub-lists
   * @since 2.4.0
   */
  public static <T> List<List<T>> collate(Iterable<T> $self, int size, boolean keepRemainder) {
    return collate($self, size, size, keepRemainder);
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
   * @param $self          an Iterable
   * @param size          the length of each sub-list in the returned list
   * @param step          the number of elements to step through for each sub-list
   * @param keepRemainder if true, any remaining elements are returned as sub-lists.  Otherwise they are discarded
   * @return a List containing the data collated into sub-lists
   */
  public static <T> List<List<T>> collate(Iterable<T> $self, int size, int step, boolean keepRemainder) {
    List<T> selfList = new ArrayList<>();
    $self.forEach(selfList::add);
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

  public static List<List<Object>> combinations(Collection<? extends Collection<?>> $self) {
    List<List<Object>> combos = new ArrayList<>();
    for (Collection<?> collection : $self) {
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
