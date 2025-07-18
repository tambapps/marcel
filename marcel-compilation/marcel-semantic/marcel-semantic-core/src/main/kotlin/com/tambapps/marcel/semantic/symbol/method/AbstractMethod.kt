package com.tambapps.marcel.semantic.symbol.method

import com.tambapps.marcel.semantic.symbol.Visibility
import java.util.*

abstract class AbstractMethod: MarcelMethod {

  // used for the toString method, overridden by ExtensionMarcelMethod
  protected open val ownerString get() = ownerClass.simpleName

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MarcelMethod) return false
    if (name != other.name) return false
    if (isConstructor && ownerClass != other.ownerClass) return false
    if (parameters != other.parameters) return false
    if (returnType != other.returnType) return false
    return true
  }

  override fun hashCode(): Int {
    return Objects.hash(ownerClass, name, parameters, returnType)
  }

  override fun toString() = StringBuilder().apply {
    if (visibility != Visibility.PUBLIC) {
      append(visibility.name.lowercase())
      append(" ")
    }
    if (isConstructor) {
      append(ownerClass.simpleName)
    } else {
      if (isAbstract) append("abstract ")
      if (isMarcelStatic) append("static ")
      if (isAsync) append("async ")
      append("fun ")
      append(returnType.simpleName)
      append(" ")
      append(ownerString)
      append(".")
      append(name)
    }
    append(parameters.joinToString(separator = ",", prefix = "(", postfix = ")"))
  }.toString()
}
