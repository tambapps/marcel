package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.ast.Annotable
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
import marcel.lang.data
import java.util.Arrays

/**
 * Transformation generating the class's equals and hashcode based on its properties
 */
class EqualsAndHashcodeAstTransformation: GenerateMethodAstTransformation() {

  override fun generateSignatures(javaType: NotLoadedJavaType, annotation: AnnotationNode): List<JavaMethod> {
    return listOf(
      signature(name = "equals", parameters = listOf(parameter(JavaType.Object, "obj")), returnType = JavaType.boolean),
      signature(name = "hashCode", returnType = JavaType.int)
    )
  }

  override fun generateMethodNodes(classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    val fields = classNode.fields.filter { !isAnnotableExcluded(it) && !it.isStatic }
    val equalsMethod = methodNode(
      ownerClass = classNode.type, name = "equals",
      parameters = listOf(parameter(JavaType.Object, "obj")),
      returnType = JavaType.boolean,
      annotations = listOf(annotationNode(Override::class.javaAnnotationType))
    ) {
      val argRef = lvRef("obj")
      ifStmt(isEqualExpr(argRef, thisRef())) {
        returnStmt(bool(true))
      }
      if (classNode.superType != JavaType.Object) {
        ifStmt(notExpr(fCall(name = "equals", owner = superRef(), arguments = listOf(argRef)))) {
          returnStmt(bool(false))
        }
      }
      ifStmt(notExpr(isInstanceExpr(classNode.type, argRef))) {
        returnStmt(bool(false))
      }
      // now we know it is the right type
      val otherVar = currentMethodScope.addLocalVariable(classNode.type)
      varAssignStmt(otherVar, argRef)

      val otherRef = ref(otherVar)
      for (field in fields) {
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
      if (fields.isEmpty()) {
        returnStmt(fCall(name = "hashCode", owner = superRef(), arguments = emptyList()))
      } else {
        val resultVar = currentMethodScope.addLocalVariable(JavaType.int)
        var i = 0
        varAssignStmt(resultVar, if (classNode.superType == JavaType.Object) hash(fields[i++]) else TODO("super.hashCode()"))
        while (i < fields.size) {
          varAssignStmt(resultVar, plus(mul(int(31), ref(resultVar)), hash(fields[i++])))
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

      // TODO handle variables being nullable
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
      fieldNode.type == JavaType.float || fieldNode.type == JavaType.double || fieldNode.type == JavaType.boolean ->
        isNotEqualExpr(
          fCall(name = "compare", ownerType = fieldNode.type.objectType, arguments = listOf(ownFieldRef, otherFieldRef)),
          int(0)
        )
      // TODO we want equals(...) call
      // TODO handle variables being nullable
      else -> IsNotEqualNode(ownFieldRef, otherFieldRef)
    }
  }
  private fun isAnnotableExcluded(annotable: Annotable): Boolean {
    return annotable.getAnnotation(data.Exclude::class.javaType) != null
  }
}