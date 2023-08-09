package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

// only works for method with no parameters
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface MethodCallDefaultValue {

  String methodName();
}
