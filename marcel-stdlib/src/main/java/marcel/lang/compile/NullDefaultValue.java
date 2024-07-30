package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Annotation allowing to specify a null default value for a method parameter of Object type
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface NullDefaultValue {

}
