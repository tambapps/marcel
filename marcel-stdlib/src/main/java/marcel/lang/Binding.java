package marcel.lang;

import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class Binding {

  private final Map<String, Object> variables;

  public Binding() {
    this(Collections.synchronizedMap(new HashMap<>()));
  }

  public <T> T getVariable(Class<T> clazz, String name) {
    Object value = getVariable(name);
    if (clazz.isInstance(value)) {
      return (T) value;
    } else if (!clazz.isPrimitive() && value == null) {
      return null;
    } else {
      throw new ClassCastException(String.format("Could not cast value %s as %s", value, clazz.getName()));
    }
  }

  public Object getVariable(String name) {
    if (!variables.containsKey(name)) {
      throw new UndefinedVariableException("Variable with name '" + name + "' is not defined");
    }
    return variables.get(name);
  }

  public void setVariable(String name, Object value) {
    Object currentValue = variables.get(name);
    Class<?> clazz = currentValue != null ? currentValue.getClass() : Object.class;
    if (clazz.isInstance(value)) {
      variables.put(name, value);
    } else if (!clazz.isPrimitive() && value == null) {
      variables.put(name, null);
    } else {
      throw new IllegalArgumentException(String.format("Variable %s is of type %s but a value of type %s was provided", name, clazz, value.getClass()));
    }
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
