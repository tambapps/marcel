package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface CharacterDefaultValue {
    char value() default '\0';
    boolean isNull() default false;
}
