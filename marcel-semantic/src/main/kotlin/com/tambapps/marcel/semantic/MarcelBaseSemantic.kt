package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.scope.MethodInnerScope
import com.tambapps.marcel.semantic.scope.MethodScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import java.util.*

abstract class MarcelBaseSemantic {

  protected abstract val typeResolver: JavaTypeResolver
  protected abstract val caster: AstNodeCaster

  val scopeQueue = LinkedList<Scope>()
  protected val currentScope: Scope get() = scopeQueue.peek() // FIFO
  protected val currentMethodScope get() = currentScope as? MethodScope ?: throw MarcelSemanticException(LexToken.DUMMY, "Not in a method")
  protected val currentInnerMethodScope get() = currentScope as? MethodInnerScope ?: throw MarcelSemanticException(LexToken.DUMMY, "Not in a inner scope")

  protected inline fun <T: Scope, U> useScope(scope: T, consumer: (T) -> U): U {
    scopeQueue.push(scope)
    val u = consumer.invoke(scope)
    scope.dispose()
    scopeQueue.pop()
    return u
  }

  protected fun newInnerScope() = MethodInnerScope(currentMethodScope)
  protected inline fun <U> useInnerScope(consumer: (MethodInnerScope) -> U)
      = useScope(newInnerScope(), consumer)

  protected fun castedArguments(method: JavaMethod, arguments: List<ExpressionNode>) =
    arguments.mapIndexed { index, expressionNode -> caster.cast(method.parameters[index].type, expressionNode) }

  protected fun fCall(node: CstNode, name: String, arguments: List<ExpressionNode>,
                    owner: ExpressionNode,
                    castType: JavaType? = null): ExpressionNode {
    return fCall(node, owner.type, name, arguments, owner, castType)
  }

  protected fun fCall(node: CstNode, ownerType: JavaType, name: String, arguments: List<ExpressionNode>,
                    owner: ExpressionNode? = null,
                    castType: JavaType? = null): ExpressionNode {
    val method = typeResolver.findMethodOrThrow(ownerType, name, arguments, node.token)
    return fCall(node, method, arguments, owner, castType)
  }

  protected fun fCall(
    node: CstNode,
    method: JavaMethod,
    arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null) = fCall(node.tokenStart,
    LexToken.DUMMY, // passing dummy to inform code highlight that this is not a fCall from the real marcel source code
    method, arguments, owner, castType)
  protected fun fCall(
    tokenStart: LexToken,
    tokenEnd: LexToken,
    method: JavaMethod,
    arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null): ExpressionNode {
    if (owner != null && method.isMarcelStatic) throw MarcelSemanticException(tokenStart, "Method $method is static but was call from an instance")
    val node = FunctionCallNode(method, owner, castedArguments(method, arguments), tokenStart, tokenEnd)
    return if (castType != null) caster.cast(castType, node) else node
  }
}