package marcel.lang.primitives.collections.maps;

import marcel.lang.primitives.collections.sets.AbstractIntSet;
import marcel.lang.primitives.collections.sets.IntSet;
import marcel.lang.primitives.iterators.IntIterator;
import marcel.lang.primitives.spliterators.IntSpliterator;
import marcel.lang.primitives.spliterators.IntSpliterators;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public abstract class AbstractInt2ObjectMap <V> implements Int2ObjectMap <V>, java.io.Serializable {
	private static final long serialVersionUID = -4940583368468432370L;
	protected AbstractInt2ObjectMap() {}
	/**
	 * {@inheritDoc}
	 * @implSpec This implementation does a linear search over the entry set, finding an entry that has the key specified.
	 *   <p>If you override {@link #keySet()}, you should probably override this method too
	 *   to take advantage of the (presumably) faster {@linkplain java.util.Set#contains key membership test} your {@link #keySet()} provides.
	 *   <p>If you override this method but not {@link #keySet()}, then the returned key set will take advantage of this method.
	 */
	@Override
	public boolean containsKey(final int k) {
	 final Iterator<Entry <V> > i = int2ObjectEntrySet().iterator();
	 while(i.hasNext())
	  if (i.next().getIntKey() == k)
	   return true;
	 return false;
	}
	/**
	 * {@inheritDoc}
	 * @implSpec This implementation does a linear search over the entry set, finding an entry that has the value specified.
	 *   <p>If you override {@link #values()}, you should probably override this method too
	 *   to take advantage of the (presumably) faster {@linkplain java.util.Collection#contains value membership test} your {@link #values()} provides.
	 *   <p>If you override this method but not {@link #values()}, then the returned values collection will take advantage of this method.
	 */
	@Override
	public boolean containsValue(final Object v) {
	 final Iterator<Int2ObjectMap.Entry <V> > i = int2ObjectEntrySet().iterator();
	 while(i.hasNext())
	  if (i.next().getValue() == v)
	   return true;
	 return false;
	}
	@Override
	public boolean isEmpty() {
	 return size() == 0;
	}
	/** This class provides a basic but complete type-specific entry class for all those maps implementations
	 * that do not have entries on their own (e.g., most immutable maps).
	 *
	 * <p>This class does not implement {@link java.util.Map.Entry#setValue(Object) setValue()}, as the modification
	 * would not be reflected in the base map.
	 */
	public static class BasicEntry <V> implements Int2ObjectMap.Entry <V> {
	 protected int key;
	 protected V value;
	 public BasicEntry() {}
	 public BasicEntry(final Integer key, final V value) {
	  this.key = (key).intValue();
	  this.value = (value);
	 }
	 public BasicEntry(final int key, final V value) {
	  this.key = key;
	  this.value = value;
	 }
	 @Override
	 public int getIntKey() {
	  return key;
	 }
	 @Override
	 public V getValue() {
	  return value;
	 }
	 @Override
	 public V setValue(final V value) {
	  throw new UnsupportedOperationException();
	 }
	 @SuppressWarnings("unchecked")
	 @Override
	 public boolean equals(final Object o) {
	  if (!(o instanceof Map.Entry)) return false;
	  if (o instanceof Int2ObjectMap.Entry) {
	   final Int2ObjectMap.Entry <V> e = (Int2ObjectMap.Entry <V>) o;
	   return ( (key) == (e.getIntKey()) ) && java.util.Objects.equals(value, e.getValue());
	  }
	  final Map.Entry<?,?> e = (Map.Entry<?,?>)o;
	  final Object key = e.getKey();
	  if (key == null || !(key instanceof Integer)) return false;
	  final Object value = e.getValue();
	  return ( (this.key) == (((Integer)(key)).intValue()) ) && java.util.Objects.equals(this.value, (value));
	 }
	 @Override
	 public int hashCode() {
	  return (key) ^ ( (value) == null ? 0 : (value).hashCode() );
	 }
	 @Override
	 public String toString() {
	  return key + "->" + value;
	 }
	}
	/** This class provides a basic implementation for an Entry set which forwards some queries to the map.
	 */
	public abstract static class BasicEntrySet <V> extends AbstractSet<Entry <V> > {
	 protected final Int2ObjectMap <V> map;
	 public BasicEntrySet(final Int2ObjectMap <V> map) {
	  this.map = map;
	 }
	 @SuppressWarnings("unchecked")
	 @Override
	 public boolean contains(final Object o) {
	  if (!(o instanceof Map.Entry)) return false;
	  if (o instanceof Int2ObjectMap.Entry) {
	   final Int2ObjectMap.Entry <V> e = (Int2ObjectMap.Entry <V>) o;
	   final int k = e.getIntKey();
	   return map.containsKey(k) && java.util.Objects.equals(map.get(k), e.getValue());
	  }
	  final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
	  final Object key = e.getKey();
	  if (key == null || !(key instanceof Integer)) return false;
	  final int k = ((Integer)(key)).intValue();
	  final Object value = e.getValue();
	  return map.containsKey(k) && java.util.Objects.equals(map.get(k), (value));
	 }
	 @SuppressWarnings("unchecked")
	 @Override
	 public boolean remove(final Object o) {
	  if (!(o instanceof Map.Entry)) return false;
	  if (o instanceof Int2ObjectMap.Entry) {
	   final Int2ObjectMap.Entry <V> e = (Int2ObjectMap.Entry <V>) o;
	   return map.remove(e.getIntKey(), e.getValue());
	  }
	  Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
	  final Object key = e.getKey();
	  if (key == null || !(key instanceof Integer)) return false;
	  final int k = ((Integer)(key)).intValue();
	  final Object v = e.getValue();
	  return map.remove(k, v);
	 }
	 @Override
	 public int size() {
	  return map.size();
	 }
	 @Override
	 public Spliterator<Entry <V> > spliterator() {
	  return Spliterators.spliterator(
	   iterator(), map.size(), IntSpliterators.SET_SPLITERATOR_CHARACTERISTICS);
	 }
	}
	/** Returns a type-specific-set view of the keys of this map.
	 *
	 * <p>The view is backed by the set returned by {@link Map#entrySet()}. Note that
	 * <em>no attempt is made at caching the result of this method</em>, as this would
	 * require adding some attributes that lightweight implementations would
	 * not need. Subclasses may easily override this policy by calling
	 * this method and caching the result, but implementors are encouraged to
	 * write more efficient ad-hoc implementations.
	 *
	 * @return a set view of the keys of this map; it may be safely cast to a type-specific interface.
	 */
	@Override
	public IntSet keySet() {
	 return new AbstractIntSet() {
	   @Override
	   public boolean contains(final int k) { return containsKey(k); }
	   @Override
	   public int size() { return AbstractInt2ObjectMap.this.size(); }
	   @Override
	   public void clear() { AbstractInt2ObjectMap.this.clear(); }
	   @Override
	   public IntIterator iterator() {
	    return new IntIterator () {
	      private final Iterator<Int2ObjectMap.Entry <V> > i = AbstractInt2ObjectMap.this.int2ObjectEntrySet().iterator();
	      @Override
	      public int nextInt() { return i.next().getIntKey(); }
	      @Override
	      public boolean hasNext() { return i.hasNext(); }
	      @Override
	      public void remove() { i.remove(); }
	      @Override
	      public void forEachRemaining(final java.util.function.IntConsumer action) {
	       i.forEachRemaining(entry -> action.accept(entry.getIntKey()));
	      }
	     };
	   }
	   @Override
	   public IntSpliterator spliterator() {
	    return IntSpliterators.asSpliterator(
	     iterator(), AbstractInt2ObjectMap.this.size(), IntSpliterators.SET_SPLITERATOR_CHARACTERISTICS);
	   }

		 @Override
		 public boolean remove(int k) {
			 throw new UnsupportedOperationException();
		 }
	 };
	}
	/** Returns a type-specific-set view of the values of this map.
	 *
	 * <p>The view is backed by the set returned by {@link Map#entrySet()}. Note that
	 * <em>no attempt is made at caching the result of this method</em>, as this would
	 * require adding some attributes that lightweight implementations would
	 * not need. Subclasses may easily override this policy by calling
	 * this method and caching the result, but implementors are encouraged to
	 * write more efficient ad-hoc implementations.
	 *
	 * @return a set view of the values of this map; it may be safely cast to a type-specific interface.
	 */
	@Override
	public Collection<V> values() {
	 return new AbstractCollection<V>() {
	   @Override
	   public boolean contains(final Object k) { return containsValue(k); }
	   @Override
	   public int size() { return AbstractInt2ObjectMap.this.size(); }
	   @Override
	   public void clear() { AbstractInt2ObjectMap.this.clear(); }
	   @Override
	   public Iterator <V> iterator() {
	    return new Iterator <V>() {
	      private final Iterator<Int2ObjectMap.Entry <V> > i = AbstractInt2ObjectMap.this.iterator();
	      @Override
	      public V next() { return i.next().getValue(); }
	      @Override
	      public boolean hasNext() { return i.hasNext(); }
	      @Override
	      public void remove() { i.remove(); }
	      @Override
	      public void forEachRemaining(final Consumer<? super V> action) {
	       i.forEachRemaining(entry -> action.accept(entry.getValue()));
	      }
	     };
	   }
	   @Override
	   public Spliterator <V> spliterator() {
	    return Spliterators.spliterator(
	     iterator(), AbstractInt2ObjectMap.this.size(), IntSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS);
	   }
	  };
	}
	/** {@inheritDoc} */
	@SuppressWarnings({"unchecked", "deprecation"})
	@Override
	public void putAll(final Map<? extends Integer,? extends V> m) {
	 if (m instanceof Int2ObjectMap) {
	  Iterator<Int2ObjectMap.Entry <V> > i = ((Int2ObjectMap <V>) m).iterator();
	  while (i.hasNext()) {
	   final Int2ObjectMap.Entry <? extends V> e = i.next();
	   put(e.getIntKey(), e.getValue());
	  }
	 } else {
	  int n = m.size();
	  final Iterator<? extends Map.Entry<? extends Integer,? extends V>> i = m.entrySet().iterator();
	  Map.Entry<? extends Integer,? extends V> e;
	  while (n-- != 0) {
	   e = i.next();
	   put(e.getKey(), e.getValue());
	  }
	 }
	}
	/** Returns a hash code for this map.
	 *
	 * The hash code of a map is computed by summing the hash codes of its entries.
	 *
	 * @return a hash code for this map.
	 */
	@Override
	public int hashCode() {
	 int h = 0, n = size();
	 final Iterator<Int2ObjectMap.Entry <V> > i = iterator();
	 while(n-- != 0) h += i.next().hashCode();
	 return h;
	}
	@Override
	public boolean equals(Object o) {
	 if (o == this) return true;
	 if (! (o instanceof Map)) return false;
	 final Map<?,?> m = (Map<?,?>)o;
	 if (m.size() != size()) return false;
	 return int2ObjectEntrySet().containsAll(m.entrySet());
	}
	@Override
	public String toString() {
	 final StringBuilder s = new StringBuilder();
	 final Iterator<Int2ObjectMap.Entry <V> > i = iterator();
	 int n = size();
	 Int2ObjectMap.Entry <V> e;
	 boolean first = true;
	 s.append("{");
	 while(n-- != 0) {
	  if (first) first = false;
	  else s.append(", ");
	  e = i.next();
	   s.append(String.valueOf(e.getIntKey()));
	  s.append("=>");
	  if (this == e.getValue()) s.append("(this map)"); else
	   s.append(String.valueOf(e.getValue()));
	 }
	 s.append("}");
	 return s.toString();
	}
}