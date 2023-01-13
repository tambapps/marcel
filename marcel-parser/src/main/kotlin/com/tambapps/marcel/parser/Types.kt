package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.type.ClassType

object Types {
  val VOID = ClassType(Void::class.javaPrimitiveType!!)
  val OBJECT = ClassType(Object::class.java)
  val STRING_ARRAY = ClassType(Array<String>::class.java)
  val STRING = ClassType(String::class.java)
  val INT = ClassType(Int::class.javaPrimitiveType!!)
  // one day I'll do the others


}