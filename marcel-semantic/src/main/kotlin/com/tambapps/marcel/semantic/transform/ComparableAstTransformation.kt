package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.Annotable
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.exception.MarcelAstTransformationException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.NotLoadedJavaType
import marcel.lang.comparable
import marcel.lang.data

/**
 * AST Transformation making a class implement
 */
class ComparableAstTransformation: GenerateMethodAstTransformation() {

  override fun doTransformType(javaType: NotLoadedJavaType, annotation: AnnotationNode, node: CstNode) {
    javaType.addImplementedInterface(java.lang.Comparable::class.javaType)
  }

  override fun generateSignatures(node: CstNode, javaType: NotLoadedJavaType, annotation: AnnotationNode): List<JavaMethod> {
    return listOf(
      signature(name = "compareTo", parameters = listOf(parameter(type = JavaType.Object, "obj")), returnType = JavaType.int),
      signature(name = "compareTo", parameters = listOf(parameter(type = javaType, "other")), returnType = JavaType.int)
    )
  }

  override fun generateMethodNodes(node: AstNode, classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    val compareTo = methodNode(
      name = "compareTo",
      parameters = listOf(parameter(type = classNode.type, "other")),
      returnType = JavaType.int) {
      val otherRef = argRef(0)
      val fields = classNode.fields.filter { !isAnnotableExcluded(it) && !it.isStatic }

      val lv = currentMethodScope.addLocalVariable(JavaType.int)
      val lvRef = ref(lv)
      for (field in fields) {
        ifStmt(isNotEqualExpr(varAssignExpr(lv, compare(ref(field), ref(field, owner = otherRef), field.name)), int(0))) {
          returnStmt(lvRef)
        }
      }

      if (annotation.getAttribute("includeGetters")?.value == true) {
        val methods = classNode.methods.filter { !isAnnotableExcluded(it) && !it.isStatic && it.isGetter }
        for (method in methods) {
          val ownProperty = fCall(method = method, owner = thisRef(), arguments = emptyList())
          val otherProperty = fCall(method = method, owner = otherRef, arguments = emptyList())
          ifStmt(isNotEqualExpr(varAssignExpr(lv, compare(ownProperty, otherProperty, method.propertyName)), int(0))) {
            returnStmt(lvRef)
          }
        }
      }
      returnStmt(int(0))
    }

    val compareToObject = methodNode(
      name = "compareTo",
      parameters = listOf(parameter(type = JavaType.Object, "obj")),
      returnType = JavaType.int) {
      returnStmt(fCall(owner = thisRef(), method = compareTo, arguments = listOf(cast(argRef(0), classNode.type))))
    }
    return listOf(compareToObject, compareTo)
  }

  // own and other should be of same type
  private fun compare(ownProperty: ExpressionNode, otherProperty: ExpressionNode, propertyName: String): ExpressionNode {
    val type = ownProperty.type
    return when  {
      type == JavaType.int || type == JavaType.float || type == JavaType.short || type == JavaType.long
          || type == JavaType.double || type == JavaType.byte
        -> minus(ownProperty, otherProperty)
      type.primitive -> fCall(name = "compare", ownerType = type.objectType, arguments = listOf(ownProperty, otherProperty))

      type.implements(java.lang.Comparable::class.javaType) ->
        fCall(owner = ownProperty, name = "compareTo", arguments = listOf(otherProperty))
      else -> throw MarcelAstTransformationException(this, ownProperty.token, "Property $propertyName is not comparable")
    }
  }

  private fun isAnnotableExcluded(annotable: Annotable): Boolean {
    return annotable.getAnnotation(comparable.Exclude::class.javaType) != null
        // useful because this can be run from a data annotation
        || annotable.getAnnotation(data.Exclude::class.javaType) != null
  }
}