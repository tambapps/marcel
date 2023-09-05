package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;
import marcel.lang.primitives.collections.lists.CharacterArrayList;
import marcel.lang.primitives.collections.lists.CharacterList;
import marcel.lang.primitives.collections.sets.CharacterOpenHashSet;
import marcel.lang.primitives.collections.sets.CharacterSet;

@AllArgsConstructor
public class DynamicCharSequence extends AbstractDynamicObject implements DynamicIndexable {

  @Getter
  private final CharSequence value;

  @Override
  public DynamicObject plus(Object object) {
    Object o = getRealValue(object);
    if (o instanceof CharSequence) return DynamicObject.of(value.toString() + o);
    throw new MissingMethodException(getValue().getClass(), "plus", new Object[]{object});
  }

  @Override
  public DynamicObject getAt(Object key) {
    return key instanceof Integer ? DynamicObject.of(value.charAt((Integer) key)) : super.getAt(key);
  }

  @Override
  public int size() {
    return value.length();
  }

  @Override
  public int toInt() {
    return Integer.parseInt(value.toString());
  }

  @Override
  public char toChar() {
    return value.charAt(0);
  }

  @Override
  public float toFloat() {
    return Float.parseFloat(value.toString());
  }

  @Override
  public double toDouble() {
    return Double.parseDouble(value.toString());
  }

  @Override
  public long toLong() {
    return Long.parseLong(value.toString());
  }

  @Override
  public CharacterList toCharacterList() {
    return new CharacterArrayList(value.toString());
  }

  @Override
  public CharacterSet toCharacterSet() {
    return new CharacterOpenHashSet(value.toString());
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }
}
