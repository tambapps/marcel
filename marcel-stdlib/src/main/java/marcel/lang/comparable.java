package marcel.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * A class annotation used to make a class implement Comparable comparing the class properties.
 * By default, all class fields are included and all property methods (getters) are excluded.
 */
// TODO handle me
@Retention(RetentionPolicy.CLASS)
@Target(value={TYPE})
@MarcelAstTransformationClass("com.tambapps.marcel.transform.ComparableAstTransformation")
public @interface comparable {

  /**
   * Annotation to exclude a class field from the comparison process
   */
  @Retention(RetentionPolicy.CLASS)
  @Target(value={FIELD})
  public @interface Exclude {
  }

  // TODO handle me
  /**
   * Annotation to include a particular method from the comparison process
   */
  @Retention(RetentionPolicy.CLASS)
  @Target(value={METHOD})
  public @interface Include {
  }
}
