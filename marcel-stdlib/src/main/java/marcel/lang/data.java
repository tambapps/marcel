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
@MarcelSyntaxTreeTransformationClass({"com.tambapps.marcel.semantic.transform.DataAstTransformation"})
public @interface data {

  /**
   * Annotation allowing to exclude a particular field/method
   */
  @Retention(RetentionPolicy.SOURCE)
  @Target({ElementType.FIELD, ElementType.METHOD})
  @interface Exclude { }

  /**
   * Whether the class should also be {@link stringify}
   * @return whether the class should also be {@link stringify}
   */
  boolean stringify() default true;

  /**
   * Whether the class should also be {@link comparable}
   * @return whether the class should also be {@link comparable}
   */
  boolean comparable() default false;

  /**
   * Whether to generate a constructor to initialize all class fields.
   * @return whether to generate a constructor to initialize all class fields
   */
  boolean withConstructor() default false;

  /**
   * Whether to include getters for equals and hashCode (and toString, compareTo if corresponding flag enabled) methods
   */
  boolean includeGetters() default false;

}
