package marcel.lang.dynamic;

import marcel.lang.DynamicObject;

import java.util.LinkedList;
import java.util.Map;
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
  public DynamicObject invokeMethod(String name, Map<String, Object> namedArgs, Object... args) {
    try {
      return super.invokeMethod(name, namedArgs, args);
    } catch (MissingMethodException e) {
      return invokeMethod(Queue.class, name, namedArgs, args);
    }
  }
}
