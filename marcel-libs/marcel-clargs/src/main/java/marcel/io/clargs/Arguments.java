package marcel.io.clargs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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

    /**
     * A class specifying elements type, as Marcel doesn't support generic types.
     *
     * @return a class specifying elements type
     */
    Class<?> elementsType() default Void.class;

    /**
     * A lambda to convert the incoming String into the desired object. the converter will be applied to each argument.
     *
     * @return the lambda class to convert this option's argument(s)
     */
    Class<?> converter() default Void.class;

    /**
     * A lambda to validate the value of the arguments. Throw an {@link java.lang.IllegalArgumentException} in the lambda
     * to indicate a validation error.
     *
     * @return the lambda class to validate this option's value
     */
    Class<?> validator() default Void.class;

}