package marcel.lang.primitives.collections;

import marcel.lang.primitives.iterators.CharacterIterator;
import marcel.lang.util.Arrays;
import marcel.lang.util.function.CharacterConsumer;
import marcel.lang.util.function.CharacterPredicate;

import java.util.AbstractCollection;
import java.util.Collection;

public abstract class AbstractCharacterCollection extends AbstractCollection<Character> implements CharacterCollection {
	protected AbstractCharacterCollection() {}
	@Override
	public abstract CharacterIterator iterator();
	/** {@inheritDoc}
	 *
	 * @implSpec This implementation always throws an {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean add(final char k) {
	 throw new UnsupportedOperationException();
	}
	/** {@inheritDoc}
	 *
	 * @implSpec This implementation iterates over the elements in the collection,
	 * looking for the specified element.
	 */
	@Override
	public boolean contains(final char k) {
	 final CharacterIterator iterator = iterator();
    while (iterator.hasNext()) {
      if (k == iterator.nextCharacter()) {
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
	 return CharacterCollection.super.add(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean contains(final Object key) {
	 return CharacterCollection.super.contains(key);
	}
	/** {@inheritDoc}
	 * @deprecated Please use the corresponding type-specific method instead.
	 */
	@Deprecated
	@Override
	public boolean remove(final Object key) {
	 return CharacterCollection.super.remove(key);
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
	public char[] toCharacterArray() {
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
	 * @apiNote This method exists to make final what should have been final in the interface.
	 */
	@Override
	public void forEach(final CharacterConsumer action) {
	 CharacterCollection.super.forEach(action);
	}
	/** {@inheritDoc}
	 *
	 * @apiNote This method exists to make final what should have been final in the interface.
	 */
	@Override
	public boolean removeIf(final CharacterPredicate filter) {
	 return CharacterCollection.super.removeIf(filter);
	}
	@Override
	public boolean addAll(final CharacterCollection c) {
	 boolean retVal = false;
	 for(final CharacterIterator i = c.iterator(); i.hasNext();)
     if (add(i.nextCharacter())) {
       retVal = true;
     }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean addAll(final Collection<? extends Character> c) {
	 if (c instanceof CharacterCollection) {
	  return addAll((CharacterCollection) c);
	 }
	 return super.addAll(c);
	}
	@Override
	public boolean containsAll(final CharacterCollection c) {
	 for(final CharacterIterator i = c.iterator(); i.hasNext();)
     if (!contains(i.nextCharacter())) {
       return false;
     }
	 return true;
	}
	/** {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
	 if (c instanceof CharacterCollection) {
	  return containsAll((CharacterCollection) c);
	 }
	 return super.containsAll(c);
	}
	@Override
	public boolean removeAll(final CharacterCollection c) {
	 boolean retVal = false;
	 for(final CharacterIterator i = c.iterator(); i.hasNext();)
     if (removeCharacter(i.nextCharacter())) {
       retVal = true;
     }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
	 if (c instanceof CharacterCollection) {
	  return removeAll((CharacterCollection) c);
	 }
	 return super.removeAll(c);
	}
	@Override
	public boolean retainAll(final CharacterCollection c) {
	 boolean retVal = false;
	 for(final CharacterIterator i = iterator(); i.hasNext();)
	  if (! c.contains(i.nextCharacter())) {
	   i.remove();
	   retVal = true;
	  }
	 return retVal;
	}
	/** {@inheritDoc}
	 *
	 * @implSpec This implementation delegates to the type-specific version if given a type-specific
	 * collection, otherwise is uses the implementation from {@link AbstractCollection}.
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
	 if (c instanceof CharacterCollection) {
	  return retainAll((CharacterCollection) c);
	 }
	 return super.retainAll(c);
	}
	@Override
	public String toString() {
	 final StringBuilder s = new StringBuilder();
	 final CharacterIterator i = iterator();
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
	  k = i.nextCharacter();
	   s.append(String.valueOf(k));
	 }
	 s.append("}");
	 return s.toString();
	}
}
