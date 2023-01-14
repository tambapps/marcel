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
import com.tambapps.marcel.parser.ast.expression.operator.unary.UnaryMinus
import com.tambapps.marcel.parser.ast.expression.operator.unary.UnaryPlus
import com.tambapps.marcel.parser.ast.expression.variable.VariableReferenceExpression
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.statement.variable.VariableAssignmentNode
import com.tambapps.marcel.parser.ast.statement.variable.VariableDeclarationNode
import com.tambapps.marcel.parser.exception.SemanticException
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
    val scope = Scope()
    val mainFunction = MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main",
      mutableListOf(), mutableListOf(MethodParameter(Types.STRING_ARRAY, "args")), Types.VOID, scope
    )
    val classNode = ClassNode(
      Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER, className, Types.OBJECT, mutableListOf(mainFunction))
    val moduleNode = ModuleNode(mutableListOf(classNode))

    while (current.type != TokenType.END_OF_FILE) {
      when (current.type) {
        TokenType.FUN, TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED,
        TokenType.VISIBILITY_HIDDEN, TokenType.VISIBILITY_PRIVATE -> {
          val method = method()
          if (method.name == "main") {
            throw SemanticException("Cannot have a \"main\" function in a script")
          }
          classNode.addMethod(method)
        }
        else -> mainFunction.statements.add(statement(scope))
      }
    }
    return moduleNode
  }

  private fun method(): MethodNode {
    // TODO handle static functions
    val visibility = acceptOptional(TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED, TokenType.VISIBILITY_HIDDEN, TokenType.VISIBILITY_PRIVATE)
      ?: TokenType.VISIBILITY_PUBLIC
    accept(TokenType.FUN)
    val methodName = accept(TokenType.IDENTIFIER).value
    accept(TokenType.LPAR)
    val parameters = mutableListOf<MethodParameter>()
    while (current.type != TokenType.RPAR) {
      val type = parseType()
      val argName = accept(TokenType.IDENTIFIER).value
      if (parameters.any { it.name == argName }) {
        throw SemanticException("Cannot two method parameters with the same name")
      }
      parameters.add(MethodParameter(type, argName))
      if (current.type == TokenType.RPAR) {
        break
      } else {
        // TODO handle argument default values
        accept(TokenType.COMMA)
      }
    }
    skip() // skipping RPAR
    val returnType = if (current.type != TokenType.BRACKETS_OPEN) parseType() else JavaPrimitiveType.VOID
    accept(TokenType.BRACKETS_OPEN)
    val statements = mutableListOf<StatementNode>()
    val scope = Scope() // TODO use scope from classNode to access class variables
    while (current.type != TokenType.BRACKETS_CLOSE) {
      statements.add(statement(scope))
    }
    skip() // skipping BRACKETS_CLOSE
    // TODO determine access Opcodes based on visibility variable
    return MethodNode(Opcodes.ACC_PUBLIC, methodName, statements, parameters, returnType, scope)
  }

  private fun parseType(): JavaType {
    val token = next()
    return when (token.type) {
      TokenType.TYPE_INT -> JavaPrimitiveType.INT
      TokenType.TYPE_LONG -> JavaPrimitiveType.LONG
      TokenType.TYPE_VOID -> JavaPrimitiveType.VOID
      TokenType.TYPE_FLOAT -> JavaPrimitiveType.FLOAT
      TokenType.TYPE_DOUBLE -> JavaPrimitiveType.DOUBLE
      else -> throw java.lang.UnsupportedOperationException("Doesn't handle type ${token.type}")
    }
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
    accept(TokenType.ASSIGNMENT)
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
          skip() // skipping RPAR
          return fCall
        } else if (current.type == TokenType.ASSIGNMENT) {
          skip()
          VariableAssignmentNode(token.value, expression(scope))
        } else {
          VariableReferenceExpression(token.value, scope)
        }
      }
      TokenType.MINUS -> UnaryMinus(atom(scope))
      TokenType.PLUS -> UnaryPlus(atom(scope))
      TokenType.LPAR -> {
        val node = expression(scope)
        if (current.type != TokenType.RPAR) {
          throw MarcelParsingException("Parenthesis should be close")
        }
        next()
        return node
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

  private fun acceptOptional(vararg types: TokenType): LexToken? {
    val token = currentSafe
    if (token?.type in types) {
      currentIndex++
    }
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