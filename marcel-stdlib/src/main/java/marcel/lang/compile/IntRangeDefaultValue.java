package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

// TODO handle me
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface IntRangeDefaultValue {
    int from() default 0;
    int to() default 0;

    boolean fromExclusive() default false;
    boolean toExclusive() default false;
}
