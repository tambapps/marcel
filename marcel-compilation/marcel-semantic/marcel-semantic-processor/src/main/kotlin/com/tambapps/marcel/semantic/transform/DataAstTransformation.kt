package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.AnnotationNode

class DataAstTransformation :
  CompositeAstTransformation(
    listOf(
      StringifyAstTransformation(),
      EqualsAndHashcodeAstTransformation(),
      ComparableAstTransformation(),
      AllArgsConstructorAstTransformation()
    )
  ) {
  override fun shouldApply(transformation: SyntaxTreeTransformation, annotation: AnnotationNode) =
    when (transformation) {
      is StringifyAstTransformation -> annotation.getAttribute("stringify")?.value != false
      is ComparableAstTransformation -> annotation.getAttribute("comparable")?.value == true
      is AllArgsConstructorAstTransformation -> annotation.getAttribute("withConstructor")?.value == true
      else -> true
    }
}