package marcel.lang.primitives.collections.sets;

import lombok.AllArgsConstructor;
import marcel.lang.primitives.iterators.LongIterator;

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
