package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ResolvableNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType

open class FunctionCallNode(val name: String, val arguments: MutableList<ExpressionNode>): ExpressionNode, ResolvableNode, JavaMethod {

  override lateinit var type: JavaType
  override val descriptor: String
    get() = AsmUtils.getDescriptor(arguments, type)
  override val parameterTypes: Array<Class<*>>
    get() = arguments.map { it.type.realClassOrObject }.toTypedArray()

  constructor(name: String): this(name, mutableListOf())

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun resolve(scope: Scope) {
    if (name != "println") { // TODO BIG HACK
      this.type = scope.getMethod(name, arguments).returnType
    } else {
      this.type = JavaType.void
    }
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