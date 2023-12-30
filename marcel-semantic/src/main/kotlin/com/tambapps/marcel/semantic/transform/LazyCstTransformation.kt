package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.FieldCstNode
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.exception.MarcelSyntaxTreeTransformationException
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

/**
 * CST Transformation making a field lazy to only compute its value when it is requesting. In order to implement the behaviour
 * the original field is renamed, and we create a getter for it. The getter will check if the renamed field is null, in which case it will
 * initialize it, and then return the value of the field.
 * The value to initialize the lazy variable is the initial value specified when initializing the field in the source code
 */
class LazyCstTransformation: AbstractCstTransformation() {

  override fun doTransform(javaType: NotLoadedJavaType, node: CstNode, annotation: AnnotationNode) {
    node as FieldCstNode
    val originalField = typeResolver.getClassField(javaType, node.name)
    val originalFieldName = originalField.name

    typeResolver.undefineField(javaType, node.name)

    node.name = "_" + originalField.name
    val fieldName = node.name

    if (originalField.type.primitive) {
      throw MarcelSyntaxTreeTransformationException(this, node.token, "Lazy only works for object types")
    }
    typeResolver.defineField(javaType, toMarcelField(javaType, node))

    if (node.initialValue == null) {
      throw MarcelSyntaxTreeTransformationException(this, node.token, "Need initial value to make field lazy")
    }

    val classNode = node.parentClassNode
    val initialValue = node.initialValue!!
    node.initialValue = null

    val getMethod = methodNode(
      classNode = classNode,
      accessNode = node.access, name = "get" + originalFieldName[0].uppercase() + originalFieldName.substring(1),
      returnType = node.type,
      tokenStart = node.tokenStart,
      tokenEnd = node.tokenEnd
      ) {
      ifStmt(
        condition = isNull(ref(fieldName))
      ) {
        varAssignStmt(fieldName, initialValue)
      }
      returnStmt(ref(fieldName))
    }

    if (classNode.methods.any { it.name == getMethod.name && it.parameters == getMethod.parameters }) {
      throw MarcelSyntaxTreeTransformationException(this, node.token, "Method ${getMethod.name} already exists")
    }

    typeResolver.defineMethod(javaType, toJavaMethod(ownerType = javaType, node = getMethod))
    classNode.methods.add(getMethod)
  }

}