package com.tambapps.marcel.semantic.symbol.type

/**
 * [JavaType] representing an array
 */
interface JavaArrayType: JavaType {
  val elementsType: JavaType

  override val simpleName: String
    get() = "${elementsType.simpleName}[]"
}