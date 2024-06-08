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

  @SuppressWarnings("unchecked")
  public <T> T getVariableOrNull(String name) {
    return (T) variables.get(name);
  }

  public <T> T getVariable(String name) {
    if (!variables.containsKey(name)) {
      throw new NoSuchPropertyException(null, name);
    }
    return (T) variables.get(name);
  }

  public void setVariable(String name, Object newValue) {
    if (variables.containsKey(name)) {
      Object value = getVariable(name);
      if (value != null && newValue != null && !value.getClass().isAssignableFrom(newValue.getClass())) {
        throw new IllegalArgumentException(String.format("Cannot set variable %s: Expected expression of type %s but gave %s",
            name, value.getClass(), newValue.getClass()));
      }
    }
    variables.put(name, newValue);
  }


  public boolean hasVariable(String name) {
    return variables.containsKey(name);
  }

  public boolean removeVariable(String name) {
    boolean wasHere = hasVariable(name);
    variables.remove(name);
    return wasHere;
  }

  public int size() {
    return variables.size();
  }
}
