package marcel.lang;

import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class Binding {

  public final Map<String, Object> variables;

  public Binding() {
    this(Collections.synchronizedMap(new HashMap<>()));
  }

  public <T> T getVariable(String name) {
    if (!variables.containsKey(name)) {
      throw new NoSuchPropertyException("Property " + name + " was not defined");
    }
    return (T) variables.get(name);
  }

  public void setVariable(String name, Object value) {
    variables.put(name, value);
  }


  public boolean hasVariable(String name) {
    return variables.containsKey(name);
  }

  public boolean removeVariable(String name) {
    boolean wasHere = hasVariable(name);
    variables.remove(name);
    return wasHere;
  }
}
