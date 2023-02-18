package marcel.lang.util;

import lombok.RequiredArgsConstructor;
import marcel.lang.primitives.iterators.AbstractCharacterIterator;
import marcel.lang.util.function.CharacterConsumer;

@RequiredArgsConstructor
public class CharSequenceIterator extends AbstractCharacterIterator {

  private final CharSequence charSequence;
  private int i = 0;

  @Override
  public char nextCharacter() {
    return charSequence.charAt(i++);
  }


  @Override
  public boolean hasNext() {
    return i < charSequence.length();
  }

  @Override
  public void forEachRemaining(CharacterConsumer action) {
    while (hasNext()) action.accept(nextCharacter());
  }
}
