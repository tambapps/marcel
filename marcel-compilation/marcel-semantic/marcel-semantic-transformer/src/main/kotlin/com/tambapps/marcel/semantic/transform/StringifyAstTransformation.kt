package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.Annotable
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.compose.StatementsComposer
import com.tambapps.marcel.semantic.extensions.javaAnnotationType
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.MarcelMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.SourceJavaType
import marcel.lang.data
import marcel.lang.stringify
import java.util.Arrays

/**
 * AST transformation to generate toString method. Implicitly referenced in stringify annotation
 */
class StringifyAstTransformation : GenerateMethodAstTransformation() {

  override fun generateSignatures(node: CstNode, javaType: SourceJavaType, annotation: AnnotationNode): List<MarcelMethod> {
    return if (node is ClassCstNode && node.methods.none { method -> method.name == "toString" && method.parameters.isEmpty() })
      listOf(signature(name = "toString", returnType = JavaType.String))
    else emptyList()
  }
  override fun generateMethodNodes(node: AstNode, classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    if (classNode.methods.any { method -> method.name == "toString" && method.parameters.isEmpty() }) {
      return emptyList()
    }
    val methodNode = methodNode(
      ownerClass = classNode.type,
      name = "toString",
      returnType = JavaType.String,
      annotations = listOf(annotationNode(Override::class.javaAnnotationType))
    ) {
      val stringParts = mutableListOf<ExpressionNode>(
        string(classNode.type.simpleName + "(")
      )
      if (classNode.superType != JavaType.Object) {
        stringParts.add(string("super="))
        stringParts.add(toString(fCall(name = "toString", owner = superRef(), arguments = emptyList())))
      }

      for (field in classNode.fields) {
        if (isAnnotableExcluded(field) || field.isStatic) continue
        stringParts.add(string(field.name + "="))
        stringParts.add(toString(ref(field)))
        stringParts.add(string(", "))
      }
      if (annotation.getAttribute("includeGetters")?.value == true) {
        for (method in classNode.methods) {
          if (isAnnotableExcluded(method) || !method.isGetter
            || method.isStatic
          ) continue
          stringParts.add(string(method.propertyName + "="))
          stringParts.add(
            toString(
              fCall(
                owner = thisRef(),
                name = method.name,
                arguments = emptyList()
              )
            )
          )
          stringParts.add(string(", "))
        }
      }

      stringParts.removeAt(stringParts.size - 1) // remove trailing ", "
      stringParts.add(string(")"))
      returnStmt(string(stringParts))
    }
    return listOf(methodNode)
  }

  private fun StatementsComposer.toString(expr: ExpressionNode): ExpressionNode {
    return when {
      expr.type.isArray -> if (expr.type.asArrayType.elementsType.primitive) fCall(
        name = "toString",
        ownerType = Arrays::class.javaType,
        arguments = listOf(expr)
      )
      else fCall(name = "deepToString", ownerType = Arrays::class.javaType, arguments = listOf(expr))

      expr.type == JavaType.String -> expr
      else -> fCall(ownerType = JavaType.String, name = "valueOf", arguments = listOf(expr))
    }
  }

  private fun isAnnotableExcluded(annotable: Annotable): Boolean {
    return annotable.getAnnotation(stringify.Exclude::class.javaType) != null
        // useful because this can be run from a data annotation
        || annotable.getAnnotation(data.Exclude::class.javaType) != null
  }
}