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
@MarcelSyntaxTreeTransformationClass("com.tambapps.marcel.semantic.transform.CachedAstTransformation")
public @interface cached {

  /**
   * Whether the caching mechanism should be thread-safe or not
   * @return whether the caching mechanism should be thread-safe or not
   */
  boolean threadSafe() default false;
}
