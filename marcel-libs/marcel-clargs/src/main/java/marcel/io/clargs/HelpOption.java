package marcel.io.clargs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a property can be used as a help option, to print usage of the CLI.
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface HelpOption {

    /**
     * The short name of this option. Defaults to the name of member being annotated if the longName is empty.
     *
     * @return the short name of this option
     */
    String shortName() default "h";

    /**
     * The long name of this option. Defaults to the name of member being annotated.
     *
     * @return the long name of this option
     */
    String longName() default "help";


    /**
     * The description of this option
     *
     * @return the description of this option
     */
    String description() default "Prints usage information";
}