package marcel.lang.lambda;

import marcel.lang.DynamicObject;

import java.util.function.Function;

public interface DynamicObjectLambda1 extends Function<DynamicObject, DynamicObject> {

  @Override
  DynamicObject apply(DynamicObject object);

}
