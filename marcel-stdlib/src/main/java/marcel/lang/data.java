package marcel.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation generating the equals, hashCode and toString method of a class based on its properties
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@MarcelAstTransformationClass({"com.tambapps.marcel.semantic.transform.DataAstTransformation"})
public @interface data {

  /**
   * Annotation allowing to exclude a particular field/method
   */
  @Retention(RetentionPolicy.SOURCE)
  @Target({ElementType.FIELD, ElementType.METHOD})
  @interface Exclude { }

  boolean comparable() default false;
  boolean stringify() default true;

  /**
   * Whether to include getters for the stringify and the comparable if enabled
   */
  boolean includeGetters() default false;
}
