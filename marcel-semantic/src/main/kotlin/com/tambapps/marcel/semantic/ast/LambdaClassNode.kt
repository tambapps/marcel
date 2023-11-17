package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.NotLoadedJavaType

class LambdaClassNode(
  override val type: NotLoadedJavaType,
  val constructorNode: MethodNode,
  cstNode: LambdaCstNode,
  val lambdaMethodParameters: List<MethodParameter>
) : ClassNode(type, Visibility.PRIVATE, cstNode.tokenStart, cstNode.tokenEnd) {

  data class MethodParameter(val type: JavaType?, val name: String)

  val explicit0Parameters = cstNode.explicit0Parameters
  val blockCstNode = cstNode.blockCstNode
  val interfaceTypes = mutableSetOf<JavaType>()

  var expectedReturnType: JavaType? = null
  val constructorParameters = mutableListOf<ExpressionNode>()

  lateinit var constructorCallNode: NewInstanceNode
  lateinit var body: StatementNode

  init {
    methods.add(constructorNode)
  }
}