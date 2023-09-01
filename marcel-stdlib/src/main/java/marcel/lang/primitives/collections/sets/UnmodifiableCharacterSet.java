package marcel.lang.primitives.collections.sets;

import lombok.AllArgsConstructor;
import marcel.lang.primitives.iterators.CharacterIterator;

@AllArgsConstructor
public class UnmodifiableCharacterSet extends AbstractCharacterSet {

  private final CharacterSet base;

  @Override
  public CharacterIterator iterator() {
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
