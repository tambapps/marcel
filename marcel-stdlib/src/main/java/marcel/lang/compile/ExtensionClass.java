package marcel.lang.compile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation allowing to specify the extended class of an extension class. This is because of this annotation that we
 * can define static extension methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={TYPE})
public @interface ExtensionClass {

  Class<?> forClass() default Void.class;

}
