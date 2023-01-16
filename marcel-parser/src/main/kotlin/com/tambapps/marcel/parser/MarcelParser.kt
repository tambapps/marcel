package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.owner.StaticOwner
import com.tambapps.marcel.parser.scope.InMethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Binding
import marcel.lang.Script

import org.objectweb.asm.Opcodes
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

class MarcelParser(private val classSimpleName: String, private val tokens: List<LexToken>) {

  constructor(tokens: List<LexToken>): this("MarcelRandomClass_" + abs(ThreadLocalRandom.current().nextInt()), tokens)

  private var currentIndex = 0
  private val resolvableNodes = mutableListOf<Pair<ResolvableNode, Scope>>()

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
    val node = script()
    resolvableNodes.forEach { it.first.resolve(it.second) }
    return node
  }

  // TODO pass args as parameter
  fun script(): ModuleNode {
    val classMethods = mutableListOf<MethodNode>()
    val superType = JavaType(Script::class.java)
    val scope = Scope(AsmUtils.getInternalName(superType), classMethods)
    val statements = mutableListOf<StatementNode>()
    val runBlock = FunctionBlockNode(JavaType.void, statements)
    val className = classSimpleName
    val runFunction = MethodNode(Opcodes.ACC_PUBLIC, StaticOwner(AsmUtils.getInternalName(className)),
      "run",
      runBlock, mutableListOf(), runBlock.methodReturnType, scope
    )

    // adding script constructors script have 2 constructors. One no-arg constructor, and one for Binding
    val bindingType = JavaType(Binding::class.java)
    classMethods.add(
      ConstructorNode(superType, Opcodes.ACC_PUBLIC, FunctionBlockNode(JavaType.void, emptyList()), mutableListOf(), scope.copy()),
    )
    classMethods.add(
      ConstructorNode(superType, Opcodes.ACC_PUBLIC, FunctionBlockNode(JavaType.void, listOf(
        ExpressionStatementNode(SuperConstructorCallNode(mutableListOf(VariableReferenceExpression(bindingType, "binding"))))
      )), mutableListOf(MethodParameter(bindingType, "binding")), scope.copy())
    )
    classMethods.add(runFunction)
    // TODO copy scope once generating right main block
    classMethods.add(generateMainMethod(className, scope, runBlock))
    val classNode = ClassNode(
      Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER, className, JavaType(Script::class.java), classMethods)
    val moduleNode = ModuleNode(mutableListOf(classNode))

    while (current.type != TokenType.END_OF_FILE) {
      when (current.type) {
        TokenType.FUN, TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED,
        TokenType.VISIBILITY_HIDDEN, TokenType.VISIBILITY_PRIVATE -> {
          val method = method(classNode)
          if (method.name == "main") {
            throw SemanticException("Cannot have a \"main\" function in a script")
          }
          classNode.addMethod(method)
        }
        else -> statements.add(statement(scope))
      }
    }
    return moduleNode
  }

  private fun generateMainMethod(className: String, scope: Scope, blockNode: FunctionBlockNode): MethodNode {
    // TODO remove blockNode argument and generate my own function block. In it I should instantiate a $className and call the method run(args)
    val statements = mutableListOf<StatementNode>()
    val mainBlockNode = FunctionBlockNode(JavaType.void, statements)
    val scriptVar = "script"
    val scriptType = JavaType(className)
    statements.addAll(listOf(
        VariableDeclarationNode(scriptType, scriptVar, ConstructorCallNode(scriptType, mutableListOf())),
    ))
    // TODO need to handle myVar.methodCall() to be able to have a
    scope.addLocalVariable(scriptType, scriptVar)
    return MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, StaticOwner(AsmUtils.getInternalName(className)),
        "main",
        blockNode /*FunctionBlockNode(JavaType.void, blockNode)*/, mutableListOf(MethodParameter(JavaType(Array<String>::class.java), "args")), JavaType.void, scope
    )
  }

  private fun method(classNode: ClassNode): MethodNode {
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
    val returnType = if (current.type != TokenType.BRACKETS_OPEN) parseType() else JavaType.void
    val scope = Scope(classNode)
    val statements = mutableListOf<StatementNode>()
    val methodNode = MethodNode(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, StaticOwner(classNode.name), methodName, FunctionBlockNode(returnType, statements), parameters, returnType, scope)
    resolvableNodes.add(Pair(methodNode, scope))
    statements.addAll(block(InMethodScope(scope, methodNode)).statements)
    // TODO determine access Opcodes based on visibility variable
    return methodNode
  }


  fun block(scope: Scope): BlockNode {
    accept(TokenType.BRACKETS_OPEN)
    val statements = mutableListOf<StatementNode>()
    while (current.type != TokenType.BRACKETS_CLOSE) {
      // TODO check if statement is return, if the return type matches the returnType
      val statement = statement(scope)
      if (statements.lastOrNull() is ReturnNode) {
        // we have another statement after a return? shouldn't be possible
        throw SemanticException("Cannot have other statements after a return")
      }
      statements.add(statement)
    }
    skip() // skipping BRACKETS_CLOSE
    return BlockNode(statements)
  }

  private fun parseType(): JavaType {
    val token = next()
    return when (token.type) {
      TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_VOID,
      TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE -> JavaType.TOKEN_TYPE_MAP.getValue(token.type)
      else -> throw java.lang.UnsupportedOperationException("Doesn't handle type ${token.type}")
    }
  }

  private fun statement(scope: Scope): StatementNode {
    val token = next()
    return when (token.type) {
      TokenType.TYPE_INT, TokenType.TYPE_LONG,
      TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE  -> variableDeclaration(scope,
          JavaType.TOKEN_TYPE_MAP.getValue(token.type))
      TokenType.RETURN -> {
        val expression = if (current.type == TokenType.SEMI_COLON) VoidExpression() else expression(scope)
        ReturnNode(expression).apply {
          resolvableNodes.add(Pair(this, scope))
        }
      }
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
      TokenType.NEW -> {
        val identifier = accept(TokenType.IDENTIFIER).value
        // TODO resolve with imports
        ConstructorCallNode(JavaType(identifier), parseFunctionArguments(scope))
      }
      TokenType.IDENTIFIER -> {
        if (current.type == TokenType.LPAR) {
          skip()
          return FunctionCallNode(token.value, parseFunctionArguments(scope)).apply {
            resolvableNodes.add(Pair(this, scope))
          }
        } else if (current.type == TokenType.ASSIGNMENT) {
          skip()
          VariableAssignmentNode(token.value, expression(scope))
        }  else if (current.type == TokenType.DOT) {
          skip()
          // TODO pass right scope
          val expression = atom(scope)
          if (expression !is FunctionCallNode) {
            throw SemanticException("Only supports function calls after a dot")
          }
          AccessMethodNode(token.value, expression)
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

  private fun parseFunctionArguments(scope: Scope): MutableList<ExpressionNode> {
    val arguments = mutableListOf<ExpressionNode>()
    while (current.type != TokenType.RPAR) {
      arguments.add(expression(scope))
      if (current.type == TokenType.RPAR) {
        break
      } else {
        accept(TokenType.COMMA)
      }
    }
    skip() // skipping RPAR
    return arguments
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