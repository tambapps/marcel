package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.MultiVariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.TryCatchNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement

interface AstNodeVisitor<T> {

  fun visit(node: IntConstantNode): T
  fun visit(node: LongConstantNode): T
  fun visit(node: FloatConstantNode): T
  fun visit(node: DoubleConstantNode): T
  fun visit(node: CharConstantNode): T

  fun visit(node: MulOperator): T
  fun visit(node: TernaryNode): T
  fun visit(node: ElvisOperator): T

  fun visit(node: FunctionCallNode): T
  fun visit(node: ConstructorCallNode): T
  fun visit(node: NamedParametersConstructorCallNode): T
  fun visit(node: SuperConstructorCallNode): T
  fun visit(node: DivOperator): T
  fun visit(node: PlusOperator): T
  fun visit(node: MinusOperator): T
  fun visit(node: PowOperator): T
  fun visit(node: RightShiftOperator): T
  fun visit(node: LeftShiftOperator): T
  fun visit(node: VariableAssignmentNode): T
  fun visit(node: FieldAssignmentNode): T

  fun visit(node: IndexedVariableAssignmentNode): T

  fun visit(node: ReferenceExpression): T
  fun visit(node: IndexedReferenceExpression): T

  fun visit(node: UnaryMinus): T
  fun visit(node: UnaryPlus): T
  fun visit(node: BlockNode): T
  fun visit(node: FunctionBlockNode): T
  fun visit(node: LambdaNode): T

  fun visit(node: ExpressionStatementNode): T
  fun visit(node: VariableDeclarationNode): T
  fun visit(node: TruthyVariableDeclarationNode): T
  fun visit(node: MultiVariableDeclarationNode): T

  fun visit(node: ReturnNode): T
  fun visit(node: VoidExpression): T
  fun visit(node: StringNode): T
  fun visit(node: StringConstantNode): T
  fun visit(node: AsNode): T

  fun visit(node: ToStringNode): T
  fun visit(node: InvokeAccessOperator): T
  fun visit(node: GetFieldAccessOperator): T
  fun visit(node: GetIndexFieldAccessOperator): T

  fun visit(node: BooleanConstantNode): T
  fun visit(node: ComparisonOperatorNode): T
  fun visit(node: AndOperator): T
  fun visit(node: OrOperator): T

  fun visit(node: NotNode): T
  fun visit(node: IfStatementNode): T
  fun visit(node: ForStatement): T
  fun visit(node: TryCatchNode): T
  fun visit(node: ForInStatement): T

  fun visit(node: WhileStatement): T
  fun visit(node: BooleanExpressionNode): T
  fun visit(node: NullValueNode): T
  fun visit(node: IncrNode): T
  fun visit(node: BreakLoopNode): T
  fun visit(node: ContinueLoopNode): T
  fun visit(node: RangeNode): T
  fun visit(node: LiteralArrayNode): T
  fun visit(node: LiteralMapNode): T
  fun visit(node: SwitchBranchNode): T
  fun visit(node: SwitchNode): T
  fun visit(node: WhenBranchNode): T
  fun visit(node: WhenNode): T
  fun visit(node: IsOperator): T
  fun visit(node: IsNotOperator): T
  fun visit(node: ByteConstantNode): T
  fun visit(node: ShortConstantNode): T
  fun visit(node: ThisReference): T
  fun visit(node: SuperReference): T
  fun visit(node: LiteralPatternNode): T
  fun visit(node: FindOperator): T
  fun visit(node: ClassExpressionNode): T
  fun visit(node: DirectFieldAccessNode): T

}