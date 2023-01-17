package com.tambapps.marcel.parser.type

interface JavaMethod {
  val descriptor: String
  val parameterTypes: Array<Class<*>>
}