package marcel.util.primitives.collections.sets;

import lombok.AllArgsConstructor;
import marcel.util.primitives.iterators.CharIterator;

@AllArgsConstructor
public class UnmodifiableCharSet extends AbstractCharSet {

  private final CharSet base;

  @Override
  public CharIterator iterator() {
    return base.iterator();
  }

  @Override
  public boolean remove(char k) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return base.size();
  }
}
