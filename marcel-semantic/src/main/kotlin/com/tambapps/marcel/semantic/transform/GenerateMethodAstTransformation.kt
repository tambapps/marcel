package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.Ast2Node
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

/**
 * Base class for AST transformations adding methods to classes
 */
abstract class GenerateMethodAstTransformation: AbstractAstTransformation() {
  final override fun transformType(javaType: NotLoadedJavaType, annotation: AnnotationNode) {
    generateSignatures(javaType, annotation).forEach { signature ->
      typeResolver.defineMethod(javaType, signature)
    }
  }

  final override fun transform(node: Ast2Node, annotation: AnnotationNode) {
    println("Transform node $node with annotation $annotation")

    val classNode = getClassNode(node)
    classNode.methods.addAll(generateMethodNodes(classNode, annotation))
  }

  protected open fun getClassNode(node: Ast2Node): ClassNode = node as ClassNode

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