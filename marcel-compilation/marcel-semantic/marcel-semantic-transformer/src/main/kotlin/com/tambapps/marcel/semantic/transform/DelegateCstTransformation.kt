package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.FieldCstNode
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.SourceJavaType
import marcel.lang.Delegable

/**
 * CST Transformation making a field lazy to only compute its value when it is requesting. In order to implement the behaviour
 * the original field is renamed, and we create a getter for it. The getter will check if the renamed field is null, in which case it will
 * initialize it, and then return the value of the field.
 * The value to initialize the lazy variable is the initial value specified when initializing the field in the source code
 */
class DelegateCstTransformation : AbstractCstTransformation() {

  override fun doTransform(javaType: SourceJavaType, node: CstNode, annotation: AnnotationNode) {
    node as FieldCstNode
    val classNode = node.parentClassNode
    javaType.addImplementedInterface(Delegable::class.javaType)

    val getDelegateMethod = methodNode(
      classNode = classNode,
      accessNode = access(node),
      name = "getDelegate",
      returnType = node.type,
      tokenStart = node.tokenStart,
      tokenEnd = node.tokenEnd
    ) {
      returnStmt(directFieldRef(node.name))
    }
    addMethod(javaType, classNode, getDelegateMethod)
  }
}