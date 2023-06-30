package marcel.lang.dynamic;

import java.util.LinkedList;
import java.util.Queue;

public class DynamicQueue extends DynamicCollection<Queue> {
  public DynamicQueue(Queue value) {
    super(value);
  }

  @Override
  Queue newEmptyInstance() {
    return new LinkedList();
  }
}
