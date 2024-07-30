package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Annotation allowing to specify the default value of an int or {@link Integer} method parameter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface IntDefaultValue {

    /**
     * Returns the default value to use when the annotated method parameter is not specified
     *
     * @return the default value of the annotated method parameter
     */
    int value() default 0;
}
