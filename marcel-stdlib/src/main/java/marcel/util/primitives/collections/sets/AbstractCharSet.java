package marcel.util.primitives.collections.sets;

import marcel.util.primitives.collections.AbstractCharCollection;
import marcel.util.primitives.iterators.CharIterator;

import java.util.Set;

public abstract class AbstractCharSet extends AbstractCharCollection implements Cloneable, CharSet {
	protected AbstractCharSet() {}
	@Override
	public abstract CharIterator iterator();
	@Override
	public boolean equals(final Object o) {
	 if (o == this) return true;
	 if (!(o instanceof Set)) return false;
	 Set<?> s = (Set<?>) o;
	 if (s.size() != size()) return false;
	 if (s instanceof CharSet) {
	  return containsAll((CharSet) s);
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
	 CharIterator i = iterator();
	 char k;
	 while(n-- != 0) {
	  k = i.nextChar(); // We need k because KEY2JAVAHASH() is a macro with repeated evaluation.
	  h += (k);
	 }
	 return h;
	}

}