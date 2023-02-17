package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.type.JavaType

data class MethodParameter constructor(override val type: JavaType, val rawType: JavaType, val name: String): AstTypedObject {
  constructor(type: JavaType, name: String): this(type, type, name)

  override fun toString(): String {
    return "$type $name"
  }
}