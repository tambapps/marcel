package marcel.util.primitives.collections.sets;

import marcel.util.primitives.collections.AbstractDoubleCollection;
import marcel.util.primitives.iterators.DoubleIterator;

import java.util.Set;

public abstract class AbstractDoubleSet extends AbstractDoubleCollection implements Cloneable, DoubleSet {
	protected AbstractDoubleSet() {}
	@Override
	public abstract DoubleIterator iterator();
	@Override
	public boolean equals(final Object o) {
	 if (o == this) return true;
	 if (!(o instanceof Set)) return false;
	 Set<?> s = (Set<?>) o;
	 if (s.size() != size()) return false;
	 if (s instanceof DoubleSet) {
	  return containsAll((DoubleSet) s);
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
	 DoubleIterator i = iterator();
	 long k;
	 while(n-- != 0) {
	  k = Double.doubleToRawLongBits(i.nextDouble()); // We need k because KEY2JAVAHASH() is a macro with repeated evaluation.
	  h += (k);
	 }
	 return h;
	}

}