package com.tambapps.marcel.compiler.util

import com.tambapps.marcel.semantic.symbol.Visibility
import org.objectweb.asm.Opcodes
import java.lang.reflect.Modifier

object ReflectUtils {


  fun computeAccess(visibility: Visibility? = null,
                    isStatic: Boolean = false,
                    isAbstract: Boolean = false,
                    isFinal: Boolean = false,
                    isSynchronized: Boolean = false,
                    isTransient: Boolean = false,
                    isSynthetic: Boolean = false,
                    isEnum: Boolean = false,
                    isInterface: Boolean = false,
                    isSuper: Boolean = false,
                    isVarArgs: Boolean = false): Int {
    var result = 0
    if (visibility != null) {
      result = result or when (visibility) {
        Visibility.PUBLIC -> Modifier.PUBLIC
        Visibility.PROTECTED -> Modifier.PROTECTED
        Visibility.PRIVATE -> Modifier.PRIVATE
        Visibility.INTERNAL -> 0
      }
    }
    if (isStatic) result = result or Opcodes.ACC_STATIC
    if (isAbstract || isInterface) result = result or Opcodes.ACC_ABSTRACT
    if (isInterface) result = result or Opcodes.ACC_INTERFACE
    if (isFinal) result = result or Opcodes.ACC_FINAL
    if (isSynchronized) result = result or Opcodes.ACC_SYNCHRONIZED
    if (isTransient) result = result or Opcodes.ACC_TRANSIENT
    if (isSynthetic) result = result or Opcodes.ACC_SYNTHETIC
    if (isEnum) result = result or Opcodes.ACC_ENUM
    if (isVarArgs) result = result or Opcodes.ACC_VARARGS
    if (isSuper) result = result or Opcodes.ACC_SUPER
    return result
  }
}