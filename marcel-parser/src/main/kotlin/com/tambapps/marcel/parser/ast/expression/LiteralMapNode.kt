package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType

class LiteralMapNode(val entries: List<Pair<ExpressionNode, ExpressionNode>>): ExpressionNode {

  override val type: JavaType get() =  JavaType.mapType(keysType, valuesType)

  val keysType: JavaType
    get() = JavaType.commonType(entries.map { it.first })
  val valuesType: JavaType
    get() = JavaType.commonType(entries.map { it.second })

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    entries.forEach {
      it.first.accept(visitor)
      it.second.accept(visitor)
    }
  }
}