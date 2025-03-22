package marcel.io.clargs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
     * Whether this option can have an optional argument.
     * Only supported for array-typed arguments to indicate that the array may be empty.
     *
     * @return true if this array-typed option can have an optional argument (i.e. could be empty)
     */
    boolean optional() default false;

    /**
     * How many arguments this option has.
     * A value greater than 1 is only allowed for array-typed arguments.
     * Ignored for boolean options which are assumed to have a default of 0
     * or if {@code numberOfArgumentsString} is set.
     *
     * @return the number of arguments
     */
    int numberOfArguments() default 1;

    /**
     * How many arguments this option has represented as a String.
     * Only allowed for array-typed arguments.
     * Overrides {@code numberOfArguments} if set.
     * The special values of '+' means one or more and '*' as 0 or more.
     *
     * @return the number of arguments (as a String)
     */
    String numberOfArgumentsString() default "";

    /**
     * The default value for this option as a String; subject to type conversion and 'convert'.
     * Ignored for Boolean options.
     *
     * @return the default value for this option
     */
    String defaultValue() default "";

    /**
     * A conversion closure to convert the incoming String into the desired object
     *
     * @return the closure to convert this option's argument(s)
     */
    Class<?> convert() default Void.class;
}