package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

// TODO rename it into null default value
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface ObjectDefaultValue {
    // only handle null for now
}
