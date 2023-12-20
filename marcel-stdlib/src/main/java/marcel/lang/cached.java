package marcel.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Meta-annotation to cache results of the annotated method
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value={METHOD})
@MarcelAstTransformationClass("com.tambapps.marcel.semantic.transform.CachedAstTransformation")
public @interface cached {
}
