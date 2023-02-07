package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ParserUtils.isTypeToken
import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.InnerScope
import com.tambapps.marcel.parser.scope.LambdaScope
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Binding
import marcel.lang.Script

import org.objectweb.asm.Opcodes
import java.lang.NumberFormatException
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

class MarcelParser(private val typeResolver: AstNodeTypeResolver, private val classSimpleName: String, private val tokens: List<LexToken>) {

  constructor(typeResolver: AstNodeTypeResolver, tokens: List<LexToken>): this(typeResolver, "MarcelRandomClass_" + abs(ThreadLocalRandom.current().nextInt()), tokens)

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
    val packageName =
      if (current.type == TokenType.PACKAGE) parsePackage()
      else null
    val imports = mutableListOf<ImportNode>()
    imports.addAll(Scope.DEFAULT_IMPORTS)

    while (current.type == TokenType.IMPORT) {
      imports.add(import())
    }

    val moduleNode = ModuleNode()
    while (current.type != TokenType.END_OF_FILE) {
      if (current.type == TokenType.CLASS
        || lookup(1)?.type == TokenType.CLASS
        || lookup(2)?.type == TokenType.CLASS) {
        moduleNode.classes.add(parseClass(imports, packageName))
      } else {
        moduleNode.classes.add(script(imports, packageName))
      }
    }
    return moduleNode
  }

  private fun parseClass(imports: MutableList<ImportNode>, packageName: String?, outerClassNode: ClassNode? = null): ClassNode {
    val (access, isInline) = parseAccess()
    if (isInline) throw MarcelParsingException("Cannot use 'inline' keyword for a class")
    val isExtensionClass = acceptOptional(TokenType.EXTENSION) != null
    accept(TokenType.CLASS)
    val classSimpleName = accept(TokenType.IDENTIFIER).value
    val className = if (packageName != null) "$packageName.$classSimpleName" else classSimpleName
    val parseTypeScope = outerClassNode?.scope ?: Scope(typeResolver, JavaType.Object, imports)
    val superType =
      if (acceptOptional(TokenType.EXTENDS) != null) parseType(parseTypeScope)
      else JavaType.Object
    val interfaces = mutableListOf<JavaType>()
    if (acceptOptional(TokenType.IMPLEMENTS) != null) {
      while (current.type == TokenType.IDENTIFIER) {
        interfaces.add(parseType(parseTypeScope))
        acceptOptional(TokenType.COMMA)
      }
    }
    // TODO use interfaces
    val classType = typeResolver.defineClass(outerClassNode?.type, className, superType, false, interfaces)
    val classScope = Scope(typeResolver, imports, classType, superType)
    val methods = mutableListOf<MethodNode>()
    val innerClasses = mutableListOf<ClassNode>()
    val classNode = ClassNode(classScope, access, classType, JavaType.Object, false, methods, innerClasses)
    accept(TokenType.BRACKETS_OPEN)

    while (current.type != TokenType.BRACKETS_CLOSE) {
      if (current.type == TokenType.CLASS
        || lookup(1)?.type == TokenType.CLASS
        || lookup(2)?.type == TokenType.CLASS) {
        innerClasses.add(parseClass(imports, packageName, classNode))
      } else {
        methods.add(method(classNode, isExtensionClass))
      }
    }
    skip() // skipping brackets close
    return classNode
  }

  private fun script(imports: MutableList<ImportNode>, packageName: String?): ClassNode {
    val classMethods = mutableListOf<MethodNode>()
    val superType = JavaType.of(Script::class.java)
    val className = if (packageName != null) "$packageName.$classSimpleName" else classSimpleName
    val classType = typeResolver.defineClass(className, superType, false, emptyList())
    val classScope = Scope(typeResolver, imports, classType, superType)
    val runScope = MethodScope(classScope, "run", emptyList(), JavaType.Object)
    val statements = mutableListOf<StatementNode>()
    val runBlock = FunctionBlockNode(runScope, statements)
    val argsParameter = MethodParameter(JavaType.of(Array<String>::class.java), "args")
    val runFunction = MethodNode(Opcodes.ACC_PUBLIC, classType,
      "run",
      runBlock, mutableListOf(argsParameter), runScope.returnType, runScope, false)
    runScope.addLocalVariable(argsParameter.type, argsParameter.name)

    classMethods.add(runFunction)
    val innerClasses = mutableListOf<ClassNode>()
    val classNode = ClassNode(classScope,
      Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER, classType, JavaType.of(Script::class.java), true, classMethods, innerClasses)

    while (current.type != TokenType.END_OF_FILE) {
      if (current.type == TokenType.CLASS
        || lookup(1)?.type == TokenType.CLASS
        || lookup(2)?.type == TokenType.CLASS) {
        innerClasses.add(parseClass(imports, packageName, classNode))
      } else {
        when (current.type) {
          TokenType.FUN, TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED,
          TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE, TokenType.STATIC,
          TokenType.INLINE -> {
            val method = method(classNode)
            if (method.name == "main") {
              throw SemanticException("Cannot have a \"main\" function in a script")
            }
            classNode.addMethod(method)
          }
          else -> statements.add(statement(runScope))
        }
      }
    }
    return classNode
  }

  private fun import(): ImportNode {
    accept(TokenType.IMPORT)
    val staticImport = acceptOptional(TokenType.STATIC) != null
    val classParts = mutableListOf(accept(TokenType.IDENTIFIER).value)
    while (current.type == TokenType.DOT) {
      skip()
      if (current.type == TokenType.MUL) {
        if (staticImport) {
          throw MarcelParsingException("Invalid static import")
        }
        skip()
        acceptOptional(TokenType.SEMI_COLON)
        return WildcardImportNode(classParts.joinToString(separator = "."))
      }
      classParts.add(accept(TokenType.IDENTIFIER).value)
    }
    if (classParts.size <= 1) {
      throw MarcelParsingException("Invalid class full name" + classParts.joinToString(separator = "."))
    }
    val node = if (staticImport) {
      val className = classParts.subList(0, classParts.size - 1).joinToString(separator = ".")
      val method = classParts.last()
      StaticImportNode(className, method)
    } else {
      val asName = if (acceptOptional(TokenType.AS) != null) accept(TokenType.IDENTIFIER).value else null
      SimpleImportNode(classParts.joinToString(separator = "."), asName)
    }
    acceptOptional(TokenType.SEMI_COLON)
    return node
  }

  internal fun method(classNode: ClassNode, forceStatic: Boolean = false): MethodNode {
    val classScope = classNode.scope
    val (acc, isInline) = parseAccess()
    val access = if (forceStatic) acc or Opcodes.ACC_STATIC else acc
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
    val methodScope = MethodScope(classScope, methodName, parameters, returnType)
    val methodNode = MethodNode(access, classNode.type, methodName, FunctionBlockNode(methodScope, statements), parameters, returnType, methodScope, isInline)
    statements.addAll(block(methodScope).statements)
    return methodNode
  }


  private fun parseAccess(): Pair<Int, Boolean> {
    val staticFlag = if (acceptOptional(TokenType.STATIC) != null) Opcodes.ACC_STATIC else 0
    val isInline = acceptOptional(TokenType.INLINE) != null
    val visibilityFlag = ParserUtils.TOKEN_VISIBILITY_MAP[acceptOptional(TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED, TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE)?.type ?: TokenType.VISIBILITY_PUBLIC]!!
    return Pair(staticFlag or visibilityFlag, isInline)
  }

  fun block(scope: MethodScope, acceptBracketOpen: Boolean = true): BlockNode {
    if (acceptBracketOpen) accept(TokenType.BRACKETS_OPEN)
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
    return BlockNode(scope, statements)
  }

  private fun parseType(scope: Scope): JavaType {
    val token = next()
    return when (token.type) {
      TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_VOID,
      TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL -> {
        val type = JavaType.TOKEN_TYPE_MAP.getValue(token.type)
        if (acceptOptional(TokenType.SQUARE_BRACKETS_OPEN) != null) {
          accept(TokenType.SQUARE_BRACKETS_CLOSE)
          JavaType.ARRAYS.find { it.elementsType == type } ?: throw MarcelParsingException("Doesn't handle array of $type")
        } else type
      }
      TokenType.IDENTIFIER -> {
        val className = token.value
        if (className == JavaType.Object.className && acceptOptional(TokenType.SQUARE_BRACKETS_OPEN) != null) {
          accept(TokenType.SQUARE_BRACKETS_CLOSE)
          JavaType.objectArray
        } else {
          val genericTypes = mutableListOf<JavaType>()
          if (current.type == TokenType.LT) { // generic types
            skip()
            genericTypes.add(parseType(scope))
            while (current.type == TokenType.COMMA) {
              skip()
              genericTypes.add(parseType(scope))
            }
            accept(TokenType.GT)
          }
          JavaType.lazy(scope, className, genericTypes)
        }
      }
      else -> throw UnsupportedOperationException("Doesn't handle type ${token.type}")
    }
  }

  internal fun statement(scope: Scope): StatementNode {
    val token = next()
    return when (token.type) {
      TokenType.TYPE_INT, TokenType.TYPE_LONG,
      TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL -> {
        rollback()
        val type = parseType(scope)
        val identifier = accept(TokenType.IDENTIFIER)
        accept(TokenType.ASSIGNMENT)
        acceptOptional(TokenType.SEMI_COLON)
        VariableDeclarationNode(scope, type, identifier.value, expression(scope))
      }
      TokenType.RETURN -> {
        val expression = if (current.type == TokenType.SEMI_COLON) VoidExpression() else expression(scope)
        if (scope !is MethodScope) {
          throw MarcelParsingException("Cannot have a return instruction outside of a function")
        }
        acceptOptional(TokenType.SEMI_COLON)
        ReturnNode(scope, expression)
      }
      TokenType.BRACKETS_OPEN -> {
        rollback()
        if (scope !is MethodScope) {
          throw MarcelParsingException("Cannot have blocks outside of a method")
        }
        // starting a new inner scope for the block
        ExpressionStatementNode(block(InnerScope(scope)))
      }
      TokenType.IF -> {
        accept(TokenType.LPAR)
        val condition = BooleanExpressionNode(
          if (isTypeToken(current.type) && lookup(1)?.type == TokenType.IDENTIFIER) {
            val type = parseType(scope)
            val variableName = accept(TokenType.IDENTIFIER).value
            accept(TokenType.ASSIGNMENT)
            TruthyVariableDeclarationNode(scope, type, variableName, expression(scope))
          } else expression(scope)
        )
        accept(TokenType.RPAR)
        val rootIf = IfStatementNode(condition, statement(scope), null)
        var currentIf = rootIf
        while (current.type == TokenType.ELSE) {
          skip()
          if (acceptOptional(TokenType.IF) != null) {
            accept(TokenType.LPAR)
            val elseIfCondition = BooleanExpressionNode(expression(scope))
            accept(TokenType.RPAR)
            val newIf = IfStatementNode(elseIfCondition, statement(scope), null)
            currentIf.falseStatementNode = newIf
            currentIf = newIf
          } else {
            currentIf.falseStatementNode = statement(scope)
          }
        }
        rootIf
      }
      TokenType.WHILE -> {
        accept(TokenType.LPAR)
        val condition = BooleanExpressionNode(expression(scope))
        accept(TokenType.RPAR)
        WhileStatement(condition, loopBody(scope))
      }
      TokenType.FOR -> {
        accept(TokenType.LPAR)
        if (lookup(1)?.type == TokenType.IDENTIFIER && lookup(2)?.type == TokenType.IN) {
          // for in statement
          val type = parseType(scope)
          val identifier = accept(TokenType.IDENTIFIER).value
          accept(TokenType.IN)
          val expression = expression(scope)
          accept(TokenType.RPAR)
          var forBlock = loopBody(scope)
          ForInStatement(type, identifier, expression, forBlock)
        } else {
          // for (;;)
          // needed especially if initStatement is var declaration
          val scope = InnerScope(scope as? MethodScope ?: throw MarcelParsingException("Cannot have for outside of a method"))
          val initStatement = statement(scope)
          if (initStatement !is VariableAssignmentNode) {
            throw MarcelParsingException("For loops should start with variable declaration/assignment")
          }
          acceptOptional(TokenType.SEMI_COLON)
          val condition = BooleanExpressionNode(expression(scope))
          accept(TokenType.SEMI_COLON)
          val iteratorStatement = statement(scope)
          if (iteratorStatement !is VariableAssignmentNode && iteratorStatement !is ExpressionStatementNode) {
            throw MarcelParsingException("Invalid for loop")
          }
          accept(TokenType.RPAR)
          var forBlock = loopBody(scope)
          ForStatement(initStatement, condition, iteratorStatement, forBlock)
        }
      }
      TokenType.CONTINUE -> {
        if (scope !is InnerScope) {
          throw MarcelParsingException("Cannot have a continue outside of an inner block")
        }
        ContinueLoopNode(scope)
      }
      TokenType.BREAK -> {
        if (scope !is InnerScope) {
          throw MarcelParsingException("Cannot have a continue outside of an inner block")
        }
        BreakLoopNode(scope)
      }
      else -> {
        if (token.type == TokenType.IDENTIFIER && current.type == TokenType.IDENTIFIER
            // generic type
            || token.type == TokenType.IDENTIFIER && current.type == TokenType.LT
          || token.type == TokenType.IDENTIFIER && current.type == TokenType.SQUARE_BRACKETS_OPEN && lookup( 1)?.type == TokenType.SQUARE_BRACKETS_CLOSE) {
          rollback()
          val type = parseType(scope)
          val variableName = accept(TokenType.IDENTIFIER).value
          accept(TokenType.ASSIGNMENT)
          val v = VariableDeclarationNode(scope, type, variableName, expression(scope))
          acceptOptional(TokenType.SEMI_COLON)
          return v
        }
        rollback()
        val node = expression(scope)
        acceptOptional(TokenType.SEMI_COLON)
        ExpressionStatementNode(node)
      }
    }
  }

  fun expression(scope: Scope): ExpressionNode {
    val expr = expression(scope, Int.MAX_VALUE)
    return completedExpression(scope, expr)
  }

  private fun completedExpression(scope: Scope, expr: ExpressionNode): ExpressionNode {
    if (current.type == TokenType.QUESTION_MARK) {
      skip()
      if (current.type == TokenType.COLON) {
        skip()
        val onNullExpression = expression(scope)
        return ElvisOperator(expr, onNullExpression)
      } else {
        val trueExpr = expression(scope)
        accept(TokenType.COLON)
        val falseExpr = expression(scope)
        return TernaryNode(BooleanExpressionNode(expr), trueExpr, falseExpr)
      }
    } else if (current.type == TokenType.AS) {
      skip()
      val type = parseType(scope)
      return AsNode(type, expr)
    }
    return expr
  }
  private fun loopBody(scope: Scope): BlockNode {
    val loopStatement = statement(scope)
    if (loopStatement.expression is BlockNode) return loopStatement.expression as BlockNode
    val newScope = InnerScope(scope as? MethodScope ?: throw MarcelParsingException("Cannot have for outside of a method"))
    return BlockNode(newScope, listOf(loopStatement))
  }
  private fun expression(scope: Scope, maxPriority: Int): ExpressionNode {
    var a = atom(scope)
    var t = current
    while (ParserUtils.isBinaryOperator(t.type) && ParserUtils.getPriority(t.type) < maxPriority) {
      next()
      val leftOperand = a
      var rightOperand = expression(scope, ParserUtils.getPriority(t.type) + ParserUtils.getAssociativity(t.type))
      if (t.type in listOf(TokenType.ASSIGNMENT, TokenType.PLUS_ASSIGNMENT, TokenType.MINUS_ASSIGNMENT, TokenType.MUL_ASSIGNMENT, TokenType.DIV_ASSIGNMENT)) {
        // need this because we want to be able to use ternary expressions for assignments
        rightOperand = completedExpression(scope, rightOperand)
      }
      a = operator(scope, t.type, leftOperand, rightOperand)
      t = current
    }
    return a
  }

  private fun atom(scope: Scope): ExpressionNode {
    val token = next()
    return when (token.type) {
      TokenType.INCR -> IncrNode(ReferenceExpression(scope, accept(TokenType.IDENTIFIER).value), 1, false)
      TokenType.DECR -> IncrNode(ReferenceExpression(scope, accept(TokenType.IDENTIFIER).value), -1, false)
      TokenType.INTEGER, TokenType.FLOAT -> {
       return if (current.type == TokenType.LT || current.type == TokenType.GT || current.type == TokenType.TWO_DOTS) {
         rangeNode(scope, parseNumberConstant(token))
        } else {
         parseNumberConstant(token)
        }
      }
      TokenType.BRACKETS_OPEN -> parseLambda(scope)
      TokenType.VALUE_TRUE -> BooleanConstantNode(true)
      TokenType.VALUE_FALSE -> BooleanConstantNode(false)
      TokenType.OPEN_QUOTE -> {
        val parts = mutableListOf<ExpressionNode>()
        while (current.type != TokenType.CLOSING_QUOTE) {
          parts.add(stringPart(scope))
        }
        skip() // skip last quote
        StringNode(parts)
      }
      TokenType.NULL -> NullValueNode()
      TokenType.NOT -> NotNode(expression(scope))
      TokenType.NEW -> {
        val type = parseType(scope)
        accept(TokenType.LPAR)
        ConstructorCallNode(Scope(typeResolver, type), type, parseFunctionArguments(scope))
      }
      TokenType.IDENTIFIER -> {
        if (current.type == TokenType.INCR) {
          skip()
          IncrNode(ReferenceExpression(scope, token.value), 1, true)
        } else  if (current.type == TokenType.DECR) {
          skip()
          IncrNode(ReferenceExpression(scope, token.value), -1, true)
        }
        else if (current.type == TokenType.LPAR) {
          skip()
          FunctionCallNode(scope, token.value, parseFunctionArguments(scope))
        } else if (current.type == TokenType.BRACKETS_OPEN) { // function call with a lambda
          skip()
          FunctionCallNode(scope, token.value, mutableListOf(parseLambda(scope)))
        } else if (current.type == TokenType.LT  && lookup(1)?.type == TokenType.TWO_DOTS
          || current.type == TokenType.GT && lookup(1)?.type == TokenType.TWO_DOTS || current.type == TokenType.TWO_DOTS) {
          rangeNode(scope, ReferenceExpression(scope, token.value))
        } else if (current.type == TokenType.SQUARE_BRACKETS_OPEN
          || (current.type == TokenType.QUESTION_MARK && lookup(1)?.type == TokenType.SQUARE_BRACKETS_OPEN)) {
          val isSafeIndex = acceptOptional(TokenType.QUESTION_MARK) != null
          skip()
          val indexArguments = mutableListOf<ExpressionNode>()
          while (current.type != TokenType.SQUARE_BRACKETS_CLOSE) {
            indexArguments.add(expression(scope))
            if (current.type == TokenType.COMMA) skip()
          }
          skip() // skip brackets close
          IndexedReferenceExpression(scope, token.value, indexArguments, isSafeIndex)
        } else {
          ReferenceExpression(scope, token.value)
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
        if (current.type == TokenType.LT  && lookup(1)?.type == TokenType.TWO_DOTS
          || current.type == TokenType.GT && lookup(1)?.type == TokenType.TWO_DOTS || current.type == TokenType.TWO_DOTS) {
          return rangeNode(scope, ReferenceExpression(scope, token.value))
        }
        return node
      }
      TokenType.SQUARE_BRACKETS_OPEN -> {
        val elements = mutableListOf<ExpressionNode>()
        var isMap = false
        while (current.type != TokenType.SQUARE_BRACKETS_CLOSE) {
          elements.add(expression(scope))
          if (current.type == TokenType.COLON) {
            isMap = true
            skip()
            elements.add(expression(scope))
          }
          if (current.type == TokenType.COMMA) {
            skip()
          }
        }
        next() // skip square brackets close
        if (isMap) {
          val entries = mutableListOf<Pair<ExpressionNode, ExpressionNode>>()
          for (i in elements.indices step 2) {
            entries.add(Pair(elements[i], elements[i + 1]))
          }
          LiteralMapNode(entries)
        } else LiteralArrayNode(elements)
      }
      else -> {
        throw UnsupportedOperationException("Not supported yet $token")
      }
    }
  }

  private fun parseLambda(scope: Scope): LambdaNode {
    val parameters = mutableListOf<MethodParameter>()
    val methodScope = MethodScope(scope, "TODO", parameters, JavaType.Object)
    if (lookup(2)?.type == TokenType.ARROW || lookup(3)?.type == TokenType.ARROW
        || lookup(2)?.type == TokenType.COMMA || lookup(3)?.type == TokenType.COMMA) {
      while (current.type != TokenType.ARROW) {
        val firstToken = accept(TokenType.IDENTIFIER)
        val parameter = if (current.type == TokenType.IDENTIFIER) {
          rollback()
          MethodParameter(parseType(scope), accept(TokenType.IDENTIFIER).value)
        } else MethodParameter(JavaType.Object, firstToken.value)
        parameters.add(parameter)
        if (current.type == TokenType.COMMA) skip()
      }
    }
    // now parse function block
    val block = block(MethodScope(scope, "invoke", parameters, JavaType.Object), false)
    return LambdaNode(LambdaScope(scope), parameters, block)
  }

  private fun parseNumberConstant(token: LexToken): ExpressionNode {
    if (token.type == TokenType.INTEGER) {
      var valueString = token.value.lowercase(Locale.ENGLISH)
      val isLong = valueString.endsWith("l")
      if (isLong) valueString = valueString.substring(0, valueString.length - 1)

      val radix = if (valueString.startsWith("0x")) 16
      else if (valueString.startsWith("0b")) 2
      else 10

      return if (isLong) {
        val value = try {
          valueString.toLong(radix)
        } catch (e: NumberFormatException) {
          throw MarcelParsingException(e)
        }
        LongConstantNode(value)
      } else {
        val value = try {
          valueString.toInt(radix)
        } catch (e: NumberFormatException) {
          throw MarcelParsingException(e)
        }
        IntConstantNode(value)
      }
    } else if (token.type == TokenType.FLOAT) {
      var valueString = token.value.lowercase(Locale.ENGLISH)
      val isDouble = valueString.endsWith("d")
      if (isDouble) valueString = valueString.substring(0, valueString.length - 1)

      return if (isDouble) {
        val value = try {
          valueString.toDouble()
        } catch (e: NumberFormatException) {
          throw MarcelParsingException(e)
        }
        DoubleConstantNode(value)
      } else {
        val value = try {
          valueString.toFloat()
        } catch (e: NumberFormatException) {
          throw MarcelParsingException(e)
        }
        FloatConstantNode(value)
      }
    } else {
      throw MarcelParsingException("Unexpected token $token")
    }
  }
  private fun rangeNode(scope: Scope, fromExpression: ExpressionNode): RangeNode {
    val fromExclusive = acceptOptional(TokenType.LT, TokenType.GT) != null
    accept(TokenType.TWO_DOTS)
    val toExclusive = acceptOptional(TokenType.LT, TokenType.GT) != null
    val toExpression = atom(scope)
    return RangeNode(fromExpression, toExpression, fromExclusive, toExclusive)
  }

  private fun stringPart(scope: Scope): ExpressionNode {
    val token = next()
    return when (token.type) {
      TokenType.REGULAR_STRING_PART -> StringConstantNode(token.value)
      TokenType.SHORT_TEMPLATE_ENTRY_START -> ReferenceExpression(scope, accept(TokenType.IDENTIFIER).value)
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

  private fun operator(scope: Scope, t: TokenType, leftOperand: ExpressionNode, rightOperand: ExpressionNode): ExpressionNode {
    return when(t) {
      TokenType.ASSIGNMENT, TokenType.PLUS_ASSIGNMENT, TokenType.MINUS_ASSIGNMENT, TokenType.MUL_ASSIGNMENT, TokenType.DIV_ASSIGNMENT -> {
        if (t == TokenType.ASSIGNMENT) {
          when (leftOperand) {
            is ReferenceExpression -> VariableAssignmentNode(scope, leftOperand.name, rightOperand)
            is IndexedReferenceExpression -> IndexedVariableAssignmentNode(scope, leftOperand, rightOperand)
            else -> throw MarcelParsingException("Can only assign a variable")
          }
        } else {
          if (leftOperand !is ReferenceExpression) throw MarcelParsingException("Can only assign a variable")
          val actualRightOperand = when(t) {
            TokenType.PLUS_ASSIGNMENT -> PlusOperator(leftOperand, rightOperand)
            TokenType.MINUS_ASSIGNMENT -> MinusOperator(leftOperand, rightOperand)
            TokenType.DIV_ASSIGNMENT -> DivOperator(leftOperand, rightOperand)
            TokenType.MUL_ASSIGNMENT -> MulOperator(leftOperand, rightOperand)
            else -> throw RuntimeException("Compiler error")
          }
          VariableAssignmentNode(scope, leftOperand.name, actualRightOperand)
        }
      }
      TokenType.MUL -> MulOperator(leftOperand, rightOperand)
      TokenType.DIV -> DivOperator(leftOperand, rightOperand)
      TokenType.PLUS -> PlusOperator(leftOperand, rightOperand)
      TokenType.MINUS -> MinusOperator(leftOperand, rightOperand)
      TokenType.AND -> AndOperator(leftOperand, rightOperand)
      TokenType.OR -> OrOperator(leftOperand, rightOperand)
      TokenType.LEFT_SHIFT -> LeftShiftOperator(leftOperand, rightOperand)
      TokenType.RIGHT_SHIFT -> RightShiftOperator(leftOperand, rightOperand)
      TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LT, TokenType.GT, TokenType.LOE, TokenType.GOE -> ComparisonOperatorNode(ComparisonOperator.fromTokenType(t), leftOperand, rightOperand)
      TokenType.DOT -> when (rightOperand) {
        is FunctionCallNode -> InvokeAccessOperator(leftOperand, rightOperand)
        is ReferenceExpression -> GetFieldAccessOperator(leftOperand, rightOperand)
        else -> throw MarcelParsingException("Can only handle function calls and fields with dot operators")
      }
      TokenType.QUESTION_DOT -> when (rightOperand) {
        is FunctionCallNode -> NullSafeInvokeAccessOperator(leftOperand, rightOperand)
        is ReferenceExpression -> NullSafeGetFieldAccessOperator(leftOperand, rightOperand)
        else -> throw MarcelParsingException("Can only handle function calls and fields with dot operators")
      }
      else -> throw MarcelParsingException("Doesn't handle operator with token type $t")
    }
  }

  private fun parsePackage(): String {
    accept(TokenType.PACKAGE)
    val parts = mutableListOf<String>(accept(TokenType.IDENTIFIER).value)
    while (current.type == TokenType.DOT) {
      skip()
      parts.add(accept(TokenType.IDENTIFIER).value)
    }
    acceptOptional(TokenType.SEMI_COLON)
    return parts.joinToString(separator = ".")
  }

  private fun accept(t: TokenType): LexToken {
    val token = current
    if (token.type != t) {
      throw MarcelParsingException("Expected token of type $t but got ${token.type}")
    }
    currentIndex++
    return token
  }

  private fun accept(vararg types: TokenType): LexToken {
    val token = current
    if (token.type !in types) {
      throw MarcelParsingException("Expected token of type ${types.contentToString()} but got ${token.type}")
    }
    currentIndex++
    return token
  }

  private fun acceptOptional(vararg types: TokenType): LexToken? {
    val token = currentSafe
    if (token?.type in types) {
      currentIndex++
      return token
    }
    return null
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

  private fun lookup(howFar: Int): LexToken? {
    return tokens.getOrNull(currentIndex + howFar)
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