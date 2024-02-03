package com.tambapps.marcel.semantic.type

/**
 * [JavaType] representing an array
 */
interface JavaArrayType: JavaType {
  val elementsType: JavaType
}