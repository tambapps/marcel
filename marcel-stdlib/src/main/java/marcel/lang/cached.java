package marcel.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Method annotation to cache results of the annoted method
 */
// TODO handle me
@Retention(RetentionPolicy.SOURCE)
@Target(value={METHOD})
// @GroovyASTTransformationClass("org.codehaus.groovy.transform.ReadWriteLockASTTransformation")
public @interface cached {
}
