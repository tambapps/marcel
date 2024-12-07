package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.AnnotationNode

class DataAstTransformation :
  CompositeAstTransformation(
    listOf(
      StringifyAstTransformation(),
      EqualsAndHashcodeAstTransformation(),
      ComparableAstTransformation(),
    )
  ) {
  override fun shouldApply(transformation: SyntaxTreeTransformation, annotation: AnnotationNode) =
    when (transformation) {
      is StringifyAstTransformation -> annotation.getAttribute("stringify")?.value != false
      is ComparableAstTransformation -> annotation.getAttribute("comparable")?.value == true
      else -> true
    }
}