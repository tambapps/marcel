package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.type.SourceJavaType
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable

class LambdaClassNode constructor(
  override val type: SourceJavaType,
  val constructorNode: MethodNode,
  isStatic: Boolean,
  cstNode: LambdaCstNode,
  fileName: String,
  val lambdaMethodParameters: List<MethodParameter>,
  val localVariablesSnapshot: List<LocalVariable>
) : ClassNode(
  type,
  Visibility.INTERNAL,
  null,
  isStatic = isStatic,
  isScript = false,
  isEnum = false,
  isFinal = true,
  fileName = fileName,
  tokenStart = cstNode.tokenStart,
  tokenEnd = cstNode.tokenEnd
) {

  val isTopLevel get() = type.isTopLevel
  data class MethodParameter(val type: JavaType?, val nullness: Nullness, val name: String)

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