package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

/**
 * Base class for AST transformations adding methods to classes
 */
abstract class GenerateMethodAstTransformation: AbstractAstTransformation() {
  final override fun transformType(javaType: NotLoadedJavaType, annotation: AnnotationNode) {
    useScope(ClassScope(typeResolver, javaType, null, emptyList())) {
      doTransformType(javaType, annotation)
      generateSignatures(javaType, annotation).forEach { signature ->
        typeResolver.defineMethod(javaType, signature)
      }
    }
  }

  final override fun transform(node: AstNode, annotation: AnnotationNode) {
    val classNode = getClassNode(node)
    useScope(ClassScope(typeResolver, classNode.type, null, emptyList())) {
      val methods = generateMethodNodes(classNode, annotation)
      for (method in methods) {
        val duplicate = classNode.methods.find { it.matches(typeResolver, method.name, method.parameters, strict = true) }
        if (duplicate != null) {
          // TODO custom exception
          throw MarcelSemanticException(duplicate.token, "Method with $method is already defined")
        }
        classNode.methods.add(method)
      }
    }
  }

  protected open fun doTransformType(javaType: NotLoadedJavaType, annotation: AnnotationNode) {}
  protected open fun getClassNode(node: AstNode): ClassNode = node as ClassNode

  /**
   * Returns the list of the method signatures that will be added by this AST transformation.
   * The signatures must be in the same order as the method nodes
   */
  protected abstract fun generateSignatures(javaType: NotLoadedJavaType, annotation: AnnotationNode): List<JavaMethod>

  /**
   * Returns the list of the method nodes that will be added by this AST transformation.
   * The nodes must be in the same order as the method signatures
   */
  protected abstract fun generateMethodNodes(classNode: ClassNode, annotation: AnnotationNode): List<MethodNode>
}