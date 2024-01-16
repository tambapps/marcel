package com.tambapps.marcel.semantic.method

import java.util.*

abstract class AbstractMethod: JavaMethod {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JavaMethod) return false
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
    if (isConstructor) {
      append(ownerClass.simpleName)
    } else {
      if (isAbstract) append("abstract ")
      if (isStatic) append("static ")
      if (isAsync) append("async ")
      append("fun ")
      append(returnType)
      append(" ")
      append(ownerClass.simpleName)
      append(".")
      append(name)
    }
    append(parameters.joinToString(separator = ",", prefix = "(", postfix = ")"))
  }.toString()
}
