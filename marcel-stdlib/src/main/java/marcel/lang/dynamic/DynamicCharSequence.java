package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;
import marcel.lang.primitives.collections.lists.CharArrayList;
import marcel.lang.primitives.collections.lists.CharList;
import marcel.lang.primitives.collections.sets.CharOpenHashSet;
import marcel.lang.primitives.collections.sets.CharSet;

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
  public int asInt() {
    return Integer.parseInt(value.toString());
  }

  @Override
  public char asChar() {
    return value.charAt(0);
  }

  @Override
  public float asFloat() {
    return Float.parseFloat(value.toString());
  }

  @Override
  public double asDouble() {
    return Double.parseDouble(value.toString());
  }

  @Override
  public long asLong() {
    return Long.parseLong(value.toString());
  }

  @Override
  public CharList asCharList() {
    return new CharArrayList(value.toString());
  }

  @Override
  public CharSet asCharSet() {
    return new CharOpenHashSet(value.toString());
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public CharSequence asCharsequence() {
    return value;
  }

  @Override
  public String asString() {
    return value.toString();
  }
}
