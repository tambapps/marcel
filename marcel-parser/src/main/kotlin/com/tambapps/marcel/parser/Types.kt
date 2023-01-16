package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.type.JavaType

object Types {
  val OBJECT = JavaType(Object::class.java)
  val STRING_ARRAY = JavaType(Array<String>::class.java)
  val STRING = JavaType(String::class.java)

}