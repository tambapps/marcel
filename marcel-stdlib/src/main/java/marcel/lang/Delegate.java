package marcel.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Interface used to indicate
 */
@Target(value={FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Delegate {
}
