package com.tambapps.marcel.compiler.util

import com.tambapps.marcel.semantic.Visibility
import java.lang.reflect.Modifier

object ReflectUtils {


  fun computeAccess(visibility: Visibility, isStatic: Boolean, isAbstract: Boolean = false, isFinal: Boolean = false,
                    isSynchronized: Boolean = false,
                    isTransient: Boolean = false): Int {
    var result = when (visibility) {
      Visibility.PUBLIC -> Modifier.PUBLIC
      Visibility.PROTECTED -> Modifier.PROTECTED
      Visibility.PRIVATE -> Modifier.PRIVATE
      Visibility.INTERNAL -> 0
    }
    if (isStatic) result = result or Modifier.STATIC
    if (isAbstract) result = result or Modifier.ABSTRACT
    if (isFinal) result = result or Modifier.FINAL
    if (isSynchronized) result = result or Modifier.SYNCHRONIZED
    if (isTransient) result = result or Modifier.TRANSIENT
    return result
  }
}