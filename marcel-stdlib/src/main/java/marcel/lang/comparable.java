package marcel.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation making a class implement Comparable. The comparison is based on the class's properties
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@MarcelAstTransformationClass("com.tambapps.marcel.semantic.transform.ComparableAstTransformation")
public @interface comparable {

  /**
   * Annotation allowing to exclude a particular field/method from the string representation
   */
  @Retention(RetentionPolicy.SOURCE)
  @Target({ElementType.FIELD, ElementType.METHOD})
  @interface Exclude { }

  // TODO handle me
  /**
   * Whether to include getters in the string representation.
   * Note that this only applies to public getters as others are always excluded
   * @return whether to include getters in the string representation
   */
  boolean includeGetters() default false;
}
