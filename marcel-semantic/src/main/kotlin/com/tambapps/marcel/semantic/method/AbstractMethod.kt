package com.tambapps.marcel.semantic.method

import java.util.*

abstract class AbstractMethod: JavaMethod {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JavaMethod) return false
    if (name != other.name) return false
    if (parameters != other.parameters) return false
    if (returnType != other.returnType) return false
    return true
  }

  override fun hashCode(): Int {
    return Objects.hash(name, parameters, returnType)
  }

  override fun toString(): String {
    return "fun $returnType $name(" + parameters.joinToString(separator = ",") + ")"
  }
}
