package marcel.util.primitives.collections.sets;

import marcel.util.primitives.collections.AbstractIntCollection;
import marcel.util.primitives.iterators.IntIterator;

import java.util.Set;

public abstract class AbstractIntSet extends AbstractIntCollection implements Cloneable, IntSet {
	protected AbstractIntSet() {}
	@Override
	public abstract IntIterator iterator();
	@Override
	public boolean equals(final Object o) {
	 if (o == this) return true;
	 if (!(o instanceof Set)) return false;
	 Set<?> s = (Set<?>) o;
	 if (s.size() != size()) return false;
	 if (s instanceof IntSet) {
	  return containsAll((IntSet) s);
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
	 IntIterator i = iterator();
	 int k;
	 while(n-- != 0) {
	  k = i.nextInt(); // We need k because KEY2JAVAHASH() is a macro with repeated evaluation.
	  h += (k);
	 }
	 return h;
	}

}