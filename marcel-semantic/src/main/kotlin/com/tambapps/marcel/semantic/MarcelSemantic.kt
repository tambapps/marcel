package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.scope.MethodScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import marcel.lang.Script
import java.util.LinkedList

// TODO implement multiple errors like in parser2
class MarcelSemantic(
  private val typeResolver: JavaTypeResolver,
  private val cst: SourceFileCstNode
): ExpressionCstNodeVisitor<ExpressionNode>, StatementCstNodeVisitor<StatementNode> {

  val exprVisitor = this as ExpressionCstNodeVisitor<ExpressionNode>
  val stmtVisitor = this as StatementCstNodeVisitor<StatementNode>

  private val scopeQueue = LinkedList<Scope>()
  // FIFO
  private val currentScope get() = scopeQueue.peek()
  fun apply(): ModuleNode {
    val imports = Scope.DEFAULT_IMPORTS.toMutableList()
    // TODO parse package if any

    // TODO check return type of all functions, as the compiler should not check anything

    val className = cst.fileName
    if (cst.statements.isNotEmpty()) {
      val classType = typeResolver.defineClass(cst.statements.first().tokenStart, Visibility.PUBLIC, className, Script::class.javaType, false, emptyList())
      val classScope = ClassScope(classType, typeResolver, imports)
      scopeQueue.push(classScope)
      val classNode = ClassNode(classType, Visibility.PUBLIC, cst.tokenStart, cst.tokenEnd)
      val runMethod =
        MethodNode(name = "run",
          visibility = Visibility.PUBLIC, returnType = JavaType.Object,
          isStatic = false, isConstructor = false,
          ownerClass = classType,
          annotations = emptyList(),
          parameters = emptyList(),
          tokenStart = cst.statements.first().tokenStart, tokenEnd = cst.statements.last().tokenEnd)
      useScope(MethodScope(classScope, runMethod.isStatic)) {
        runMethod.blockStatement = BlockStatementNode(
          cst.statements.map { cstStmt -> cstStmt.accept(stmtVisitor) },
          runMethod.tokenStart, runMethod.tokenEnd)
      }
      classNode.addMethod(runMethod)
      return ModuleNode(cst.tokenStart, cst.tokenEnd).apply {
        classes.add(classNode)
      }
    } else {
      TODO()
    }
  }

  private inline fun useScope(scope: Scope, consumer: () -> Unit) {
    scopeQueue.push(scope)
    consumer.invoke()
    scopeQueue.pop()
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

  override fun visit(node: ReferenceCstNode) = ReferenceNode(currentScope.findVariableOrThrow(node.value, node.token), node.token)

  override fun visit(node: FunctionCallCstNode): ExpressionNode {
    if (node.namedArgumentNodes.isNotEmpty()) TODO("Doesn't handle named arguments yet")
    val arguments = node.positionalArgumentNodes.map { it.accept(exprVisitor) }
    val method = currentScope.findMethodOrThrow(node.value, arguments, node)
    val castType = if (node.castType != null) type(node.castType!!) else null
    val owner = if (method.isStatic) null else null // TODO ThisNode()
    return FunctionCallNode(method, owner, castType, arguments, node.token)
  }

  private fun type(node: TypeCstNode): JavaType = typeResolver.of(node.value, emptyList())

  override fun visit(node: ExpressionStatementCstNode) = ExpressionStatementNode(node.expressionNode.accept(exprVisitor), node.tokenStart, node.tokenEnd)
  override fun visit(node: ReturnCstNode) = ReturnStatementNode(node.expressionNode.accept(exprVisitor), node.tokenStart, node.tokenEnd)

}