package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

// only works for method with no parameters
/**
 * Annotation allowing to specify the method returning the default value of the annotated method parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface MethodCallDefaultValue {

  /**
   * The name of the method used to compute the default value of the annotated method parameter. The method should not
   * have any parameters, otherwise it won't be considered.
   *
   * @return the name of the method used to compute the default value of the annotated method parameter
   */
  String methodName();
}
