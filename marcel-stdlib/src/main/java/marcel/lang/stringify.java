package marcel.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates the toString method based on the class's fields
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@MarcelAstTransformationClass("com.tambapps.marcel.semantic.transform.StringifyAstTransformation")
public @interface stringify {
}
