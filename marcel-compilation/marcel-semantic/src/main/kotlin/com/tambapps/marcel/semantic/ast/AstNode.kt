package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.visitor.ForEachNodeVisitor
import kotlin.reflect.KClass

interface AstNode {
  val tokenStart: LexToken
  val tokenEnd: LexToken

  val token get() = tokenStart

  /**
   * Apply the provided consumer on the given node and all descending nodes attached to it if any
   */
  fun <T: AstNode> forEach(clazz: KClass<T>, consumer: (T) -> Unit) {
    forEach {
      if (clazz.isInstance(it)) consumer(it as T)
    }
  }
  fun forEach(consumer: (AstNode) -> Unit) {
    when (this) {
      is ExpressionNode -> accept(ForEachNodeVisitor(consumer))
      is StatementNode -> accept(ForEachNodeVisitor(consumer))
      else -> consumer(this)
    }
  }
}