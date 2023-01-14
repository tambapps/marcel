package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.TernaryNode
import com.tambapps.marcel.parser.ast.expression.operator.binary.BinaryOperatorNode
import com.tambapps.marcel.parser.ast.expression.operator.binary.DivOperator
import com.tambapps.marcel.parser.ast.expression.operator.binary.MinusOperator
import com.tambapps.marcel.parser.ast.expression.operator.binary.MulOperator
import com.tambapps.marcel.parser.ast.expression.operator.binary.PlusOperator
import com.tambapps.marcel.parser.ast.expression.variable.VariableReferenceExpression
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.statement.variable.VariableAssignmentNode
import com.tambapps.marcel.parser.ast.statement.variable.VariableDeclarationNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.type.JavaType

import org.objectweb.asm.Opcodes
import java.util.concurrent.ThreadLocalRandom

class MarcelParser(private val className: String, private val tokens: List<LexToken>) {

  constructor(tokens: List<LexToken>): this("MarcelRandomClass_" + ThreadLocalRandom.current().nextInt(), tokens)

  private var currentIndex = 0

  private val current: LexToken
    get() {
      checkEof()
      return tokens[currentIndex]
    }

  private val eof: Boolean
    get() = currentIndex >= tokens.size
  private val currentSafe: LexToken?
    get() = if (eof) null else tokens[currentIndex]


  fun parse(): ModuleNode {
    return script()
  }

  fun script(): ModuleNode {
    val statements = mutableListOf<StatementNode>()
    val scope = Scope()
    while (current.type != TokenType.END_OF_FILE) {
      statements.add(statement(scope))
    }
    return  ModuleNode(mutableListOf(ClassNode(
      Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER, className, Types.OBJECT, mutableListOf(
        MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main",
          statements, arrayOf(Types.STRING_ARRAY), Types.VOID, scope
        )))))
  }
  private fun statement(scope: Scope): StatementNode {
    val token = next()
    return when (token.type) {
      TokenType.TYPE_INT -> variableDeclaration(scope, JavaPrimitiveType.INT)
      TokenType.TYPE_LONG -> variableDeclaration(scope, JavaPrimitiveType.LONG)
      TokenType.TYPE_FLOAT -> variableDeclaration(scope, JavaPrimitiveType.FLOAT)
      TokenType.TYPE_DOUBLE -> variableDeclaration(scope, JavaPrimitiveType.DOUBLE)
      else -> {
        rollback()
        val node = expression(scope)
        acceptOptional(TokenType.SEMI_COLON)
        ExpressionStatementNode(node)
      }
    }
  }

  // assuming type has already been accepted
  private fun variableDeclaration(scope: Scope, type: JavaType): VariableDeclarationNode {
    val identifier = accept(TokenType.IDENTIFIER)
    accept(TokenType.ASSIGNEMENT)
    val variableDeclarationNode = VariableDeclarationNode(type, identifier.value, expression(scope))
    scope.addLocalVariable(variableDeclarationNode.type, variableDeclarationNode.name)
    return variableDeclarationNode
  }

  fun expression(scope: Scope): ExpressionNode {
    val expr = expression(scope, Int.MAX_VALUE)
    if (current.type == TokenType.QUESTION_MARK) {
      skip()
      val trueExpr = expression(scope)
      accept(TokenType.COLON)
      val falseExpr = expression(scope)
      return TernaryNode(expr, trueExpr, falseExpr)
    }
    return expr
  }

  // TODO problem with priorities
  private fun expression(scope: Scope, maxPriority: Int): ExpressionNode {
    var a = atom(scope)
    var t = current
    while (ParserUtils.isBinaryOperator(t.type) && ParserUtils.getPriority(t.type) < maxPriority) {
      next()
      val leftOperand = a
      val rightOperand = expression(scope, ParserUtils.getPriority(t.type) + ParserUtils.getAssociativity(t.type))
      a = operator(t.type, leftOperand, rightOperand)
      t = current
    }
    return a
  }

  private fun atom(scope: Scope): ExpressionNode {
    val token = next()
    return when (token.type) {
      TokenType.INTEGER -> IntConstantNode(token.value.toInt())
      TokenType.IDENTIFIER -> {
        if (current.type == TokenType.LPAR) {
          skip()
          val fCall = FunctionCallNode(token.value)
          while (current.type != TokenType.RPAR) {
            fCall.arguments.add(expression(scope))
            if (current.type == TokenType.RPAR) {
              break
            } else {
              accept(TokenType.COMMA)
            }
          }
          skip() // skipping PARENT_CLOSE
          return fCall
        } else if (current.type == TokenType.ASSIGNEMENT) {
          skip()
          VariableAssignmentNode(token.value, expression(scope))
        } else {
          VariableReferenceExpression(token.value, scope)
        }
      }
      else -> {
        throw UnsupportedOperationException("Not supported yet")
      }
    }
  }

  private fun operator(t: TokenType, leftOperand: ExpressionNode, rightOperand: ExpressionNode): BinaryOperatorNode {
    return when(t) {
      TokenType.MUL -> MulOperator(leftOperand, rightOperand)
      TokenType.DIV -> DivOperator(leftOperand, rightOperand)
      TokenType.PLUS -> PlusOperator(leftOperand, rightOperand)
      TokenType.MINUS -> MinusOperator(leftOperand, rightOperand)
      else -> TODO()
    }
  }

  private fun accept(t: TokenType): LexToken {
    val token = current
    if (token.type != t) {
      throw MarcelParsingException("Expected token of type $t but got ${token.type}")
    }
    currentIndex++
    return token
  }

  private fun acceptOptional(t: TokenType): LexToken? {
    val token = currentSafe
    if (token?.type == t) {
      currentIndex++
    }
    return token
  }

  private fun rollback() {
    currentIndex--
  }

  private fun skip() {
    currentIndex++
  }
  private fun next(): LexToken {
    checkEof()
    return tokens[currentIndex++]
  }

  fun reset() {
    currentIndex = 0
  }

  private fun checkEof() {
    if (eof) {
      throw MarcelParsingException("Unexpected end of file")
    }
  }
}