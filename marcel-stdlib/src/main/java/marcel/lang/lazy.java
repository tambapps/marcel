package marcel.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Field annotation to simplify lazy initialization.
 */
// TODO handle me
@Retention(RetentionPolicy.SOURCE)
@Target(value={FIELD})
public @interface lazy {
}
