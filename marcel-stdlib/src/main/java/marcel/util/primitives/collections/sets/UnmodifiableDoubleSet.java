package marcel.util.primitives.collections.sets;

import lombok.AllArgsConstructor;
import marcel.util.primitives.iterators.DoubleIterator;

@AllArgsConstructor
public class UnmodifiableDoubleSet extends AbstractDoubleSet {

  private final DoubleSet base;

  @Override
  public DoubleIterator iterator() {
    return base.iterator();
  }

  @Override
  public boolean remove(double k) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return base.size();
  }
}
