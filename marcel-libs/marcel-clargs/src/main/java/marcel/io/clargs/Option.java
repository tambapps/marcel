package marcel.io.clargs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO document it
/**
 * Indicates that a property can be used to set a CLI option.
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Option {
    /**
     * The description of this option
     *
     * @return the description of this option
     */
    String description() default "";

    /**
     * The short name of this option. Defaults to the name of member being annotated if the longName is empty.
     *
     * @return the short name of this option
     */
    String shortName() default "";

    /**
     * The long name of this option. Defaults to the name of member being annotated.
     *
     * @return the long name of this option
     */
    String longName() default "";

    /**
     * The value separator for this multivalued option. Only allowed for array-typed arguments.
     *
     * @return the value separator for this multivalued option
     */
    String valueSeparator() default "";

    /**
     * Whether this option is required, must be provided.
     * Note this flag will be ignored when the annotated field has a non-null/zero/false value, as it would mean a default value was specified for this field.
     *
     * @return true if this option must be provided.
     */
    boolean required() default true;

    /**
     * How many arguments this option has represented as a String.
     * Only allowed for collection-typed arguments.
     * Can be a number, or the following.
     * The special values of '+' means one or more and '*' as 0 or more.
     * You can also combine a number with a '+', e.g. 2+ to specify at least 2
     *
     * @return the number of arguments (as a String)
     */
    String numberOfArguments() default "1";

    /**
     * A conversion lambda to convert the incoming String into the desired object
     *
     * @return the closure to convert this option's argument(s)
     */
    Class<?> converter() default Void.class;
}