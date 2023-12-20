package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.NotLoadedJavaType
import marcel.lang.stringify

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
      TODO("Add super=super.toString()")
    }

    for (field in classNode.fields) {
      if (field.getAnnotation(stringify.Exclude::class.javaType) != null
        || field.isStatic || field.visibility != Visibility.PUBLIC) continue
      stringParts.add(string(field.name + "="))
      stringParts.add(
        if (field.type == JavaType.String) ref(field)
        else fCall(ownerType = JavaType.String, name = "valueOf", arguments = listOf(ref(field)))
      )
      stringParts.add(string(", "))
    }
    if (annotation.getAttribute("includeGetters")?.value == true) {
      for (method in classNode.methods) {
        if (method.getAnnotation(stringify.Exclude::class.javaType) != null || !method.isGetter
          || method.isStatic || method.visibility != Visibility.PUBLIC) continue
        stringParts.add(string(method.propertyName + "="))
        stringParts.add(
          fCall(ownerType = JavaType.String, name = "valueOf", arguments = listOf(fCall(
            owner = thisRef(classNode.type),
            name = method.name,
            arguments = emptyList()
          )))
        )
        stringParts.add(string(", "))
      }
    }

    stringParts.removeLast() // remove trailing ", "
    stringParts.add(string(")"))
    val methodNode = methodNode(ownerClass = classNode.type, name = "toString", returnType = JavaType.String) {
      returnStmt(string(stringParts))
    }
    return listOf(methodNode)
  }
}