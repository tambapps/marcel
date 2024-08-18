package marcel.util.primitives.collections;

import marcel.util.primitives.iterators.CharIterator;
import marcel.util.primitives.Arrays;
import marcel.util.function.CharConsumer;
import marcel.util.function.CharPredicate;

import java.util.AbstractCollection;
import java.util.Collection;

public abstract class AbstractCharCollection extends AbstractCollection<Character> implements CharCollection {
	protected AbstractCharCollection() {}
	@Override
	public abstract CharIterator iterator();
	/** {@inheritDoc}
	 *
	 * This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean add(final char k) {
	 throw new UnsupportedOperationException();
	}
	/** {@inheritDoc}
	 *
	 * This implementation iterates over the elements in the collection,
	 * looking for the specified element.
	 */
	@Override
	public boolean contains(final char k) {
	 final CharIterator iterator = iterator();
    while (iterator.hasNext()) {
      if (k == iterator.nextChar()) {
        return true;
      }
    }
	 return false;
	}

	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean add(final Character key) {
	 return CharCollection.super.add(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean contains(final Object key) {
	 return CharCollection.super.contains(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean remove(final Object key) {
	 return CharCollection.super.remove(key);
	}
	@Override
	public char[] toArray(char a[]) {
	 final int size = size();
	 if (a == null) {
	  a = new char[size];
	 } else if (a.length < size) {
	  a = java.util.Arrays.copyOf(a, size);
	 }
	 iterator().unwrap(a);
	 return a;
	}
	@Override
	public char[] toCharArray() {
		final int size = size();
		if (size == 0) {
			return Arrays.EMPTY_CHARACTER_ARRAY;
		}
		final char[] a = new char[size];
		iterator().unwrap(a);
		return a;
	}
	/** {@inheritDoc}
	 *
	 * This method exists to make final what should have been final in the interface.
	 */
	@Override
	public void forEach(final CharConsumer action) {
	 CharCollection.super.forEach(action);
	}
	/** {@inheritDoc}
	 *
	 * This method exists to make final what should have been final in the interface.
	 */
	@Override
	public boolean removeIf(final CharPredicate filter) {
	 return CharCollection.super.removeIf(filter);
	}
	@Override
	public boolean addAll(final CharCollection c) {
	 boolean retVal = false;
	 for(final CharIterator i = c.iterator(); i.hasNext();)
     if (add(i.nextChar())) {
       retVal = true;
     }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean addAll(final Collection<? extends Character> c) {
	 if (c instanceof CharCollection) {
	  return addAll((CharCollection) c);
	 }
	 return super.addAll(c);
	}
	@Override
	public boolean containsAll(final CharCollection c) {
	 for(final CharIterator i = c.iterator(); i.hasNext();)
     if (!contains(i.nextChar())) {
       return false;
     }
	 return true;
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
	 if (c instanceof CharCollection) {
	  return containsAll((CharCollection) c);
	 }
	 return super.containsAll(c);
	}
	@Override
	public boolean removeAll(final CharCollection c) {
	 boolean retVal = false;
	 for(final CharIterator i = c.iterator(); i.hasNext();)
     if (removeChar(i.nextChar())) {
       retVal = true;
     }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
	 if (c instanceof CharCollection) {
	  return removeAll((CharCollection) c);
	 }
	 return super.removeAll(c);
	}
	@Override
	public boolean retainAll(final CharCollection c) {
	 boolean retVal = false;
	 for(final CharIterator i = iterator(); i.hasNext();)
	  if (! c.contains(i.nextChar())) {
	   i.remove();
	   retVal = true;
	  }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
	 if (c instanceof CharCollection) {
	  return retainAll((CharCollection) c);
	 }
	 return super.retainAll(c);
	}
	@Override
	public String toString() {
	 final StringBuilder s = new StringBuilder();
	 final CharIterator i = iterator();
	 int n = size();
	 char k;
	 boolean first = true;
	 s.append("{");
	 while(n-- != 0) {
     if (first) {
       first = false;
     } else {
       s.append(", ");
     }
	  k = i.nextChar();
	   s.append(String.valueOf(k));
	 }
	 s.append("}");
	 return s.toString();
	}
}
