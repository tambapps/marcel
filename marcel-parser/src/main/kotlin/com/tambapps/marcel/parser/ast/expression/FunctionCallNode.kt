package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.TypedNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType

open class FunctionCallNode(override val scope: Scope, val name: String, val arguments: MutableList<ExpressionNode>): ExpressionNode, ScopedNode<Scope> {

  constructor(scope: Scope, name: String, arguments: MutableList<ExpressionNode>, methodOwnerType: TypedNode): this(scope, name, arguments) {
    this.methodOwnerType = methodOwnerType
  }

  var methodOwnerType: TypedNode? = null

  override val type: JavaType
    // TODO BIG HACK for println. May be able to solve it by defining a println method on the defined class
    get() = if (name == "println")  JavaType.void
     else method.returnType

  val method: JavaMethod
    get() = if (methodOwnerType != null) methodOwnerType!!.type.findMethodOrThrow(name, arguments)
   else scope.getMethod(name, arguments)

  val descriptor: String
    get() = AsmUtils.getDescriptor(arguments, type)

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