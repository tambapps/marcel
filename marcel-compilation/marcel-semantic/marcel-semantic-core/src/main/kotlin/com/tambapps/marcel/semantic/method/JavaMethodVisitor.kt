package com.tambapps.marcel.semantic.method

interface JavaMethodVisitor<T> {

  fun visit(method: MarcelMethod): T
  fun visit(method: CastMethod): T

}