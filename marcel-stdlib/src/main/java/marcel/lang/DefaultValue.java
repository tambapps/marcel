package marcel.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Annotation used by the compiler to specify default values of method parameters
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface DefaultValue {

  int defaultIntValue() default 0;
  long defaultLongValue() default 0L;
  float defaultFloatValue() default 0f;
  double defaultDoubleValue() default 0d;
  char defaultCharValue() default '\0';
  String defaultStringValue() default "";

}
