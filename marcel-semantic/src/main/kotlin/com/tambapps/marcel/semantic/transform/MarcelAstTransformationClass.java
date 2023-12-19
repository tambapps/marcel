package com.tambapps.marcel.semantic.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an annotation to put on Marcel meta annotations.
 * It allows the compiler to know which transformation to apply, based on the classes provided
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface MarcelAstTransformationClass {
  String[] value() default {};
  Class[] classes() default {};
}
