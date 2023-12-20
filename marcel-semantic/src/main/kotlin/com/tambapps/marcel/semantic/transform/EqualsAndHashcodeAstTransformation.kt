package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsNotEqualNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.NotLoadedJavaType
import java.util.Arrays

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
    // TODO annotate with override. Same for toString method in stringify
    val equalsMethod = methodNode(ownerClass = classNode.type, name = "equals",
      parameters = listOf(parameter(JavaType.Object, "obj")),
      returnType = JavaType.boolean) {
      val argRef = lvRef("obj")
      ifStmt(isExpr(argRef, thisRef())) {
        returnStmt(bool(true))
      }
      ifStmt(notExpr(isInstanceExpr(classNode.type, argRef))) {
        returnStmt(bool(false))
      }
      // now we know it is the right type
      val otherVar = currentMethodScope.addLocalVariable(classNode.type)
      varAssignStmt(otherVar, argRef)

      if (classNode.superType != JavaType.Object) {
        TODO("Add super.equals(argRef)")
      }

      val otherRef = ref(otherVar)
      for (field in classNode.fields) {
        ifStmt(notEqual(field, otherRef)) {
          returnStmt(bool(false))
        }
      }
      returnStmt(bool(true))
    }
    val hashCode = methodNode(ownerClass = classNode.type, name = "hashCode", returnType = JavaType.int) {
      // TODO
      returnStmt(int(1))
    }
    return listOf(equalsMethod, hashCode)
  }

  private fun notEqual(fieldNode: FieldNode, argRef: ReferenceNode): ExpressionNode {
    val ownFieldRef = ref(fieldNode)
    val otherFieldRef = ref(fieldNode, owner = argRef)
    return when {
      fieldNode.type.isArray -> notExpr(
        fCall(name = "deepEquals", ownerType = Arrays::class.javaType, arguments = listOf(ownFieldRef, otherFieldRef))
      )
      fieldNode.type == JavaType.float || fieldNode.type == JavaType.double ->
        IsNotEqualNode(
          fCall(name = "compare", ownerType = fieldNode.type.objectType, arguments = listOf(ownFieldRef, otherFieldRef)),
          int(0)
        )
      else -> IsNotEqualNode(ownFieldRef, otherFieldRef)
    }
  }
}