package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.extensions.javaAnnotationType
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.NotLoadedJavaType
import marcel.lang.stringify
import java.util.Arrays

/**
 * AST transformation to generate toString method. Implicitly referenced in stringify annotation
 */
class StringifyAstTransformation: GenerateMethodAstTransformation() {

  override fun generateSignatures(javaType: NotLoadedJavaType, annotation: AnnotationNode) = listOf(
    signature(ownerClass = javaType, name = "toString", returnType = JavaType.String)
  )

  override fun generateMethodNodes(classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    val stringParts = mutableListOf<ExpressionNode>(
      string(classNode.type.simpleName + "(")
    )
    if (classNode.superType != JavaType.Object) {
      stringParts.add(string("super="))
      stringParts.add(toString(fCall(name = "toString", owner = superRef(), arguments = emptyList())))
    }

    for (field in classNode.fields) {
      if (field.getAnnotation(stringify.Exclude::class.javaType) != null
        || field.isStatic || field.visibility != Visibility.PUBLIC) continue
      stringParts.add(string(field.name + "="))
      stringParts.add(toString(ref(field)))
      stringParts.add(string(", "))
    }
    if (annotation.getAttribute("includeGetters")?.value == true) {
      for (method in classNode.methods) {
        if (method.getAnnotation(stringify.Exclude::class.javaType) != null || !method.isGetter
          || method.isStatic || method.visibility != Visibility.PUBLIC) continue
        stringParts.add(string(method.propertyName + "="))
        stringParts.add(toString(fCall(
          owner = thisRef(),
          name = method.name,
          arguments = emptyList()
        )))
        stringParts.add(string(", "))
      }
    }

    stringParts.removeLast() // remove trailing ", "
    stringParts.add(string(")"))
    val methodNode = methodNode(
      ownerClass = classNode.type,
      name = "toString",
      returnType = JavaType.String,
      annotations = listOf(annotationNode(Override::class.javaAnnotationType))
    ) {
      returnStmt(string(stringParts))
    }
    return listOf(methodNode)
  }

  private fun toString(expr: ExpressionNode): ExpressionNode {
    return when {
      expr.type.isArray -> if (expr.type.asArrayType.elementsType.primitive) fCall(name = "toString", ownerType = Arrays::class.javaType, arguments = listOf(expr))
      else fCall(name = "deepToString", ownerType = Arrays::class.javaType, arguments = listOf(expr))
      expr.type == JavaType.String -> expr
      else -> fCall(ownerType = JavaType.String, name = "valueOf", arguments = listOf(expr))
    }
  }
}