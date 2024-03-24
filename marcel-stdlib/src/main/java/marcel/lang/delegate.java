package marcel.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Meta annotation used to specify the delegate of a class
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value={FIELD})
@MarcelSyntaxTreeTransformationClass({"com.tambapps.marcel.semantic.transform.DelegateCstTransformation"})
public @interface delegate {

}
