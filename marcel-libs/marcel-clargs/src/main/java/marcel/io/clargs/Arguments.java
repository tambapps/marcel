package marcel.io.clargs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO handle this
/**
 * Indicates that a property will contain the remaining arguments.
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Arguments {
    /**
     * The description for the remaining non-option arguments
     *
     * @return the description for the remaining non-option arguments
     */
    String description() default "ARGUMENTS";
}