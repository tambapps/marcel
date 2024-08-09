package com.tambapps.marcel.semantic.method

interface JavaMethodVisitor<T> {

  fun visit(method: JavaMethod): T
  fun visit(method: CastMethod): T

}