package marcel.lang.primitives.collections.sets;

import marcel.lang.primitives.collections.AbstractLongCollection;
import marcel.lang.primitives.iterators.LongIterator;

import java.util.Set;

public abstract class AbstractLongSet extends AbstractLongCollection implements Cloneable, LongSet {
	protected AbstractLongSet() {}
	@Override
	public abstract LongIterator iterator();
	@Override
	public boolean equals(final Object o) {
	 if (o == this) return true;
	 if (!(o instanceof Set)) return false;
	 Set<?> s = (Set<?>) o;
	 if (s.size() != size()) return false;
	 if (s instanceof LongSet) {
	  return containsAll((LongSet) s);
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
	 LongIterator i = iterator();
	 long k;
	 while(n-- != 0) {
	  k = i.nextLong(); // We need k because KEY2JAVAHASH() is a macro with repeated evaluation.
	  h += (k);
	 }
	 return h;
	}

}