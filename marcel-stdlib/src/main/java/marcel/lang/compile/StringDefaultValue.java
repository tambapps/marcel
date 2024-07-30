package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Annotation specifying the default value of a {@link String} method parameter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface StringDefaultValue {

    /**
     * Returns the default value to use when the annotated method parameter is not specified
     *
     * @return the default value of the annotated method parameter
     */
    String value() default "";

}
