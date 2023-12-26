package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.exception.MarcelAstTransformationException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

/**
 * Base class for AST transformations adding methods to classes
 */
abstract class GenerateMethodAstTransformation: AbstractAstTransformation() {
  final override fun transformType(javaType: NotLoadedJavaType, annotation: AnnotationNode, node: CstNode) {
    useScope(classScope(javaType)) {
      doTransformType(javaType, annotation)
      generateSignatures(node, javaType, annotation).forEach { signature ->
        typeResolver.defineMethod(javaType, signature)
      }
    }
  }

  final override fun transform(node: AstNode, classNode: ClassNode, annotation: AnnotationNode) {
    useScope(classScope(classNode)) {
      val methods = generateMethodNodes(node, classNode, annotation)
      for (method in methods) {

        val duplicate = classNode.methods.find { method.matches(it) }
        if (duplicate != null) {
          throw MarcelAstTransformationException(this, duplicate.token, "Method with $method is already defined")
        }
        classNode.methods.add(method)
      }
    }
  }

  protected open fun doTransformType(javaType: NotLoadedJavaType, annotation: AnnotationNode) {}

  /**
   * Returns the list of the method signatures that will be added by this AST transformation.
   * The signatures must be in the same order as the method nodes
   */
  protected abstract fun generateSignatures(node: CstNode, javaType: NotLoadedJavaType, annotation: AnnotationNode): List<JavaMethod>

  /**
   * Returns the list of the method nodes that will be added by this AST transformation.
   * The nodes must be in the same order as the method signatures
   */
  protected abstract fun generateMethodNodes(node: AstNode, classNode: ClassNode, annotation: AnnotationNode): List<MethodNode>
}