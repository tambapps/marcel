package com.tambapps.marcel.compiler.util

import com.tambapps.marcel.semantic.Visibility
import org.objectweb.asm.Opcodes
import java.lang.reflect.Modifier

object ReflectUtils {


  fun computeAccess(visibility: Visibility, isStatic: Boolean, isAbstract: Boolean = false, isFinal: Boolean = false,
                    isSynchronized: Boolean = false,
                    isTransient: Boolean = false,
                    isSynthetic: Boolean = false,
                    isVarArgs: Boolean = false): Int {
    var result = when (visibility) {
      Visibility.PUBLIC -> Modifier.PUBLIC
      Visibility.PROTECTED -> Modifier.PROTECTED
      Visibility.PRIVATE -> Modifier.PRIVATE
      Visibility.INTERNAL -> 0
    }
    if (isStatic) result = result or Opcodes.ACC_STATIC
    if (isAbstract) result = result or Opcodes.ACC_ABSTRACT
    if (isFinal) result = result or Opcodes.ACC_FINAL
    if (isSynchronized) result = result or Opcodes.ACC_SYNCHRONIZED
    if (isTransient) result = result or Opcodes.ACC_TRANSIENT
    if (isSynthetic) result = result or Opcodes.ACC_SYNTHETIC
    if (isVarArgs) result = result or Opcodes.ACC_VARARGS
    return result
  }
}