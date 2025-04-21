package com.tambapps.marcel.semantic.processor.extensions

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.processor.visitor.ForEachNodeVisitor
import kotlin.reflect.KClass


fun AstNode.forEach(consumer: (AstNode) -> Unit) {
  when (this) {
    is ExpressionNode -> accept(ForEachNodeVisitor(consumer))
    is StatementNode -> accept(ForEachNodeVisitor(consumer))
    else -> consumer(this)
  }
}

/**
 * Apply the provided consumer on the given node and all descending nodes attached to it if any
 */
fun <T: AstNode> AstNode.forEach(clazz: KClass<T>, consumer: (T) -> Unit) {
  forEach {
    if (clazz.isInstance(it)) consumer(it as T)
  }
}