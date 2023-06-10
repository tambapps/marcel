package marcel.lang.dynamic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DynamicList extends DynamicCollection {
  public DynamicList(List value) {
    super(value);
  }

  @Override
  Collection copy() {
    return new ArrayList(value);
  }
}
