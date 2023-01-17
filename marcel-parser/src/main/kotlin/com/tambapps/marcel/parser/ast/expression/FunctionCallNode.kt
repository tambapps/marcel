package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType

open class FunctionCallNode(val scope: Scope, val name: String, val arguments: MutableList<ExpressionNode>): ExpressionNode, JavaMethod {

  var owner: ExpressionNode? = null

  override val type: JavaType
    get() = if (name != "println") { // TODO BIG HACK
      scope.getMethod(name, arguments).returnType
    } else {
      JavaType.void
    }


  override val descriptor: String
    get() = AsmUtils.getDescriptor(arguments, type)
  override val parameterTypes: Array<Class<*>>
    get() = arguments.map { it.type.realClassOrObject }.toTypedArray()

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
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
    if (type != other.type) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + arguments.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }
}