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
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Binding
import marcel.lang.Script

import org.objectweb.asm.Opcodes
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

class MarcelParser(private val classSimpleName: String, private val tokens: List<LexToken>) {

  constructor(tokens: List<LexToken>): this("MarcelRandomClass_" + abs(ThreadLocalRandom.current().nextInt()), tokens)

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
    val node = script()
    return node
  }

  // TODO pass args as parameter
  fun script(): ModuleNode {

    val imports = mutableListOf<ImportNode>()
    while (current.type == TokenType.IMPORT) {
      imports.add(import())
    }
    val classMethods = mutableListOf<MethodNode>()
    val superType = JavaType(Script::class.java)
    val className = classSimpleName
    val classType = JavaType(className)
    val scope = Scope(imports, className, AsmUtils.getInternalName(superType), classMethods)
    val statements = mutableListOf<StatementNode>()
    val runBlock = FunctionBlockNode(JavaType.void, statements)
    val runFunction = MethodNode(Opcodes.ACC_PUBLIC, classType,
      "run",
      runBlock, mutableListOf(), runBlock.methodReturnType, MethodScope(scope, className, emptyList(), JavaType.OBJECT)
    )

    // adding script constructors script have 2 constructors. One no-arg constructor, and one for Binding
    val bindingType = JavaType(Binding::class.java)
    classMethods.add(
      ConstructorNode(superType, Opcodes.ACC_PUBLIC, FunctionBlockNode(JavaType.void, emptyList()), mutableListOf(), MethodScope(scope, JavaMethod.CONSTRUCTOR_NAME, emptyList(), JavaType.void)),
    )
    classMethods.add(
      ConstructorNode(superType, Opcodes.ACC_PUBLIC, FunctionBlockNode(JavaType.void, listOf(
        ExpressionStatementNode(SuperConstructorCallNode(scope, mutableListOf(VariableReferenceExpression(
          Scope().apply { addLocalVariable(bindingType, "binding") }
          , "binding"))))
      )), mutableListOf(MethodParameter(bindingType, "binding")), MethodScope(scope, JavaMethod.CONSTRUCTOR_NAME, listOf(MethodParameter(bindingType, "binding")), JavaType.void))
    )
    classMethods.add(runFunction)
    // TODO copy scope once generating right main block
    classMethods.add(generateMainMethod(className, scope, runBlock))
    val classNode = ClassNode(
      Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER, classType, JavaType(Script::class.java), classMethods)
    val moduleNode = ModuleNode(mutableListOf(classNode))

    while (current.type != TokenType.END_OF_FILE) {
      when (current.type) {
        TokenType.FUN, TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED,
        TokenType.VISIBILITY_HIDDEN, TokenType.VISIBILITY_PRIVATE -> {
          val method = method(scope, classNode)
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
        VariableDeclarationNode(scriptType, scriptVar, ConstructorCallNode(Scope(), scriptType, mutableListOf())),
    ))
    scope.addLocalVariable(scriptType, scriptVar)
    return MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, scriptType,
        "main",
        blockNode /*FunctionBlockNode(JavaType.void, blockNode)*/, mutableListOf(MethodParameter(JavaType(Array<String>::class.java), "args")), JavaType.void,
      MethodScope(scope, "main", listOf(MethodParameter(JavaType(Array<String>::class.java), "args")), JavaType.void),
    )
  }

  private fun import(): ImportNode {
    accept(TokenType.IMPORT)
    val classParts = mutableListOf(accept(TokenType.IDENTIFIER).value)
    while (current.type == TokenType.DOT) {
      skip()
      if (current.type == TokenType.MUL) {
        skip()
        acceptOptional(TokenType.SEMI_COLON)
        return WildcardImportNode(classParts.joinToString(separator = "."))
      }
      classParts.add(accept(TokenType.IDENTIFIER).value)
    }
    if (classParts.size <= 1) {
      throw MarcelParsingException("Invalid class full name" + classParts.joinToString(separator = "."))
    }
    val asName = if (acceptOptional(TokenType.AS) != null) accept(TokenType.IDENTIFIER).value else null
    acceptOptional(TokenType.SEMI_COLON)
    return SimpleImportNode(classParts.joinToString(separator = "."), asName)
  }

  internal fun method(classScope: Scope, classNode: ClassNode): MethodNode {
    // TODO handle static functions
    val visibility = acceptOptional(TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED, TokenType.VISIBILITY_HIDDEN, TokenType.VISIBILITY_PRIVATE)
      ?: TokenType.VISIBILITY_PUBLIC
    accept(TokenType.FUN)
    val methodName = accept(TokenType.IDENTIFIER).value
    accept(TokenType.LPAR)
    val parameters = mutableListOf<MethodParameter>()
    while (current.type != TokenType.RPAR) {
      val type = parseType(classScope)
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
    val returnType = if (current.type != TokenType.BRACKETS_OPEN) parseType(classScope) else JavaType.void
    val statements = mutableListOf<StatementNode>()
    // TODO determine access Opcodes based on visibility variable
    val methodScope = MethodScope(classScope, methodName, parameters, returnType)
    val methodNode = MethodNode(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, classNode.type, methodName, FunctionBlockNode(returnType, statements), parameters, returnType, methodScope)
    statements.addAll(block(methodScope).statements)
    return methodNode
  }


  fun block(scope: Scope): BlockNode {
    accept(TokenType.BRACKETS_OPEN)
    val statements = mutableListOf<StatementNode>()
    while (current.type != TokenType.BRACKETS_CLOSE) {
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

  private fun parseType(scope: Scope): JavaType {
    val token = next()
    return when (token.type) {
      TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_VOID,
      TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE -> JavaType.TOKEN_TYPE_MAP.getValue(token.type)
      TokenType.IDENTIFIER -> JavaType(scope.resolveClassName(token.value))
      else -> throw java.lang.UnsupportedOperationException("Doesn't handle type ${token.type}")
    }
  }

  internal fun statement(scope: Scope): StatementNode {
    val token = next()
    return when (token.type) {
      TokenType.TYPE_INT, TokenType.TYPE_LONG,
      TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE  -> variableDeclaration(scope,
          JavaType.TOKEN_TYPE_MAP.getValue(token.type))
      TokenType.RETURN -> {
        val expression = if (current.type == TokenType.SEMI_COLON) VoidExpression() else expression(scope)
        ReturnNode(expression)
      }
      else -> {
        if (token.type == TokenType.IDENTIFIER && current.type == TokenType.IDENTIFIER) {
          val className = scope.resolveClassName(token.value)
          return variableDeclaration(scope, JavaType(className))
        }
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
      TokenType.OPEN_QUOTE -> {
        val parts = mutableListOf<ExpressionNode>()
        while (current.type != TokenType.CLOSING_QUOTE) {
          parts.add(stringPart(scope))
        }
        skip() // skip last quote
        StringNode(parts)
      }
      TokenType.NEW -> {
        val classSimpleName = accept(TokenType.IDENTIFIER).value
        val className = scope.resolveClassName(classSimpleName)
        accept(TokenType.LPAR)
        // TODO add parameters in scope
        ConstructorCallNode(Scope(), JavaType(className), parseFunctionArguments(scope)).apply {
          methodOwnerType = JavaType(className)
        }
      }
      TokenType.IDENTIFIER -> {
        if (current.type == TokenType.LPAR) {
          skip()
          return FunctionCallNode(scope, token.value, parseFunctionArguments(scope))
        } else if (current.type == TokenType.ASSIGNMENT) {
          skip()
          VariableAssignmentNode(token.value, expression(scope))
        } else {
          VariableReferenceExpression(scope, token.value)
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
        throw UnsupportedOperationException("Not supported yet $token")
      }
    }
  }

  private fun stringPart(scope: Scope): ExpressionNode {
    val token = next()
    return when (token.type) {
      TokenType.REGULAR_STRING_PART -> StringConstantNode(token.value)
      TokenType.SHORT_TEMPLATE_ENTRY_START -> VariableReferenceExpression(scope, accept(TokenType.IDENTIFIER).value)
      TokenType.LONG_TEMPLATE_ENTRY_START -> {
        val expr = expression(scope)
        accept(TokenType.LONG_TEMPLATE_ENTRY_END)
        expr
      }
      else -> {
        rollback()
        expression(scope)
      }
    }
  }
  // assuming we already passed LPAR
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
      TokenType.DOT -> {
        if (rightOperand !is FunctionCallNode) {
          throw MarcelParsingException("Can only handle function calls with dot operators")
        }
        AccessOperator(leftOperand, rightOperand.apply {
          methodOwnerType = leftOperand
        })
      }
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
      return token
    } else {
      return null
    }
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