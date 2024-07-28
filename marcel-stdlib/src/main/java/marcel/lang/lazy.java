package marcel.lang;

import marcel.transform.MarcelSyntaxTreeTransformationClass;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Meta annotation used to implement lazy initialization of a field
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value={FIELD})
@MarcelSyntaxTreeTransformationClass({"com.tambapps.marcel.semantic.transform.LazyCstTransformation"})
public @interface lazy {

}
