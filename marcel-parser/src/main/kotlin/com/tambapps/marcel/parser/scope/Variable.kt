package com.tambapps.marcel.parser.scope

import com.tambapps.marcel.parser.type.JavaType

interface Variable {

  val type: JavaType
  val name: String
}

class LocalVariable(override val type: JavaType, override val name: String): Variable {
  override fun toString(): String {
    return "LocalVariable(type=$type, name='$name')"
  }

}

class Field(override val type: JavaType, override val name: String, val owner: JavaType): Variable