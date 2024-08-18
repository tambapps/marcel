package marcel.util.primitives.collections.sets;

import lombok.AllArgsConstructor;
import marcel.util.primitives.iterators.LongIterator;

@AllArgsConstructor
public class UnmodifiableLongSet extends AbstractLongSet {

  private final LongSet base;

  @Override
  public LongIterator iterator() {
    return base.iterator();
  }

  @Override
  public boolean remove(long k) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return base.size();
  }
}
