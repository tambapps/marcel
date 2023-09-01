package marcel.lang.primitives.collections.sets;

import lombok.AllArgsConstructor;
import marcel.lang.primitives.iterators.FloatIterator;

@AllArgsConstructor
public class UnmodifiableFloatSet extends AbstractFloatSet {

  private final FloatSet base;

  @Override
  public FloatIterator iterator() {
    return base.iterator();
  }

  @Override
  public boolean remove(float k) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return base.size();
  }
}
