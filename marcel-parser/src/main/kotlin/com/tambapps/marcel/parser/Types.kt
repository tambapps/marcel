package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.type.ClassType

object Types {
  val VOID = ClassType(Void::class.javaPrimitiveType!!)
  val OBJECT = ClassType(Object::class.java)
  val STRING_ARRAY = ClassType(Array<String>::class.java)
  val STRING = ClassType(String::class.java)
  /// TODO put this in a PrimitiveTypes object
  val INT = Int::class.javaPrimitiveType!!
  val LONG = Long::class.javaPrimitiveType!!
  val FLOAT = Float::class.javaPrimitiveType!!
  val DOUBLE = Double::class.javaPrimitiveType!!
  val VOID_P = Void::class.javaPrimitiveType!!

  // one day I'll do the others


}