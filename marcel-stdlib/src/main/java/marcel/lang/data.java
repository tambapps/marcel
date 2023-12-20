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
@MarcelAstTransformationClass({
    "com.tambapps.marcel.semantic.transform.StringifyAstTransformation",
    "com.tambapps.marcel.semantic.transform.EqualsAndHashcodeAstTransformation"
})
public @interface data {

  // TODO handle me
  boolean comparable() default false;
}
