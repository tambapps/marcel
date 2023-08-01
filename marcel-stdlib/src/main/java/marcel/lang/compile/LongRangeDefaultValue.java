package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

// TODO handle me
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface LongRangeDefaultValue {
    long from();
    long to();

    boolean fromExclusive();
    boolean toExclusive();
}
