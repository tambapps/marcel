package com.tambapps.marcel.parser.type

// TODO add an imple for dynamic types e.g. class defined in a marcel script
interface JavaType {

  val className: String
  val internalName: String
  val descriptor: String
  val storeCode: Int
  val loadCode: Int
}