package marcel.util.primitives.collections.sets;

import lombok.AllArgsConstructor;
import marcel.util.primitives.iterators.FloatIterator;

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
