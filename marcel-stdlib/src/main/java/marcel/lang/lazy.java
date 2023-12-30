package marcel.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Meta annotation to implement lazy initialization of a field
 */
// TODO document me
@Retention(RetentionPolicy.SOURCE)
@Target(value={FIELD})
@MarcelSyntaxTreeTransformationClass({"com.tambapps.marcel.semantic.transform.LazyAstTransformation"})
public @interface lazy {

  /**
   * Whether the lazy mechanism should be thread-safe or not
   * @return whether the lazy mechanism should be thread-safe or not
   */
  boolean threadSafe() default false;

}
