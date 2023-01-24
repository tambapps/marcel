package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.type.JavaType

interface Variable {

  val type: JavaType
  val name: String
}

class LocalVariable(override val type: JavaType, override val name: String): Variable {

  // yup, long and doubles takes 2 variable slots
  val nbSlots = if (type == JavaType.long || type == JavaType.double) 2 else 1
  override fun toString(): String {
    return "LocalVariable(type=$type, name='$name')"
  }

}

class ClassField(override val type: JavaType, override val name: String, val owner: JavaType): Variable

// for getter/setters
class MethodField(override val type: JavaType, override val name: String, val owner: JavaType): Variable