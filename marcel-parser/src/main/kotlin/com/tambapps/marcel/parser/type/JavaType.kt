package com.tambapps.marcel.parser.type

// TODO add an imple for dynamic types e.g. class defined in a marcel script
interface JavaType {

  val name: String
  val internalName: String
  val descriptor: String
}