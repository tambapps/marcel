package com.tambapps.marcel.semantic.symbol.method

interface MarcelMethodVisitor<T> {

  fun visit(method: MarcelMethod): T
  fun visit(method: CastMethod): T

}