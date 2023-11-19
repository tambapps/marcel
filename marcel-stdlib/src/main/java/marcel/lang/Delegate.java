package marcel.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Interface used to indicate that the annotated field should be a delegate of the class
 */
@Target(value={FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Delegate {
}
