package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;
import marcel.lang.IntRange;
import marcel.lang.MarcelTruth;
import marcel.lang.lambda.DynamicObjectLambda1;
import marcel.lang.primitives.collections.lists.IntList;
import marcel.lang.primitives.collections.sets.IntSet;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class DynamicIntRange extends AbstractDynamicObject {

  @Getter
  final IntRange value;

  @Override
  public List asList() {
    return value.toList();
  }

  @Override
  public DynamicObject find(DynamicObjectLambda1 lambda1) {
    return DynamicObject.of(
        value.toList().stream().filter(i -> MarcelTruth.isTruthy(lambda1.invoke(DynamicObject.of(i))))
            .findFirst().orElse(null)
    );
  }

  @Override
  public DynamicObject map(DynamicObjectLambda1 lambda1) {
    return new DynamicList(
        value.toList().stream().map(i -> lambda1.invoke(DynamicObject.of(i)))
            .collect(Collectors.toList())
    );
  }

  @Override
  public IntList asIntList() {
    return value.toList();
  }

  @Override
  public IntSet asIntSet() {
    return value.toList().toSet();
  }
}
