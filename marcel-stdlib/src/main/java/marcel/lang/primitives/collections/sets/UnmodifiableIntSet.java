package marcel.lang.primitives.collections.sets;

import lombok.AllArgsConstructor;
import marcel.lang.primitives.iterators.IntIterator;

@AllArgsConstructor
public class UnmodifiableIntSet extends AbstractIntSet {

  private final IntSet base;

  @Override
  public IntIterator iterator() {
    return base.iterator();
  }

  @Override
  public boolean remove(int k) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return base.size();
  }
}
