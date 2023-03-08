package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.AstInstructionNode
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod

interface FunctionCallNode: ExpressionNode, ScopedNode<Scope> {

  val name: String
  var methodOwnerType: ExpressionNode?

  val argumentNodes: List<AstInstructionNode>

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  fun getArguments(typeResolver: AstNodeTypeResolver): List<ExpressionNode>
  fun getMethod(typeResolver: AstNodeTypeResolver): JavaMethod

}

sealed class AbstractFunctionCallNode(token: LexToken) : FunctionCallNode, AbstractExpressionNode(token) {

  protected var method: JavaMethod? = null

  final override fun getMethod(typeResolver: AstNodeTypeResolver): JavaMethod {
    if (method == null) {
      method = doGetMethod(typeResolver)
    }
    return method!!
  }

  abstract fun doGetMethod(typeResolver: AstNodeTypeResolver): JavaMethod
}
open class SimpleFunctionCallNode constructor(
  token: LexToken, override var scope: Scope,
  override val name: String, val arguments: List<ExpressionNode>,
  // useful for autogenerated function (e.g. switches)
  method: JavaMethod? = null
): AbstractFunctionCallNode(token) {
  init {
      this.method = method
  }

  final override var methodOwnerType: ExpressionNode? = null
  override val argumentNodes = arguments

  constructor(token: LexToken, scope: Scope, name: String, arguments: MutableList<ExpressionNode>, methodOwnerType: ExpressionNode,
              method: JavaMethod? = null): this(token, scope, name, arguments, method) {
    this.methodOwnerType = methodOwnerType
  }

  override fun doGetMethod(typeResolver: AstNodeTypeResolver): JavaMethod {
   return if (methodOwnerType != null) typeResolver.findMethodOrThrow(typeResolver.resolve(methodOwnerType!!), name,
      arguments.map { it.accept(typeResolver) })
    else scope.findMethodOrThrow(name, arguments.map { it.accept(typeResolver) })
  }

  override fun getArguments(typeResolver: AstNodeTypeResolver): List<ExpressionNode> {
    return arguments
  }

  override fun toString(): String {
    return name + "(" + arguments.joinToString(separator = ",") + ")"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as SimpleFunctionCallNode

    if (name != other.name) return false
    if (arguments != other.arguments) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + arguments.hashCode()
    return result
  }
}

class NamedArgument(val name: String, val valueExpression: ExpressionNode)

open class NamedParametersFunctionCall constructor(token: LexToken, override var scope: Scope, override val name: String,
                                                   val namedArguments: List<NamedArgument>
): AbstractFunctionCallNode(token) {
  override val argumentNodes = namedArguments.map { it.valueExpression }

  override fun getArguments(typeResolver: AstNodeTypeResolver): List<ExpressionNode> {
    val method = getMethod(typeResolver)
    return method.parameters.map { parameter: MethodParameter ->
      namedArguments.find { it.name  == parameter.name }?.valueExpression
        ?: parameter.defaultValue
        ?: parameter.type.defaultValueExpression
    }
  }

  final override var methodOwnerType: ExpressionNode? = null

  private fun toMethodParameters(typeResolver: AstNodeTypeResolver): List<MethodParameter> {
    return namedArguments.map { MethodParameter(typeResolver.resolve(it.valueExpression), it.name) }
  }
  override fun doGetMethod(typeResolver: AstNodeTypeResolver): JavaMethod {
    val methodParameters = toMethodParameters(typeResolver)

    val m =  if (methodOwnerType != null) typeResolver.findMethodByParametersOrThrow(
      typeResolver.resolve(methodOwnerType!!), name, methodParameters)
    else scope.getMethodWithParameters(name, methodParameters)
    return m
  }

  override fun toString(): String {
    return name + "(" + namedArguments.joinToString(separator = ",", transform = { "${it.name}: ${it.valueExpression}"}) + ")"
  }
}