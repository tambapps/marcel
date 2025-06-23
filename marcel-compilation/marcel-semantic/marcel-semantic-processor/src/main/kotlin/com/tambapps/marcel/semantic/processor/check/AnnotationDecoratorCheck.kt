package com.tambapps.marcel.semantic.processor.check

import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.extensions.javaAnnotationType
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.processor.visitor.ClassNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.type.annotation.JavaAnnotation
import marcel.util.concurrent.Async
import org.jspecify.annotations.NullMarked
import org.jspecify.annotations.Nullable

/**
 * Check that adds annotation to reflect Marcel features in the bytecode if the class were to be compiled
 */
class AnnotationDecoratorCheck: ClassNodeVisitor {
  companion object {
    val NULL_MARKED_TYPE = NullMarked::class.javaAnnotationType
    val NULLABLE_TYPE = Nullable::class.javaAnnotationType
  }
  override fun visit(
    classNode: ClassNode,
    symbolResolver: MarcelSymbolResolver
  ) {
    if (classNode.annotations.none { it.type == NULL_MARKED_TYPE }) {
      classNode.annotations.add(AnnotationNode(NULL_MARKED_TYPE, listOf(), classNode))
    }

    for (field in classNode.fields) {
      annotateNullableIfNecessary(field, field.nullness, field.annotations)
    }
    for (method in classNode.methods) {
      annotateNullableIfNecessary(method, method.nullness, method.annotations)
      for (parameter in method.parameters) {
        annotateNullableIfNecessary(method, parameter.nullness, parameter.annotations)
      }
      // check async
      if (method.isAsync) {
        method.annotations.add(AnnotationNode(
          type = Async::class.javaAnnotationType,
          node = method,
          attributes = listOf(
            JavaAnnotation.Attribute("returnType", JavaType.Clazz, method.returnType.genericTypes.firstOrNull()?.objectType ?: JavaType.Object)
          ),
        )
        )
      }
    }
  }

  private fun annotateNullableIfNecessary(node: AstNode, nullness: Nullness, annotations: MutableList<AnnotationNode>) {
    if (nullness == Nullness.NULLABLE && annotations.none { it.type == NULLABLE_TYPE }) {
      annotations.add(AnnotationNode(NULLABLE_TYPE, listOf(), node))
    }
  }
}