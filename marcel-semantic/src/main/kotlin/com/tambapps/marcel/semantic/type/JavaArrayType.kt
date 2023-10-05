package com.tambapps.marcel.semantic.type

interface JavaArrayType: JavaType {
  val elementsType: JavaType
}