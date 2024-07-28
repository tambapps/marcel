package marcel.util;

import lombok.RequiredArgsConstructor;
import marcel.lang.primitives.iterators.AbstractCharIterator;
import marcel.util.function.CharConsumer;

@RequiredArgsConstructor
public class CharSequenceIterator extends AbstractCharIterator {

  private final CharSequence charSequence;
  private int i = 0;

  @Override
  public char nextChar() {
    return charSequence.charAt(i++);
  }


  @Override
  public boolean hasNext() {
    return i < charSequence.length();
  }

  @Override
  public void forEachRemaining(CharConsumer action) {
    while (hasNext()) action.accept(this.nextChar());
  }
}
