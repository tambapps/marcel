package marcel.lang.dynamic;

import marcel.lang.DynamicObject;

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

  @Override
  public DynamicObject invokeMethod(String name, Object... args) {
    try {
      return super.invokeMethod(name, args);
    } catch (MissingMethodException e) {
      return invokeMethod(Queue.class, name, args);
    }
  }
}
