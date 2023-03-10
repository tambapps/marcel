package com.tambapps.marcel.parser

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ParserUtils.isTypeToken
import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.BlockStatement
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.MultiVariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.statement.TryCatchNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.InnerScope
import com.tambapps.marcel.parser.scope.LambdaScope
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Script

import org.objectweb.asm.Opcodes
import java.lang.NumberFormatException
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

class MarcelParser constructor(
  private val typeResolver: AstNodeTypeResolver,
  private val classSimpleName: String, tokens: List<LexToken>,
  private val configuration: ParserConfiguration) {

  constructor(typeResolver: AstNodeTypeResolver, classSimpleName: String, tokens: List<LexToken>):
      this(typeResolver, classSimpleName, tokens, ParserConfiguration(independentScriptInnerClasses= false))

  private val tokens = tokens.filter { it.type != TokenType.WHITE_SPACE }

  constructor(typeResolver: AstNodeTypeResolver, tokens: List<LexToken>, configuration: ParserConfiguration): this(typeResolver, "MarcelRandomClass_" + abs(ThreadLocalRandom.current().nextInt()), tokens, configuration)
  constructor(typeResolver: AstNodeTypeResolver, tokens: List<LexToken>): this(typeResolver, tokens, ParserConfiguration(independentScriptInnerClasses = false))

  private var currentIndex = 0

  private val current: LexToken
    get() {
      checkEof()
      return tokens[currentIndex]
    }

  private val previous get() = tokens[currentIndex - 1]

  private val eof: Boolean
    get() = currentIndex >= tokens.size
  private val currentSafe: LexToken?
    get() = if (eof) null else tokens[currentIndex]

  init {
    if (configuration.scriptClass != null && !Script::class.java.isAssignableFrom(configuration.scriptClass)) {
      throw MarcelParserException("scriptSuperClass should be a subclass of marcel.lang.Script", false)
    }
    if (configuration.scriptInterfaces.any { !it.isInterface }) {
      throw MarcelParserException("Script interfaces should ve java interfaces", false)
    }
  }

  fun parse(): ModuleNode {
    val packageName =
      if (current.type == TokenType.PACKAGE) parsePackage()
      else null
    val imports = mutableListOf<ImportNode>()
    imports.addAll(Scope.DEFAULT_IMPORTS)

    val dumbbells = mutableSetOf<String>()
    while (current.type == TokenType.DUMBBELL) {
      dumbbells.add(dumbbell())
    }

    while (current.type == TokenType.IMPORT) {
      imports.add(import())
    }

    val moduleNode = ModuleNode(imports, dumbbells)
    while (current.type != TokenType.END_OF_FILE) {
      if (current.type == TokenType.CLASS
        || lookup(1)?.type == TokenType.CLASS
        || lookup(2)?.type == TokenType.CLASS) {
        moduleNode.classes.add(parseClass(imports, packageName))
      } else {
        moduleNode.classes.addAll(script(imports, packageName))
      }
    }
    return moduleNode
  }

  private fun parseField(classNode: ClassNode): FieldNode {
    val (access, isInline) = parseAccess()
    if (isInline) throw MarcelParserException(previous, "Cannot use 'inline' keyword for a field")
    val type = parseType(classNode.scope)
    val identifierToken = accept(TokenType.IDENTIFIER)
    val name = identifierToken.value

    if (classNode.fields.any { it.name == name }) throw MarcelParserException(previous,
      "Field with name $name was defined more than once"
    )

    val expression = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(
      // simulate a constructor scope as this would be executed in a constructor
      MethodScope(classNode.scope, JavaMethod.CONSTRUCTOR_NAME, emptyList(), classNode.type)
    )
    else null
    acceptOptional(TokenType.SEMI_COLON)

    return FieldNode(identifierToken, type, name, classNode.type, access, expression)
  }

  private fun parseClass(imports: MutableList<ImportNode>, packageName: String?, outerClassNode: ClassNode? = null): ClassNode {
    val (access, isInline) = parseAccess()
    if (isInline) throw MarcelParserException(previous, "Cannot use 'inline' keyword for a class")
    val isExtensionClass = acceptOptional(TokenType.EXTENSION) != null
    val classToken = accept(TokenType.CLASS)
    val classSimpleName = accept(TokenType.IDENTIFIER).value
    val className = if (packageName != null) "$packageName.$classSimpleName" else classSimpleName

    if (outerClassNode != null) {
      val conflictClass = outerClassNode.innerClasses.find { it.type.className == className }
      if (conflictClass != null) throw MarcelParserException(previous, "Class with name $className was defined more than once")
    }

    val parseTypeScope = outerClassNode?.scope
      // will set the actual classType later, as we didn't parse the class type yet
      ?: Scope(typeResolver, JavaType.Object, imports)
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
    val classType = JavaType.newType(outerClassNode?.type, className, superType, false, interfaces)
    if (outerClassNode == null) parseTypeScope.classType = classType  // setting the classType here
    val classScope = Scope(typeResolver, imports, classType)
    val methods = mutableListOf<MethodNode>()
    val classFields = mutableListOf<FieldNode>()
    val innerClasses = mutableListOf<ClassNode>()
    val classNode = ClassNode(classToken, classScope, access, classType, superType, false, methods, classFields, innerClasses)
    accept(TokenType.BRACKETS_OPEN)

    while (current.type != TokenType.BRACKETS_CLOSE) {
      // can be a class, a field or a function
      when (getNextMemberToken()) {
        TokenType.CLASS -> innerClasses.add(parseClass(imports, packageName, classNode))
        TokenType.FUN -> methods.add(method(classNode, isExtensionClass))
        // must be a type token
        else -> classFields.add(parseField(classNode))
      }
    }
    skip() // skipping brackets close
    return classNode
  }

  fun script(imports: MutableList<ImportNode>, packageName: String?): List<ClassNode> {
    val classMethods = mutableListOf<MethodNode>()
    val classFields = mutableListOf<FieldNode>()
    val superType = JavaType.of(Script::class.java)
    val className = if (packageName != null) "$packageName.$classSimpleName" else classSimpleName
    val classType = JavaType.newType(null, className, superType, false,
      configuration.scriptInterfaces.map { JavaType.of(it) })
    val classScope = Scope(typeResolver, imports, classType)
    val argsParameter = MethodParameterNode(JavaType.of(Array<String>::class.java), "args")
    val runScope = MethodScope(classScope, "run", listOf(argsParameter), JavaType.Object)
    val statements = mutableListOf<StatementNode>()
    val runBlock = FunctionBlockNode(LexToken.dummy(), runScope, statements)
    val runFunction = MethodNode(Opcodes.ACC_PUBLIC, classType,
      "run",
      runBlock, mutableListOf(argsParameter), runScope.returnType, runScope, false)

    classMethods.add(runFunction)
    val innerClasses = mutableListOf<ClassNode>()
    val classNode = ClassNode(LexToken.dummy(), classScope,
      Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER, classType, JavaType.of(Script::class.java),
      true, classMethods, classFields, innerClasses)
    val classNodes = mutableListOf(classNode)

    while (current.type != TokenType.END_OF_FILE) {
      when (current.type) {
        TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED, TokenType.FUN, TokenType.INLINE, TokenType.CLASS,
        TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE, TokenType.STATIC, -> {
          // can be a class, a field or a function
          when (getNextMemberToken()) {
            TokenType.CLASS -> {
              if (configuration.independentScriptInnerClasses) {
                classNodes.add(parseClass(imports, packageName, null))
              } else {
                innerClasses.add(parseClass(imports, packageName, classNode))
              }
            }
              TokenType.FUN -> {
              val method = method(classNode)
              if (method.name == "main") {
                throw MarcelSemanticException(classNode.token, "Cannot have a \"main\" function in a script")
              }
              classNode.addMethod(method)
            }
            // must be a type token
            else -> classFields.add(parseField(classNode))
          }
        }
        else -> statements.add(statement(runScope))
      }

    }
    return classNodes
  }

  // return next class, fun, or typetoken
  private fun getNextMemberToken(): TokenType {
    var i = currentIndex
    while (i < tokens.size && tokens[i].type !in listOf(TokenType.CLASS, TokenType.FUN) && !isTypeToken(tokens[i].type)) i++
    if (i >= tokens.size) throw MarcelParserException(current, "Unexpected tokens")
    return tokens[i].type
  }
  private fun dumbbell(): String {
    accept(TokenType.DUMBBELL)
    accept(TokenType.OPEN_SIMPLE_QUOTE)
    val d = simpleStringPart()
    accept(TokenType.CLOSING_SIMPLE_QUOTE)
    return d
  }

  fun import(): ImportNode {
    val importToken = accept(TokenType.IMPORT)
    val staticImport = acceptOptional(TokenType.STATIC) != null
    val classParts = mutableListOf(accept(TokenType.IDENTIFIER).value)
    while (current.type == TokenType.DOT) {
      skip()
      if (current.type == TokenType.MUL) {
        if (staticImport) {
          throw MarcelParserException(current, "Invalid static import")
        }
        skip()
        acceptOptional(TokenType.SEMI_COLON)
        return WildcardImportNode(importToken, classParts.joinToString(separator = "."))
      }
      classParts.add(accept(TokenType.IDENTIFIER).value)
    }
    if (classParts.size <= 1) {
      throw MarcelParserException(previous,
        "Invalid class full name" + classParts.joinToString(
          separator = "."
        )
      )
    }
    val node = if (staticImport) {
      val className = classParts.subList(0, classParts.size - 1).joinToString(separator = ".")
      val method = classParts.last()
      StaticImportNode(importToken, className, method)
    } else {
      val asName = if (acceptOptional(TokenType.AS) != null) accept(TokenType.IDENTIFIER).value else null
      SimpleImportNode(importToken, classParts.joinToString(separator = "."), asName)
    }
    acceptOptional(TokenType.SEMI_COLON)
    return node
  }

  internal fun method(classNode: ClassNode, forceStatic: Boolean = false): MethodNode {
    val classScope = classNode.scope
    val (acc, isInline) = parseAccess()
    val access = if (forceStatic) acc or Opcodes.ACC_STATIC else acc
    val token = accept(TokenType.FUN)
    val methodName = accept(TokenType.IDENTIFIER).value
    accept(TokenType.LPAR)
    val parameters = mutableListOf<MethodParameterNode>()
    while (current.type != TokenType.RPAR) {
      val type = parseType(classScope)
      val identifierToken = accept(TokenType.IDENTIFIER)
      val argName = identifierToken.value
      if (parameters.any { it.name == argName }) {
        throw MarcelSemanticException(token, "Cannot two method parameters with the same name")
      }
      val defaultValue = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(classScope) else null
      parameters.add(MethodParameterNode(identifierToken, type, argName, defaultValue))
      if (current.type == TokenType.RPAR) {
        break
      } else {
        accept(TokenType.COMMA)
      }
    }
    skip() // skipping RPAR
    val currentToken = current
    val returnType = if (current.type != TokenType.BRACKETS_OPEN) parseType(classScope) else JavaType.void
    val statements = mutableListOf<StatementNode>()
    val methodScope = MethodScope(classScope, methodName, parameters, returnType)
    val methodNode = MethodNode(access, classNode.type, methodName, FunctionBlockNode(currentToken, methodScope, statements), parameters, returnType, methodScope, isInline)
    statements.addAll(block(methodScope).statements)

    // check conflict
    val conflictMethod = classNode.methods.find { it.matches(methodNode) }
    if (conflictMethod != null) throw MarcelParserException(previous, "Method $methodNode conflicts with $conflictMethod")
    return methodNode
  }


  private fun parseAccess(): Pair<Int, Boolean> {
    val visibilityFlag = ParserUtils.TOKEN_VISIBILITY_MAP[acceptOptional(TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED, TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE)?.type ?: TokenType.VISIBILITY_PUBLIC]!!
    val staticFlag = if (acceptOptional(TokenType.STATIC) != null) Opcodes.ACC_STATIC else 0
    val finalFlag = if (acceptOptional(TokenType.FINAL) != null) Opcodes.ACC_FINAL else 0

    val isInline = acceptOptional(TokenType.INLINE) != null
    return Pair(staticFlag or visibilityFlag or finalFlag, isInline)
  }

  fun block(scope: MethodScope, acceptBracketOpen: Boolean = true): BlockNode {
    val token = current
    if (acceptBracketOpen) accept(TokenType.BRACKETS_OPEN)
    val statements = mutableListOf<StatementNode>()
    while (current.type != TokenType.BRACKETS_CLOSE) {
      val statement = statement(scope)
      if (statements.lastOrNull() is ReturnNode) {
        // we have another statement after a return? shouldn't be possible
        throw MarcelSemanticException(statement.token, "Cannot have other statements after a return")
      }
      statements.add(statement)
    }
    skip() // skipping BRACKETS_CLOSE
    return BlockNode(token, scope, statements)
  }

  private fun parseType(scope: Scope): JavaType {
    val token = next()
    return when (token.type) {
      TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_VOID, TokenType.TYPE_CHAR,
      TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL -> {
        val type = JavaType.TOKEN_TYPE_MAP.getValue(token.type)
        if (acceptOptional(TokenType.SQUARE_BRACKETS_OPEN) != null) {
          accept(TokenType.SQUARE_BRACKETS_CLOSE)
          JavaType.ARRAYS.find { it.elementsType == type } ?: throw MarcelParserException(previous,
            "Doesn't handle array of $type"
          )
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
      TokenType.END_OF_FILE -> throw MarcelParserException(token, "Unexpected end of file", true)
      else -> throw MarcelParserException(token, "Doesn't handle type ${token.type}")
    }
  }

  internal fun statement(scope: Scope): StatementNode {
    var token = next()
    return when (token.type) {
      TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_CHAR,
      TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL -> {
        rollback()
        val type = parseType(scope)
        val identifier = accept(TokenType.IDENTIFIER)
        val expression = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(scope)
        else null
        acceptOptional(TokenType.SEMI_COLON)
        VariableDeclarationNode(token, scope, type, identifier.value, false, expression)
      }
      TokenType.DEF -> {
        accept(TokenType.LPAR)
        val declarations = mutableListOf<Pair<JavaType, String>?>()
        while (current.type != TokenType.RPAR) {
          if (current.type == TokenType.IDENTIFIER && current.value.all { it == '_' }) {
            declarations.add(null)
            skip()
          } else {
            val varType = parseType(scope)
            val varName = accept(TokenType.IDENTIFIER).value
            declarations.add(Pair(varType, varName))
          }
          if (current.type == TokenType.COMMA) skip()
        }
        skip() // skip RPAR
        accept(TokenType.ASSIGNMENT)
        MultiVariableDeclarationNode(token, scope, declarations, expression(scope))
      }
      TokenType.RETURN -> {
        val expression = if (current.type == TokenType.SEMI_COLON) VoidExpression(token) else expression(scope)
        if (scope !is MethodScope) {
          throw MarcelParserException(previous, "Cannot have a return instruction outside of a function")
        }
        acceptOptional(TokenType.SEMI_COLON)
        ReturnNode(token, scope, expression)
      }
      TokenType.BRACKETS_OPEN -> {
        rollback()
        if (scope !is MethodScope) {
          throw MarcelParserException(current, "Cannot have blocks outside of a method")
        }
        // starting a new inner scope for the block
        BlockStatement(block(InnerScope(scope)))
      }
      TokenType.IF -> {
        accept(TokenType.LPAR)
        val condition = ifConditionExpression(token, scope)
          accept(TokenType.RPAR)
        val rootIf = IfStatementNode(condition, statement(scope), null)
        var currentIf = rootIf
        while (current.type == TokenType.ELSE) {
          skip()
          if (acceptOptional(TokenType.IF) != null) {
            accept(TokenType.LPAR)
            val elseIfCondition = ifConditionExpression(token, scope)
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
        val condition = BooleanExpressionNode.of(token, expression(scope))
        accept(TokenType.RPAR)
        val whileScope = InnerScope(scope as? MethodScope ?: throw MarcelParserException(previous,
          "Cannot have for outside of a method"
        )
        )
        WhileStatement(token, whileScope, condition, loopBody(whileScope))
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
          val loopScope = InnerScope(scope as? MethodScope ?: throw MarcelParserException(previous,
            "Cannot have for outside of a method"
          )
          )
          val forBlock = loopBody(loopScope)
          ForInStatement(token, loopScope, type, identifier, expression, forBlock)
        } else {
          // for (;;)
          // needed especially if initStatement is var declaration
          val forScope = InnerScope(scope as? MethodScope ?: throw MarcelParserException(previous,
            "Cannot have for outside of a method")
          )
          val initStatement = statement(forScope)
          if (initStatement !is VariableAssignmentNode) {
            throw MarcelParserException(previous, "For loops should start with variable declaration/assignment")
          }
          acceptOptional(TokenType.SEMI_COLON)
          val condition = BooleanExpressionNode.of(token, expression(forScope))
          accept(TokenType.SEMI_COLON)
          val iteratorStatement = statement(forScope)
          if (iteratorStatement !is VariableAssignmentNode && iteratorStatement !is ExpressionStatementNode) {
            throw MarcelParserException(previous, "Invalid for loop")
          }
          accept(TokenType.RPAR)
          val forBlock = loopBody(forScope)
          ForStatement(token, forScope, initStatement, condition, iteratorStatement, forBlock)
        }
      }
      TokenType.TRY -> {
        val tryNode = statement(scope)
        val catchNodes = mutableListOf<TryCatchNode.CatchBlock>()
        while (current.type == TokenType.CATCH) {
          skip()
          accept(TokenType.LPAR)
          val exceptions = mutableListOf(
            parseType(scope)
          )
          while (current.type == TokenType.PIPE) {
            skip()
            exceptions.add(parseType(scope))
          }
          val exceptionVarName = accept(TokenType.IDENTIFIER).value
          accept(TokenType.RPAR)
          val catchScope = InnerScope(
            scope as? MethodScope ?: throw MarcelParserException(previous,
              "Cannot have for outside of a method"
            )
          )
          catchNodes.add(TryCatchNode.CatchBlock(exceptions, exceptionVarName, catchScope, statement(catchScope)))
        }
        val finallyScope = InnerScope(
          scope as? MethodScope ?: throw MarcelParserException(previous,
            "Cannot have for outside of a method"
          )
        )

        val finallyBlock = if (acceptOptional(TokenType.FINALLY) != null) TryCatchNode.FinallyBlock(finallyScope,
          statement(finallyScope)) else null
        TryCatchNode(token, tryNode, catchNodes, finallyBlock)
      }
      TokenType.CONTINUE -> {
        if (scope !is InnerScope) {
          throw MarcelParserException(previous, "Cannot have a continue outside of an inner block")
        }
        acceptOptional(TokenType.SEMI_COLON)
        ContinueLoopNode(token, scope)
      }
      TokenType.BREAK -> {
        if (scope !is InnerScope) {
          throw MarcelParserException(previous, "Cannot have a continue outside of an inner block")
        }
        acceptOptional(TokenType.SEMI_COLON)
        BreakLoopNode(token, scope)
      }
      else -> {
        val isFinal = if (token.type == TokenType.FINAL) {
          token = next()
          true
        } else false
        if (isTypeToken(token.type) && current.type == TokenType.IDENTIFIER
            // generic type
            || isTypeToken(token.type) && current.type == TokenType.LT
          || isTypeToken(token.type) && current.type == TokenType.SQUARE_BRACKETS_OPEN && lookup( 1)?.type == TokenType.SQUARE_BRACKETS_CLOSE) {
          rollback()
          val type = parseType(scope)
          val variableName = accept(TokenType.IDENTIFIER).value
          val expression = if (acceptOptional(TokenType.ASSIGNMENT) != null) expression(scope)
          else null
          val v = VariableDeclarationNode(token, scope, type, variableName, isFinal, expression)
          acceptOptional(TokenType.SEMI_COLON)
          return v
        }
        rollback()
        val node = expression(scope)
        acceptOptional(TokenType.SEMI_COLON)
        ExpressionStatementNode(token, node)
      }
    }
  }

  private fun ifConditionExpression(token: LexToken, scope: Scope): BooleanExpressionNode {
    return BooleanExpressionNode.of(token,
      if (isTypeToken(current.type) && lookup(1)?.type == TokenType.IDENTIFIER) {
        val type = parseType(scope)
        val variableName = accept(TokenType.IDENTIFIER).value
        accept(TokenType.ASSIGNMENT)
        TruthyVariableDeclarationNode(token, scope, type, variableName, expression(scope))
      } else expression(scope)
    )
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
        return ElvisOperator(expr.token, scope, expr, onNullExpression)
      } else {
        val trueExpr = expression(scope)
        accept(TokenType.COLON)
        val falseExpr = expression(scope)
        return TernaryNode(expr.token, BooleanExpressionNode.of(expr.token, expr), trueExpr, falseExpr)
      }
    } else if (current.type == TokenType.AS) {
      skip()
      val type = parseType(scope)
      return AsNode(expr.token, type, expr)
    }
    return expr
  }
  private fun loopBody(scope: Scope): BlockNode {
    val loopStatement = statement(scope)
    if (loopStatement is BlockStatement) return loopStatement.block
    val newScope = InnerScope(scope as? MethodScope ?: throw MarcelParserException(previous,
      "Cannot have for outside of a method"
    )
    )
    return BlockNode(loopStatement.token, newScope, mutableListOf(loopStatement))
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
      a = operator(scope, t, leftOperand, rightOperand)
      t = current
    }
    return a
  }

  private fun atom(scope: Scope): ExpressionNode {
    val token = next()
    return when (token.type) {
      TokenType.INCR -> {
        val identifierToken = accept(TokenType.IDENTIFIER)
        IncrNode(token, ReferenceExpression(identifierToken, scope, identifierToken.value), 1, false)
      }
      TokenType.DECR -> {
        val identifierToken = accept(TokenType.IDENTIFIER)
        IncrNode(token, ReferenceExpression(identifierToken, scope, identifierToken.value), -1, false)
      }
      TokenType.INTEGER, TokenType.FLOAT -> {
       return if (current.type == TokenType.LT || current.type == TokenType.GT || current.type == TokenType.TWO_DOTS) {
         rangeNode(scope, parseNumberConstant(token))
        } else {
         parseNumberConstant(token)
        }
      }
      TokenType.THIS -> ThisReference(token, scope) // TODO parse thisConstructorCall
      TokenType.SUPER -> SuperReference(token, scope) // TODO parse SuperConstructorCall. Also handle super method calls.
      //                                            They are like "normal" method calls but needs to use the super class internal name when performing the invokeSPECIAL instruciton

      TokenType.BRACKETS_OPEN -> parseLambda(token, scope)
      TokenType.VALUE_TRUE -> BooleanConstantNode(token, true)
      TokenType.VALUE_FALSE -> BooleanConstantNode(token, false)
      TokenType.OPEN_CHAR_QUOTE -> {
        val valueToken = next()
        val value = when (valueToken.type) {
          TokenType.REGULAR_STRING_PART -> valueToken.value
          TokenType.ESCAPE_SEQUENCE -> escapedSequenceValue(valueToken.value)
          TokenType.END_OF_FILE -> throw MarcelParserException(token, "Unexpected end of file", true)
          else -> throw MarcelParserException(previous, "Unexpected token ${valueToken.type} for character constant")
        }
        accept(TokenType.CLOSING_CHAR_QUOTE)
        CharConstantNode(token, value)
      }
      TokenType.OPEN_QUOTE -> {
        val parts = mutableListOf<ExpressionNode>()
        while (current.type != TokenType.CLOSING_QUOTE) {
          parts.add(stringPart(scope))
        }
        skip() // skip last quote
        StringNode.of(token, parts)
      }
      TokenType.OPEN_SIMPLE_QUOTE -> {
        val builder = StringBuilder()
        while (current.type != TokenType.CLOSING_SIMPLE_QUOTE) {
          builder.append(simpleStringPart())
        }
        skip() // skip last quote
        StringConstantNode(token, builder.toString())
      }
      TokenType.OPEN_REGEX_QUOTE -> {
        val builder = StringBuilder()
        while (current.type != TokenType.CLOSING_REGEX_QUOTE) {
          builder.append(simpleStringPart())
        }
        skip() // skip last quote
        val flags = mutableListOf<Int>()
        val optFlags = acceptOptional(TokenType.IDENTIFIER)?.value
        if (optFlags != null) {
          for (char in optFlags) {
            LiteralPatternNode.FLAGS_MAP
            flags.add(LiteralPatternNode.FLAGS_MAP[char] ?: throw MarcelParserException(previous, "Unknown pattern flag $char"))
          }
        }
        LiteralPatternNode(token, builder.toString(), flags)
      }
      TokenType.ESCAPE_SEQUENCE -> StringConstantNode(token, escapedSequenceValue(token.value))
      TokenType.NULL -> NullValueNode()
      TokenType.NOT -> NotNode(token, expression(scope))
      TokenType.NEW -> {
        val type = parseType(scope)
        accept(TokenType.LPAR)
        val (arguments, namedArguments) = parseFunctionArguments(scope)
        if (arguments.isNotEmpty() && namedArguments.isNotEmpty()) {
          throw MarcelParserException(current, "Cannot have both positional and named arguments for constructor calls")
        }
        if (namedArguments.isNotEmpty()) NamedParametersConstructorCallNode(token, Scope(typeResolver, type), type, namedArguments)
        else ConstructorCallNode(token, Scope(typeResolver, type), type, arguments)
      }
      TokenType.SWITCH -> {
        accept(TokenType.LPAR)
        val switchExpression = expression(scope)
        accept(TokenType.RPAR)
        accept(TokenType.BRACKETS_OPEN)
        val branches = mutableListOf<SwitchBranchNode>()
        var elseStatement: StatementNode? = null
        val switchScope = InnerScope(scope as? MethodScope ?: throw MarcelParserException(previous,
          "Cannot have switch outside of method"
        )
        )
        while (current.type != TokenType.BRACKETS_CLOSE) {
          if (current.type == TokenType.ELSE) {
            skip()
            accept(TokenType.ARROW)

            if (elseStatement != null) throw MarcelParserException(previous, "Cannot have multiple else statements")
            elseStatement = statement(switchScope)
          } else {
            val valueExpression = expression(scope)
            accept(TokenType.ARROW)
            branches.add(SwitchBranchNode(token, switchScope, valueExpression, statement(switchScope)))
          }
        }
        skip() // skip bracket_close
        SwitchNode(token, scope, switchExpression, branches, elseStatement)
      }
      TokenType.WHEN -> {
        accept(TokenType.BRACKETS_OPEN)
        val branches = mutableListOf<WhenBranchNode>()
        val whenScope = InnerScope(scope as? MethodScope ?: throw MarcelParserException(previous,
          "Cannot have switch outside of method"
        )
        )
        var elseStatement: StatementNode? = null
        while (current.type != TokenType.BRACKETS_CLOSE) {
          if (current.type == TokenType.ELSE) {
            skip()
            accept(TokenType.ARROW)
            if (elseStatement != null) throw MarcelParserException(previous, "Cannot have multiple else statements")
            elseStatement = statement(whenScope)
          } else {
            val conditionExpression = expression(scope)
            accept(TokenType.ARROW)
            branches.add(WhenBranchNode(token, conditionExpression, statement(whenScope)))
          }
        }
        skip() // skip bracket_close
        WhenNode(token, scope, branches, elseStatement)
      }
      TokenType.IDENTIFIER -> {
        if (current.type == TokenType.INCR) {
          skip()
          IncrNode(token, ReferenceExpression(token, scope, token.value), 1, true)
        } else  if (current.type == TokenType.DECR) {
          skip()
          IncrNode(token, ReferenceExpression(token, scope, token.value), -1, true)
        }
        else if (current.type == TokenType.LPAR) {
          skip()
          val (arguments, namedArguments) = parseFunctionArguments(scope)
          if (namedArguments.isEmpty()) SimpleFunctionCallNode(token, scope, token.value, arguments)
          else NamedParametersFunctionCall(token, scope, token.value, arguments, namedArguments)
        } else if (current.type == TokenType.BRACKETS_OPEN) { // function call with a lambda
          skip()
          SimpleFunctionCallNode(token, scope, token.value, mutableListOf(parseLambda(previous, scope)))
        } else if (current.type == TokenType.LT  && lookup(1)?.type == TokenType.TWO_DOTS
          || current.type == TokenType.GT && lookup(1)?.type == TokenType.TWO_DOTS || current.type == TokenType.TWO_DOTS) {
          val fromExpression = if (current.type == TokenType.TWO_DOTS) ReferenceExpression(token, scope, token.value)
          else expression(scope)
          rangeNode(scope, fromExpression)
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
          IndexedReferenceExpression(token, scope, token.value, indexArguments, isSafeIndex)
        } else {
          ReferenceExpression(token, scope, token.value)
        }
      }
      TokenType.MINUS -> UnaryMinus(token, atom(scope))
      TokenType.PLUS -> UnaryPlus(token, atom(scope))
      TokenType.LPAR -> {
        val node = expression(scope)
        if (current.type != TokenType.RPAR) {
          throw MarcelParserException(previous, "Parenthesis should be close")
        }
        next()
        if (current.type == TokenType.LT  && lookup(1)?.type == TokenType.TWO_DOTS
          || current.type == TokenType.GT && lookup(1)?.type == TokenType.TWO_DOTS || current.type == TokenType.TWO_DOTS) {
          return rangeNode(scope, node)
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
          LiteralMapNode(token, entries)
        } else LiteralArrayNode(token, elements)
      }
      TokenType.END_OF_FILE -> throw MarcelParserException(token, "Unexpected end of file", true)
      else -> throw MarcelParserException(token, "Not supported $token")

    }
  }

  private fun escapedSequenceValue(tokenValue: String): String {
    return when (val escapedSequence = tokenValue.substring(1)) {
      "b" -> "\b"
      "n" -> "\n"
      "r" -> "\r"
      "t" -> "\t"
      "\\" -> "\\"
      "\'" -> "'"
      "\"" -> "\""
      "`" -> "`"
      "/" -> "/"
      else -> throw MarcelParserException(previous, "Unknown escaped sequence \\$escapedSequence")
    }
  }
  private fun parseLambda(token: LexToken, scope: Scope): LambdaNode {
    val parameters = mutableListOf<MethodParameter>()
    var explicit0Parameters = false
    if (lookup(2)?.type == TokenType.ARROW || lookup(3)?.type == TokenType.ARROW
        || lookup(2)?.type == TokenType.COMMA || lookup(3)?.type == TokenType.COMMA) {
      explicit0Parameters = true
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
    return LambdaNode(token, LambdaScope(scope), parameters, block, explicit0Parameters)
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
          throw MarcelParserException.malformedNumber(token, e)
        }
        LongConstantNode(token, value)
      } else {
        val value = try {
          valueString.toInt(radix)
        } catch (e: NumberFormatException) {
          throw MarcelParserException.malformedNumber(token, e)
        }
        IntConstantNode(token, value)
      }
    } else if (token.type == TokenType.FLOAT) {
      var valueString = token.value.lowercase(Locale.ENGLISH)
      val isDouble = valueString.endsWith("d")
      if (isDouble) valueString = valueString.substring(0, valueString.length - 1)

      return if (isDouble) {
        val value = try {
          valueString.toDouble()
        } catch (e: NumberFormatException) {
          throw MarcelParserException.malformedNumber(token, e)
        }
        DoubleConstantNode(token, value)
      } else {
        val value = try {
          valueString.toFloat()
        } catch (e: NumberFormatException) {
          throw MarcelParserException.malformedNumber(token, e)
        }
        FloatConstantNode(token, value)
      }
    } else {
      throw MarcelParserException(token, "Unexpected token $token", token.type == TokenType.END_OF_FILE)
    }
  }
  private fun rangeNode(scope: Scope, fromExpression: ExpressionNode): RangeNode {
    val fromExclusive = acceptOptional(TokenType.LT, TokenType.GT) != null
    accept(TokenType.TWO_DOTS)
    val toExclusive = acceptOptional(TokenType.LT, TokenType.GT) != null
    val toExpression = expression(scope)
    return RangeNode(fromExpression.token, fromExpression, toExpression, fromExclusive, toExclusive)
  }

  private fun stringPart(scope: Scope): ExpressionNode {
    val token = next()
    return when (token.type) {
      TokenType.REGULAR_STRING_PART -> StringConstantNode(token, token.value)
      TokenType.SHORT_TEMPLATE_ENTRY_START -> {
        val identifierToken = accept(TokenType.IDENTIFIER)
        ReferenceExpression(identifierToken, scope, identifierToken.value)
      }
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
  private fun simpleStringPart(): String {
    val token = next()
    return when (token.type) {
      TokenType.REGULAR_STRING_PART -> token.value
      TokenType.ESCAPE_SEQUENCE -> escapedSequenceValue(token.value)
      TokenType.END_OF_FILE -> throw MarcelParserException(token, "Unexpected end of file", true)
      else -> throw MarcelParserException(token, "Illegal token ${token.type} when parsing literal string")
    }
  }

  // assuming we already passed LPAR
  private fun parseFunctionArguments(scope: Scope): Pair<MutableList<ExpressionNode>, MutableList<NamedArgument>> {
    val arguments = mutableListOf<ExpressionNode>()
    val namedArguments = mutableListOf<NamedArgument>()
    while (current.type != TokenType.RPAR) {
      if (current.type == TokenType.IDENTIFIER && lookup(1)?.type == TokenType.COLON) {
        val identifierToken = accept(TokenType.IDENTIFIER)
        val name = identifierToken.value
        if (namedArguments.any { it.name == name }) {
          throw MarcelParserException(identifierToken, "Method parameter $name was specified more than one")
        }
        accept(TokenType.COLON)
        namedArguments.add(NamedArgument(name, expression(scope)))
      } else {
        if (namedArguments.isNotEmpty()) {
          throw MarcelParserException(current, "Cannot have a positional function argument after a named one")
        }
        arguments.add(expression(scope))
      }

      if (current.type == TokenType.COMMA) {
        accept(TokenType.COMMA)
      }
    }
    skip() // skipping RPAR
    return Pair(arguments, namedArguments)
  }

  private fun operator(scope: Scope, token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode): ExpressionNode {
    val t = token.type
    return when(t) {
      TokenType.ASSIGNMENT, TokenType.PLUS_ASSIGNMENT, TokenType.MINUS_ASSIGNMENT, TokenType.MUL_ASSIGNMENT, TokenType.DIV_ASSIGNMENT -> {
        if (t == TokenType.ASSIGNMENT) {
          when (leftOperand) {
            is ReferenceExpression -> VariableAssignmentNode(token, scope, leftOperand.name, rightOperand)
            is IndexedReferenceExpression -> IndexedVariableAssignmentNode(token, scope, leftOperand, rightOperand)
            is GetFieldAccessOperator -> FieldAssignmentNode(token, scope, leftOperand, rightOperand)
            else -> throw MarcelParserException(token, "Cannot assign to $leftOperand")
          }
        } else {
          val actualRightOperand = when(t) {
            TokenType.PLUS_ASSIGNMENT -> PlusOperator(token, leftOperand, rightOperand)
            TokenType.MINUS_ASSIGNMENT -> MinusOperator(token, leftOperand, rightOperand)
            TokenType.DIV_ASSIGNMENT -> DivOperator(token, leftOperand, rightOperand)
            TokenType.MUL_ASSIGNMENT -> MulOperator(token, leftOperand, rightOperand)
            else -> throw RuntimeException("Compiler error")
          }
          when (leftOperand) {
            is ReferenceExpression -> VariableAssignmentNode(token, scope, leftOperand.name, actualRightOperand)
            is IndexedReferenceExpression -> IndexedVariableAssignmentNode(token, scope, leftOperand, actualRightOperand)
            is GetFieldAccessOperator -> FieldAssignmentNode(token, scope, leftOperand, actualRightOperand)
            else -> throw MarcelParserException(token, "Cannot assign to $leftOperand")
          }
        }
      }
      TokenType.FIND -> FindOperator(token, leftOperand, rightOperand)
      TokenType.IS -> IsOperator(token, leftOperand, rightOperand)
      TokenType.IS_NOT -> IsNotOperator(token, leftOperand, rightOperand)
      TokenType.MUL -> MulOperator(token, leftOperand, rightOperand)
      TokenType.DIV -> DivOperator(token, leftOperand, rightOperand)
      TokenType.PLUS -> PlusOperator(token, leftOperand, rightOperand)
      TokenType.MINUS -> MinusOperator(token, leftOperand, rightOperand)
      TokenType.AND -> AndOperator(token, leftOperand, rightOperand)
      TokenType.OR -> OrOperator(token, leftOperand, rightOperand)
      TokenType.LEFT_SHIFT -> LeftShiftOperator(token, leftOperand, rightOperand)
      TokenType.RIGHT_SHIFT -> RightShiftOperator(token, leftOperand, rightOperand)
      TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LT, TokenType.GT, TokenType.LOE, TokenType.GOE -> ComparisonOperatorNode(token, ComparisonOperator.fromTokenType(token), leftOperand, rightOperand)
      TokenType.DOT -> when (rightOperand) {
        is FunctionCallNode -> InvokeAccessOperator(token, leftOperand, rightOperand, false)
        is ReferenceExpression -> GetFieldAccessOperator(token, leftOperand, rightOperand, false)
        else -> throw MarcelParserException(token, "Can only handle function calls and fields with dot operators")
      }
      TokenType.QUESTION_DOT -> when (rightOperand) {
        is FunctionCallNode -> InvokeAccessOperator(token, leftOperand, rightOperand, true)
        is ReferenceExpression -> GetFieldAccessOperator(token, leftOperand, rightOperand, true)
        else -> throw MarcelParserException(token, "Can only handle function calls and fields with dot operators")
      }
      TokenType.END_OF_FILE -> throw MarcelParserException(token, "Unexpected end of file", true)
      else -> throw MarcelParserException(token, "Doesn't handle operator with token type $t")
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
      throw MarcelParserException(current, "Expected token of type $t but got ${token.type}",
        current.type == TokenType.END_OF_FILE)
    }
    currentIndex++
    return token
  }

  private fun accept(vararg types: TokenType): LexToken {
    val token = current
    if (token.type !in types) {
      throw MarcelParserException(current, "Expected token of type ${types.contentToString()} but got ${token.type}")
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
      throw MarcelParserException(currentSafe ?: previous, "Unexpected end of file", true)
    }
  }
}