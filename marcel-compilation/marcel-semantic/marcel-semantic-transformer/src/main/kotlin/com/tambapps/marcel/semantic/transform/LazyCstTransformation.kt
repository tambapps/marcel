package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.AccessCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.FieldCstNode
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.exception.MarcelSyntaxTreeTransformationException
import com.tambapps.marcel.semantic.type.SourceJavaType

/**
 * CST Transformation making a field lazy to only compute its value when it is requesting. In order to implement the behaviour
 * the original field is renamed, and we create a getter for it. The getter will check if the renamed field is null, in which case it will
 * initialize it, and then return the value of the field.
 * The value to initialize the lazy variable is the initial value specified when initializing the field in the source code
 */
class LazyCstTransformation : AbstractCstTransformation() {

  override fun doTransform(javaType: SourceJavaType, node: CstNode, annotation: AnnotationNode) {
    node as FieldCstNode
    val originalField = symbolResolver.getClassField(javaType, node.name)
    val originalVisibility = node.access.visibility
    // making field private
    node.access.visibility = TokenType.VISIBILITY_PRIVATE
    val originalFieldName = originalField.name

    symbolResolver.undefineField(javaType, node.name)

    // rename original field
    node.name = "_" + originalField.name
    val fieldName = node.name

    if (originalField.type.primitive) {
      throw MarcelSyntaxTreeTransformationException(this, node.token, "Lazy only works for object types")
    }
    symbolResolver.defineField(javaType, toMarcelField(javaType, node))

    val classNode = node.parentClassNode
    val initialValue = node.initialValue ?: throw MarcelSyntaxTreeTransformationException(
      this,
      node.token,
      "Need initial value to make field lazy"
    )
    node.initialValue = null

    val getMethod = methodNode(
      classNode = classNode,
      accessNode = access(
        parent = classNode,
        isStatic = node.access.isStatic,
        isFinal = true,
        visibility = originalVisibility
      )
      ,
      name = "get" + originalFieldName[0].uppercase() + originalFieldName.substring(1),
      returnType = node.type,
      tokenStart = node.tokenStart,
      tokenEnd = node.tokenEnd
    ) {
      ifStmt(condition = isNull(ref(fieldName))) {
        varAssignStmt(fieldName, initialValue)
      }
      returnStmt(ref(fieldName))
    }
    addMethod(javaType, classNode, getMethod)
  }
}