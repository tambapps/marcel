package marcel.lang.dynamic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class DynamicQueue extends DynamicCollection {
  public DynamicQueue(Queue value) {
    super(value);
  }

  @Override
  Collection copy() {
    return new LinkedList(value);
  }
}
