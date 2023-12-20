package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.exception.MarcelAstTransformationException

import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

/**
 * AST Transformations caching results of the annotated method
 */
class CachedAstTransformation: GenerateMethodAstTransformation() {

  override fun generateSignatures(node: CstNode, javaType: NotLoadedJavaType, annotation: AnnotationNode): List<JavaMethod> {
    // as the generated method is private and only called for this caching usecase, no need to define it here
    //  (it would be anyway hard to do so as we don't have the AST node here)
    return emptyList()
  }

  override fun generateMethodNodes(node: AstNode, classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    val originalMethod = node as MethodNode
    if (originalMethod.isConstructor) throw MarcelAstTransformationException(this, node.token, "Annotated method is a constructor")
    if (originalMethod.returnType == JavaType.void) throw MarcelAstTransformationException(this, node.token, "Cannot cache void methods")
    if (originalMethod.parameters.isEmpty()) throw MarcelAstTransformationException(this, node.token, "Cannot cache methods with no parameters")
    val newMethodName = "do" + originalMethod.name.first().uppercase() + originalMethod.name.substring(1)

    val doComputeMethod = methodNode(visibility = Visibility.PRIVATE, name = newMethodName, parameters = originalMethod.parameters, returnType = originalMethod.returnType) {
      addAllStmt(originalMethod.blockStatement.statements)
    }

    // TODO modify the original method
    return listOf(doComputeMethod)
  }
}