package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.Annotable
import com.tambapps.marcel.parser.cst.ClassCstNode as ClassCstNode
import com.tambapps.marcel.parser.cst.FieldCstNode as FieldCstNode
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.exception.MarcelSyntaxTreeTransformationException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.SourceJavaType
import marcel.lang.data

class AllArgsConstructorAstTransformation : GenerateMethodAstTransformation() {

  override fun generateSignatures(
    node: CstNode,
    javaType: SourceJavaType,
    annotation: AnnotationNode
  ): List<JavaMethod> {
    val classNode = node as? ClassCstNode ?: throw MarcelSyntaxTreeTransformationException(
      this,
      node.token,
      "Cannot perform AllArgsConstructor as the annotated member is not a class"
    )

    val fields = classNode.fields.filter { fieldCstNode: FieldCstNode ->
      !fieldCstNode.access.isStatic && fieldCstNode.annotations.none { resolve(it.typeNode) == data.Exclude::class.javaType }
    }

    if (fields.isEmpty()) {
      throw MarcelSyntaxTreeTransformationException(this, node.token, "No fields matched to create a constructor")
    }

    for (f in fields) {
      if (f.initialValue != null) {
        throw MarcelSyntaxTreeTransformationException(
          this,
          f.token,
          "Cannot generate all args constructor as field $f has an initial value"
        )
      }
    }
    return listOf(
      signature(
        visibility = Visibility.PUBLIC,
        name = JavaMethod.CONSTRUCTOR_NAME,
        parameters = fields.map { MethodParameter(resolve(it.type), it.name) },
        returnType = JavaType.void // constructor return void
      )
    )
  }

  override fun generateMethodNodes(node: AstNode, classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    val fields = classNode.fields.filter { !isAnnotableExcluded(it) && !it.isStatic }

    val constructorNode = constructorNode(classNode, parameters = fields.map { MethodParameter(it.type, it.name) }) {
      for (i in fields.indices) {
        val fieldNode = fields[i]
        val parameterRef = argRef(i)

        varAssignStmt(owner = thisRef(), variable = fieldNode, expr = parameterRef)
      }
    }
    return listOf(constructorNode)
  }

  private fun isAnnotableExcluded(annotable: Annotable): Boolean {
    return annotable.getAnnotation(data.Exclude::class.javaType) != null
  }
}