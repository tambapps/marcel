package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.SourceJavaType
import com.tambapps.marcel.semantic.variable.LocalVariable

class LambdaClassNode(
  override val type: SourceJavaType,
  val constructorNode: MethodNode,
  isStatic: Boolean,
  cstNode: LambdaCstNode,
  fileName: String,
  val lambdaMethodParameters: List<MethodParameter>,
  val localVariablesSnapshot: List<LocalVariable>
) : ClassNode(type, Visibility.INTERNAL, null, isStatic = isStatic, isScript = false, fileName, cstNode.tokenStart, cstNode.tokenEnd) {

  data class MethodParameter(val type: JavaType?, val name: String)

  val explicit0Parameters = cstNode.explicit0Parameters
  val blockCstNode = cstNode.blockCstNode
  val interfaceTypes = mutableSetOf<JavaType>()

  // this list was passed to the NewInstanceNode of this lambda. Updating it will update the arguments of this node
  val constructorArguments = mutableListOf<ExpressionNode>()
  lateinit var constructorCallNode: NewInstanceNode

  init {
    methods.add(constructorNode)
  }
}