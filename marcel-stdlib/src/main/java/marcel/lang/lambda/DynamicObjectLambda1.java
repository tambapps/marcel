package marcel.lang.lambda;

import marcel.lang.DynamicObject;

public interface DynamicObjectLambda1 extends Lambda1<DynamicObject, DynamicObject> {

  @Override
  DynamicObject invoke(DynamicObject object);

}
