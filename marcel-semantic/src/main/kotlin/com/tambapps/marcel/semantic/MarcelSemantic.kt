package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.literral.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literral.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literral.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literral.LongCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodeNode
import com.tambapps.marcel.semantic.ast.expression.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.LongConstantNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.type.JavaType

class MarcelSemantic: ExpressionCstNodeVisitor<ExpressionNode>, StatementCstNodeVisitor<StatementNode> {

  private val exprVisitor = this as ExpressionCstNodeVisitor<ExpressionNode>
  private val stmtVisitor = this as StatementCstNodeVisitor<StatementNode>

  fun apply(cst: SourceFileCstNode): ClassNode {
    // TODO parse package if any
    val className = cst.fileName
    if (cst.instructions.isNotEmpty()) {
      val classNode = ClassNode(className, cst.tokenStart, cst.tokenEnd)
      val runMethod = MethodeNode("run", Visibility.PUBLIC, JavaType.Object, isStatic = false, isConstructor = false,
        tokenStart = cst.instructions.first().tokenStart, tokenEnd = cst.instructions.last().tokenEnd)
      classNode.addMethod(runMethod)
      cst.instructions.forEach { cstStmt -> runMethod.instructions.add(cstStmt.accept(stmtVisitor)) }
      return classNode
    } else {
      TODO()
    }
  }

  override fun visit(node: DoubleCstNode) = DoubleConstantNode(node.token, node.value)

  override fun visit(node: FloatCstNode) = FloatConstantNode(node.token, node.value)

  override fun visit(node: IntCstNode) = IntConstantNode(node.token, node.value)

  override fun visit(node: LongCstNode) = LongConstantNode(node.token, node.value)

  override fun visit(node: ClassReferenceCstNode): ExpressionNode {
    TODO("Not yet implemented")
  }

  override fun visit(node: DirectFieldReferenceCstNode): ExpressionNode {
    TODO("Not yet implemented")
  }

  override fun visit(node: IncrCstNode): ExpressionNode {
    TODO("Not yet implemented")
  }

  override fun visit(node: IndexAccessCstNode): ExpressionNode {
    TODO("Not yet implemented")
  }

  override fun visit(node: ReferenceCstNode): ExpressionNode {
    TODO("Not yet implemented")
  }

  override fun visit(node: FunctionCallCstNode): ExpressionNode {
    TODO("Not yet implemented")
  }

  override fun visit(node: ExpressionStatementCstNode) = ExpressionStatementNode(node.expressionNode.accept(exprVisitor), node.tokenStart, node.tokenEnd)

}