package marcel.lang.dynamic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DynamicSet extends DynamicCollection {
  public DynamicSet(Set value) {
    super(value);
  }

  @Override
  Collection copy() {
    return new HashSet(value);
  }
}
