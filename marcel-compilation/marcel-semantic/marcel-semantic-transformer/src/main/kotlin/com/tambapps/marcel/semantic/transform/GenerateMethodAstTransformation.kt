package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.exception.MarcelSyntaxTreeTransformationException
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.type.SourceJavaType

/**
 * Base class for AST transformations adding methods to classes
 */
abstract class GenerateMethodAstTransformation : AbstractAstTransformation() {
  override fun transform(javaType: SourceJavaType, node: CstNode, annotation: AnnotationNode) {
    useScope(classScope(javaType)) {
      doTransformType(javaType, annotation, node)
      generateSignatures(node, javaType, annotation).forEach { signature ->
        symbolResolver.defineMethod(javaType, signature)
      }
    }
  }

  final override fun transform(node: AstNode, classNode: ClassNode, annotation: AnnotationNode) {
    useScope(classScope(classNode)) {
      val methods = generateMethodNodes(node, classNode, annotation)
      for (method in methods) {

        val duplicate = classNode.methods.find { method.matches(it) }
        if (duplicate != null) {
          throw MarcelSyntaxTreeTransformationException(this, duplicate.token, "Method with $method is already defined")
        }
        classNode.methods.add(method)
      }
    }
  }

  protected open fun doTransformType(javaType: SourceJavaType, annotation: AnnotationNode, node: CstNode) {}

  /**
   * Returns the list of the method signatures that will be added by this AST transformation.
   * The signatures must be in the same order as the method nodes
   */
  protected open fun generateSignatures(
    node: CstNode,
    javaType: SourceJavaType,
    annotation: AnnotationNode
  ): List<MarcelMethod> {
    return emptyList()
  }

  /**
   * Returns the list of the method nodes that will be added by this AST transformation.
   * The nodes must be in the same order as the method signatures
   */
  protected abstract fun generateMethodNodes(
    node: AstNode,
    classNode: ClassNode,
    annotation: AnnotationNode
  ): List<MethodNode>
}