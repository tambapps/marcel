package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.type.JavaClassType

object Types {
  val OBJECT = JavaClassType(Object::class.java)
  val STRING_ARRAY = JavaClassType(Array<String>::class.java)
  val STRING = JavaClassType(String::class.java)

}