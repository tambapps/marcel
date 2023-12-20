package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsNotEqualNode
import com.tambapps.marcel.semantic.extensions.javaAnnotationType
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
    val equalsMethod = methodNode(
      ownerClass = classNode.type, name = "equals",
      parameters = listOf(parameter(JavaType.Object, "obj")),
      returnType = JavaType.boolean,
      annotations = listOf(annotationNode(Override::class.javaAnnotationType))
    ) {
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
    val hashCode = methodNode(
      ownerClass = classNode.type,
      name = "hashCode",
      returnType = JavaType.int,
      annotations = listOf(annotationNode(Override::class.javaAnnotationType))
    ) {
      if (classNode.fields.isEmpty()) {
        TODO("super.hashCode()")
      } else {
        val resultVar = currentMethodScope.addLocalVariable(JavaType.int)
        var i = 0
        varAssignStmt(resultVar, if (classNode.superType == JavaType.Object) hash(classNode.fields[i++]) else TODO("super.hashCode()"))
        while (i < classNode.fields.size) {
          varAssignStmt(resultVar, plus(mul(int(31), ref(resultVar)), hash(classNode.fields[i++])))
        }
        returnStmt(ref(resultVar))
      }
    }
    return listOf(equalsMethod, hashCode)
  }

  private fun hash(fieldNode: FieldNode): ExpressionNode {
    return when {
      fieldNode.type.isArray -> fCall(name = "deepHashCode", ownerType = Arrays::class.javaType,
        arguments = listOf(ref(fieldNode)))
      fieldNode.type.primitive -> fCall(name = "hashCode", ownerType = fieldNode.type.objectType, arguments = listOf(ref(fieldNode)))
      else -> fCall(name = "hashCode", owner = ref(fieldNode), arguments = emptyList())
    }
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