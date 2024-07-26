package marcel.util.concurrent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Annotation allowing to specify that a function is Marcel asynchronous
 */
@Retention(RetentionPolicy.CLASS)
@Target(value={METHOD})
public @interface Async {

  /**
   * The return type of the Future
   * @return the return type of the future
   */
  Class<?> returnType();

}
