package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

/**
 * Transformation generating the class's equals and hashcode based on its properties
 */
class EqualsAndHashcodeAstTransformation: GenerateMethodAstTransformation() {

  override fun generateSignatures(javaType: NotLoadedJavaType, annotation: AnnotationNode): List<JavaMethod> {
    return listOf(
      signature(ownerClass = javaType, name = "equals", parameters = listOf(parameter(JavaType.Object, "obj")), returnType = JavaType.boolean),
      signature(ownerClass = javaType, name = "hashCode", returnType = JavaType.int)
    )
  }

  override fun generateMethodNodes(classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    val equalsMethod = methodNode(ownerClass = classNode.type, name = "equals", returnType = JavaType.boolean) {
      // TODO
      returnStmt(bool(true))
    }
    val hashCode = methodNode(ownerClass = classNode.type, name = "hashCode", returnType = JavaType.int) {
      // TODO
      returnStmt(int(1))
    }
    return listOf(equalsMethod, hashCode)
  }

  override fun equals(other: Any?): Boolean {
    return super.equals(other)
  }

  fun equals(other: EqualsAndHashcodeAstTransformation): Boolean {
    return super.equals(other)
  }

  override fun hashCode(): Int {
    return super.hashCode()
  }
}