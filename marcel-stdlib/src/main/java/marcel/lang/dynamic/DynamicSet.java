package marcel.lang.dynamic;

import java.util.HashSet;
import java.util.Set;

public class DynamicSet extends DynamicCollection<Set> {
  public DynamicSet(Set value) {
    super(value);
  }

  @Override
  Set copy() {
    return new HashSet(value);
  }
}
