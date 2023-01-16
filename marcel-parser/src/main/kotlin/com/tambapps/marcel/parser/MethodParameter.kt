package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.ast.TypedNode
import com.tambapps.marcel.parser.type.JavaType

data class MethodParameter(override val type: JavaType, val name: String): TypedNode {
  override fun toString(): String {
    return "$type $name"
  }
}