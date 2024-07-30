package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Annotation allowing to specify the default value of a double or {@link Double} method parameter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface DoubleDefaultValue {

    /**
     * Returns the default value to use when the annotated method parameter is not specified
     *
     * @return the default value of the annotated method parameter
     */
    double value() default 0d;
}
