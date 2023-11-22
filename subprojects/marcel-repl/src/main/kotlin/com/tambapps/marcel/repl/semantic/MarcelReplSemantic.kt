package com.tambapps.marcel.repl.semantic

import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaTypeResolver

class MarcelReplSemantic(typeResolver: JavaTypeResolver, cst: SourceFileCstNode) : MarcelSemantic(typeResolver, cst) {

  override fun assignment(node: BinaryOperatorCstNode): ExpressionNode {
    val leftResult = runCatching { node.leftOperand.accept(this) }
    if (leftResult.isSuccess) return assignment(node, leftResult.getOrThrow())

    // if we went here this means the field was not defined
    // TODO allow defining variables that don't exist, with bound field.
    //  we may not need bound field at all since it will be a methodField at the end
    TODO()
  }
}