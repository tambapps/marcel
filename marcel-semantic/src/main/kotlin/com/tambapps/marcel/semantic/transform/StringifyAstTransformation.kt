package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

/**
 * AST transformation to generate toString method. Implicitely referenced in stringify annotation
 */
class StringifyAstTransformation: GenerateMethodAstTransformation() {

  override fun generateSignatures(javaType: NotLoadedJavaType, annotation: AnnotationNode) = listOf(
    signature(ownerClass = javaType, name = "toString", returnType = JavaType.String)
  )

  override fun generateMethodNodes(classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    // TODO
    val methodNode = methodNode(ownerClass = classNode.type, name = "toString", returnType = JavaType.String,
      statements = mutableListOf(
        returnStatement(string("TODO"), JavaType.String)
      )
    )
    return listOf(methodNode)
  }
}