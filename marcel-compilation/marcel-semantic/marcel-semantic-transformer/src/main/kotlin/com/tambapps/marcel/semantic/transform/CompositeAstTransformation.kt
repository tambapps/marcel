package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.SemanticPurpose
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.type.SourceJavaType

abstract class CompositeAstTransformation(
  private val transformations: List<SyntaxTreeTransformation>
) : AbstractAstTransformation() {

  override fun init(symbolResolver: MarcelSymbolResolver, purpose: SemanticPurpose) {
    super.init(symbolResolver, purpose)
    for (t in transformations) {
      t.init(symbolResolver, purpose)
    }
  }

  override fun transform(javaType: SourceJavaType, node: CstNode, annotation: AnnotationNode) {
    for (t in transformations) {
      if (shouldApply(t, annotation)) t.transform(javaType, node, annotation)
    }
  }

  override fun transform(node: AstNode, classNode: ClassNode, annotation: AnnotationNode) {
    for (t in transformations) {
      if (shouldApply(t, annotation)) t.transform(node, classNode, annotation)
    }
  }

  protected abstract fun shouldApply(transformation: SyntaxTreeTransformation, annotation: AnnotationNode): Boolean

}