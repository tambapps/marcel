package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.Annotable
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.extensions.javaAnnotationType
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.SourceJavaType
import marcel.lang.data
import java.util.Arrays
import java.util.Objects

/**
 * Transformation generating the class's equals and hashcode based on its properties
 */
class EqualsAndHashcodeAstTransformation : GenerateMethodAstTransformation() {

  override fun generateSignatures(
    node: CstNode,
    javaType: SourceJavaType,
    annotation: AnnotationNode
  ): List<MarcelMethod> {
    return listOf(
      signature(name = "equals", parameters = listOf(parameter(JavaType.Object, "obj")), returnType = JavaType.boolean),
      signature(name = "hashCode", returnType = JavaType.int)
    )
  }

  override fun generateMethodNodes(node: AstNode, classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    val fields = classNode.fields.filter { !isAnnotableExcluded(it) && !it.isStatic }
    val methods =
      // only handle method properties if field is enabled
      if (annotation.getAttribute("includeGetters")?.value == true)
        classNode.methods.filter { !isAnnotableExcluded(it) && !it.isStatic && it.isGetter }
      else emptyList()

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
        ifStmt(notEqual(ref(field), ref(field, owner = otherRef))) {
          returnStmt(bool(false))
        }
      }
      for (method in methods) {
        val ownProperty = fCall(method = method, owner = thisRef(), arguments = emptyList())
        val otherProperty = fCall(method = method, owner = otherRef, arguments = emptyList())
        ifStmt(notEqual(ownProperty, otherProperty)) {
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
      if (fields.isEmpty() && methods.isEmpty()) {
        returnStmt(fCall(name = "hashCode", owner = superRef(), arguments = emptyList()))
      } else {
        val resultVar = currentMethodScope.addLocalVariable(JavaType.int)
        varAssignStmt(
          resultVar,
          if (classNode.superType == JavaType.Object) int(0) else fCall(
            owner = superRef(),
            name = "hashCode",
            arguments = emptyList()
          )
        )
        for (field in fields) {
          varAssignStmt(resultVar, plus(mul(int(31), ref(resultVar)), hash(ref(field))))
        }
        for (method in methods) {
          val methodProperty = fCall(method = method, owner = thisRef(), arguments = emptyList())
          varAssignStmt(resultVar, plus(mul(int(31), ref(resultVar)), hash(methodProperty)))
        }
        returnStmt(ref(resultVar))
      }
    }
    return listOf(equalsMethod, hashCode)
  }

  private fun hash(propertyExpr: ExpressionNode): ExpressionNode {
    val type = propertyExpr.type
    return when {
      type.isArray -> fCall(
        name = "deepHashCode", ownerType = Arrays::class.javaType,
        arguments = listOf(propertyExpr)
      )

      type.primitive -> fCall(name = "hashCode", ownerType = type.objectType, arguments = listOf(propertyExpr))
      else -> fCall(ownerType = Objects::class.javaType, name = "hashCode", arguments = listOf(propertyExpr))
    }
  }

  private fun notEqual(
    ownPropertyRef: ExpressionNode,
    otherPropertyRef: ExpressionNode
  ): ExpressionNode {
    val type = ownPropertyRef.type
    return when {
      type.isArray -> notExpr(
        fCall(
          name = "deepEquals",
          ownerType = Arrays::class.javaType,
          arguments = listOf(ownPropertyRef, otherPropertyRef)
        )
      )

      type == JavaType.float || type == JavaType.double || type == JavaType.boolean ->
        isNotEqualExpr(
          fCall(name = "compare", ownerType = type.objectType, arguments = listOf(ownPropertyRef, otherPropertyRef)),
          int(0)
        )

      else -> notExpr(
        fCall(
          ownerType = Objects::class.javaType,
          name = "equals",
          arguments = listOf(ownPropertyRef, otherPropertyRef)
        )
      )
    }
  }

  private fun isAnnotableExcluded(annotable: Annotable): Boolean {
    return annotable.getAnnotation(data.Exclude::class.javaType) != null
  }
}