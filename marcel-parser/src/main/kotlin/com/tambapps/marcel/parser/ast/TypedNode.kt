package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.type.JavaType

// TODO rename this to TypedObject
interface TypedNode {
  val type: JavaType

}