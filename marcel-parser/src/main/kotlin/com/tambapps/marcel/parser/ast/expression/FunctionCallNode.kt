package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod

interface FunctionCallNode: ExpressionNode, ScopedNode<Scope> {

  val name: String
  val arguments: MutableList<ExpressionNode>
  // useful for autogenerated function (e.g. switches)
  val method: JavaMethod?
  var methodOwnerType: ExpressionNode?

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  fun getMethod(typeResolver: AstNodeTypeResolver): JavaMethod

}

open class SimpleFunctionCallNode constructor(override var scope: Scope, override val name: String, override val arguments: MutableList<ExpressionNode>,
                                              // useful for autogenerated function (e.g. switches)
                                              final override val method: JavaMethod? = null): FunctionCallNode {

  final override var methodOwnerType: ExpressionNode? = null

  constructor(scope: Scope, name: String, arguments: MutableList<ExpressionNode>, methodOwnerType: ExpressionNode,
              method: JavaMethod? = null): this(scope, name, arguments, method) {
    this.methodOwnerType = methodOwnerType
  }

  override fun getMethod(typeResolver: AstNodeTypeResolver): JavaMethod {
    return if (this.method != null) this.method
    else if (methodOwnerType != null) typeResolver.findMethodOrThrow(typeResolver.resolve(methodOwnerType!!), name,
      arguments.map { it.accept(typeResolver) })
    else scope.getMethod(name, arguments.map { it.accept(typeResolver) })
  }


  override fun toString(): String {
    return name + "(" + arguments.joinToString(separator = ",") + ")"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as FunctionCallNode

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

open class NamedParametersFunctionCall constructor(override var scope: Scope, override val name: String,
                                                   val namedArguments: List<NamedArgument>
): FunctionCallNode {
  override val arguments: MutableList<ExpressionNode>
    get() {
      val method = this.method ?: throw IllegalStateException("Method has not been retrieved")
      return  method.parameters.map { parameter: MethodParameter ->
        namedArguments.find { it.name  == parameter.name }?.valueExpression ?: parameter.type.defaultValueExpression
      }.toMutableList()
    }

  final override var methodOwnerType: ExpressionNode? = null

  final override var method: JavaMethod? = null

  private fun toMethodParameters(typeResolver: AstNodeTypeResolver): List<MethodParameter> {
    return namedArguments.map { MethodParameter(typeResolver.resolve(it.valueExpression), it.name) }
  }
  override fun getMethod(typeResolver: AstNodeTypeResolver): JavaMethod {
    if (method != null) return method!!
    val methodParameters = toMethodParameters(typeResolver)

    val m =  if (methodOwnerType != null) typeResolver.findMethodByParametersOrThrow(
      typeResolver.resolve(methodOwnerType!!), name, methodParameters)
    else scope.getMethodWithParameters(name, methodParameters)
    this.method = m // cache it for eventual future calls
    return m
  }
}