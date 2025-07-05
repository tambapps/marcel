package com.tambapps.marcel.semantic.processor.visitor

import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.BreakCstNode
import com.tambapps.marcel.parser.cst.statement.ContinueCstNode
import com.tambapps.marcel.parser.cst.statement.DoWhileStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ForInCstNode
import com.tambapps.marcel.parser.cst.statement.ForInMultiVarCstNode
import com.tambapps.marcel.parser.cst.statement.ForVarCstNode
import com.tambapps.marcel.parser.cst.statement.IfStatementCstNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.ThrowCstNode
import com.tambapps.marcel.parser.cst.statement.TryCatchCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.WhileCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.processor.SemanticCstNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType

/**
 * Visitor of a switch/when CST branch allowing to separate the statements (if any) from the yield value.
 * We're processing CST instead of AST to prevent converting statementExpressions as void expressions, and to properly pass
 * smartCastType
 */
class WhenStatementExpressionSeparator(
  val smartCastType: JavaType? = null,
  val semantic: SemanticCstNodeVisitor,
  val exprError: (AstNode, String) -> ExpressionNode,
): StatementCstNodeVisitor<Pair<StatementNode?, ExpressionNode>> {

  override fun visit(node: ExpressionStatementCstNode): Pair<StatementNode?, ExpressionNode> {
    val expression = node.expressionNode.accept(semantic, smartCastType)
    return null to expression
  }

  override fun visit(node: BlockCstNode): Pair<StatementNode?, ExpressionNode> {
    if (node.statements.isEmpty()) return error(node)
    val lastStatement = node.statements.last()
    if (lastStatement !is ExpressionStatementCstNode) return error(lastStatement)
    val expression = lastStatement.expressionNode.accept(semantic, smartCastType)
    return if (node.statements.size == 1) null to expression
    else BlockCstNode(
      statements = node.statements.toMutableList().apply {
        removeLast()
      },
      parent = node.parent,
      tokenStart = node.tokenStart,
      tokenEnd = lastStatement.tokenEnd
    ).accept(semantic) to expression
  }

  override fun visit(node: ReturnCstNode) = error(node)

  override fun visit(node: VariableDeclarationCstNode) = error(node)

  override fun visit(node: MultiVarDeclarationCstNode) = error(node)

  override fun visit(node: IfStatementCstNode) = error(node)

  override fun visit(node: ForInCstNode) = error(node)

  override fun visit(node: ForInMultiVarCstNode) = error(node)

  override fun visit(node: ForVarCstNode) = error(node)

  override fun visit(node: WhileCstNode) = error(node)

  override fun visit(node: DoWhileStatementCstNode) = error(node)

  override fun visit(node: BreakCstNode) = error(node)

  override fun visit(node: ContinueCstNode) = error(node)

  override fun visit(node: ThrowCstNode) = error(node)

  override fun visit(node: TryCatchCstNode) = error(node)

  private fun error(node: StatementCstNode, msg: String = "When/switch branch doesn't yield a value"): Pair<StatementNode?, ExpressionNode> {
    return null to exprError(node.accept(semantic), msg)
  }
}