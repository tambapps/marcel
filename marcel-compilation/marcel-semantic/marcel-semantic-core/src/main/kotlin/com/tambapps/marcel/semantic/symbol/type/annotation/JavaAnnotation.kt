package com.tambapps.marcel.semantic.symbol.type.annotation

import com.tambapps.marcel.semantic.symbol.type.JavaAnnotationType
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.JavaTyped

/**
 * Abstraction of a Java annotation
 *
 * @property type the [JavaType] representing the annotation
 *
 */
interface JavaAnnotation: JavaTyped {

  override val type: JavaAnnotationType

  /**
   * Get the attribute of the annotation
   *
   * @param name the attribute name
   * @return the attribute or null if it doesn't exist
   */
  fun getAttribute(name: String): Attribute?


  /**
   * Java annotation attribute
   *
   * @property name the name of the attribute
   * @property type the type of the attribute
   * @property value the value of the attribute
   * @constructor Create a new attribute
   */
  data class Attribute(val name: String, val type: JavaType, val value: Any)

}