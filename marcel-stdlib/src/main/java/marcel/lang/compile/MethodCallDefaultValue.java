package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

// TODO create new NoArgsFunctionCallNode which doesn't require a scope and will always find the method from the class
// only works for method with no parameters
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface MethodCallDefaultValue {

  String methodName();
}
