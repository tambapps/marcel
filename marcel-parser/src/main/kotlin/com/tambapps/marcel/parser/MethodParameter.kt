package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.type.JavaType

data class MethodParameter(val type: JavaType, val name: String) {
  override fun toString(): String {
    return "$type $name"
  }
}