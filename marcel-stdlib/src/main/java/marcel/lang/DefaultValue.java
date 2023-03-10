package marcel.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

public @interface DefaultValue {

  int defaultIntValue() default 0;
  long defaultLongValue() default 0L;
  float defaultFloatValue() default 0f;
  double defaultDoubleValue() default 0d;
  char defaultCharValue() default '\0';
  String defaultStringValue() default "";

}
