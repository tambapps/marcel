package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.FieldCstNode
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

// TODO WIP
class LazyAstTransformation: GenerateMethodAstTransformation() {

  override fun transform(javaType: NotLoadedJavaType, node: CstNode, annotation: AnnotationNode) {
    node as FieldCstNode
    val originalField = typeResolver.getClassField(javaType, node.name)

    typeResolver.undefineField(javaType, node.name)

    val visibility = Visibility.fromTokenType(node.access.visibility)
    val suffix = node.name.first().uppercase() + node.name.substring(1)
    // define getter
    typeResolver.defineMethod(javaType, signature(
      visibility = visibility,
      name = "get$suffix",
      returnType = originalField.type
    ))

    // define setter
    typeResolver.defineMethod(javaType, signature(
      visibility = visibility,
      name = "set$suffix",
      parameters = listOf(
        parameter(type = originalField.type, name = originalField.name)
      ),
      returnType = JavaType.void
    ))
  }

  override fun generateMethodNodes(node: AstNode, classNode: ClassNode, annotation: AnnotationNode): List<MethodNode> {
    node as FieldCstNode
    val visibility = Visibility.fromTokenType(node.access.visibility)
    val suffix = node.name.first().uppercase() + node.name.substring(1)

    val getter = methodNode(
      visibility = visibility,
      name = "get$suffix",
      returnType = visit(node.type)
    ) {

    }

    return listOf(
      getter,
      // TODO setter
    )
  }
}