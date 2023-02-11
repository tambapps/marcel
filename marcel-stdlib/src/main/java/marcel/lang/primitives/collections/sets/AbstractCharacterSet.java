package marcel.lang.primitives.collections.sets;

import marcel.lang.primitives.collections.AbstractCharacterCollection;
import marcel.lang.primitives.iterators.CharacterIterator;

import java.util.Set;

public abstract class AbstractCharacterSet extends AbstractCharacterCollection implements Cloneable, CharacterSet {
	protected AbstractCharacterSet() {}
	@Override
	public abstract CharacterIterator iterator();
	@Override
	public boolean equals(final Object o) {
	 if (o == this) return true;
	 if (!(o instanceof Set)) return false;
	 Set<?> s = (Set<?>) o;
	 if (s.size() != size()) return false;
	 if (s instanceof CharacterSet) {
	  return containsAll((CharacterSet) s);
	 }
	 return containsAll(s);
	}
	/** Returns a hash code for this set.
	 *
	 * The hash code of a set is computed by summing the hash codes of
	 * its elements.
	 *
	 * @return a hash code for this set.
	 */
	@Override
	public int hashCode() {
	 int h = 0, n = size();
	 CharacterIterator i = iterator();
	 char k;
	 while(n-- != 0) {
	  k = i.nextCharacter(); // We need k because KEY2JAVAHASH() is a macro with repeated evaluation.
	  h += (k);
	 }
	 return h;
	}

}