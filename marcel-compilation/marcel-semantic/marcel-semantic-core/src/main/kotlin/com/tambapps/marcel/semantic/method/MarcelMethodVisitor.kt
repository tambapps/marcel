package com.tambapps.marcel.semantic.method

interface MarcelMethodVisitor<T> {

  fun visit(method: MarcelMethod): T
  fun visit(method: CastMethod): T

}