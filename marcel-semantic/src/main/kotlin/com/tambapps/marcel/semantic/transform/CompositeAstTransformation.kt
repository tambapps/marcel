package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

abstract class CompositeAstTransformation(
  private val transformations: List<AstTransformation>
): AbstractAstTransformation() {

  override fun init(typeResolver: JavaTypeResolver) {
    super.init(typeResolver)
    for (t in transformations) {
      t.init(typeResolver)
    }
  }

  override fun transformType(javaType: NotLoadedJavaType, annotation: AnnotationNode, node: CstNode) {
    for (t in transformations) {
      if (shouldApply(t, annotation)) t.transformType(javaType, annotation, node)
    }
  }

  override fun transform(node: AstNode, classNode: ClassNode, annotation: AnnotationNode) {
    for (t in transformations) {
      if (shouldApply(t, annotation)) t.transform(node, classNode, annotation)
    }
  }

  protected abstract fun shouldApply(transformation: AstTransformation, annotation: AnnotationNode): Boolean

}