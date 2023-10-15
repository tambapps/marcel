package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.NewInstanceCstNode
import com.tambapps.marcel.parser.cst.expression.SuperConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.SuperReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ThisReferenceCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor
import com.tambapps.marcel.parser.cst.expression.literal.ArrayCstNode
import com.tambapps.marcel.parser.cst.expression.literal.MapCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.operator.BinaryOperatorNode
import com.tambapps.marcel.semantic.ast.expression.operator.DivNode
import com.tambapps.marcel.semantic.ast.expression.operator.MinusNode
import com.tambapps.marcel.semantic.ast.expression.operator.ModNode
import com.tambapps.marcel.semantic.ast.expression.operator.MulNode
import com.tambapps.marcel.semantic.ast.expression.operator.PlusNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.scope.MethodScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.variable.Variable
import com.tambapps.marcel.semantic.visitor.AllPathsReturnVisitor
import marcel.lang.Script
import java.util.LinkedList

// TODO implement multiple errors like in parser2
class MarcelSemantic(
  private val typeResolver: JavaTypeResolver,
  private val cst: SourceFileCstNode
): ExpressionCstNodeVisitor<ExpressionNode>, StatementCstNodeVisitor<StatementNode> {

  private val caster = AstNodeCaster(typeResolver)

  val exprVisitor = this as ExpressionCstNodeVisitor<ExpressionNode>
  val stmtVisitor = this as StatementCstNodeVisitor<StatementNode>

  internal val scopeQueue = LinkedList<Scope>()
  // FIFO
  private val currentScope get() = scopeQueue.peek()
  private val currentMethodScope get() = currentScope as? MethodScope ?: throw MarcelSemanticException("Not in a method")

  fun apply(): ModuleNode {
    val imports = Scope.DEFAULT_IMPORTS.toMutableList()
    // TODO parse package if any

    val className = cst.fileName
    if (cst.statements.isNotEmpty()) {
      return scriptModule(className, imports)
    } else {
      TODO()
    }
  }

  private fun scriptModule(className: String, imports: List<ImportNode>): ModuleNode {
    val classType = typeResolver.defineClass(cst.statements.first().tokenStart, Visibility.PUBLIC, className, Script::class.javaType, false, emptyList())
    val moduleNode = ModuleNode(cst.tokenStart, cst.tokenEnd)
    moduleNode.classes.add(classNode(classType, cst.methods, imports))
    return moduleNode
  }

  private fun classNode(classType: JavaType, methods: List<MethodCstNode>, imports: List<ImportNode>): ClassNode
  = useScope(ClassScope(classType, typeResolver, imports)) { classScope ->
    val classNode = ClassNode(classType, Visibility.PUBLIC, cst.tokenStart, cst.tokenEnd)

    val runMethod = SemanticHelper.scriptRunMethod(classType, cst)
    fillMethodNode(classScope, runMethod, cst.statements, scriptRunMethod = true)
    classNode.methods.add(runMethod)

    methods.forEach { classNode.methods.add(methodNode(it, classScope)) }

    if (classNode.constructorCount == 0) {
      // default no arg constructor
      classNode.methods.add(SemanticHelper.noArgConstructor(classNode, typeResolver))
    }
    return classNode
  }

  private fun methodNode(methodCst: MethodCstNode, classScope: ClassScope): MethodNode {
    val methodNode = MethodNode(
      name = methodCst.name,
      visibility = Visibility.fromTokenType(methodCst.accessNode.visibility),
      returnType = visit(methodCst.returnTypeCstNode),
      isStatic = methodCst.accessNode.isStatic,
      tokenStart = methodCst.tokenStart,
      tokenEnd = methodCst.tokenEnd,
      ownerClass = classScope.classType
    )
    fillMethodNode(classScope, methodNode, methodCst.statements)
    return methodNode
  }

  private fun fillMethodNode(classScope: ClassScope, methodeNode: MethodNode, cstStatements: List<StatementCstNode>,
                             scriptRunMethod: Boolean = false): Unit
  = useScope(MethodScope(classScope, methodeNode)) {
    val statements = mutableListOf<StatementNode>()
    for (i in cstStatements.indices) {
      val statement = cstStatements[i].accept(stmtVisitor)
      // TODO add this check in all block/list of statements
      if (statement is ReturnStatementNode && i < cstStatements.lastIndex)
        throw MarcelSemanticException("Cannot have statements after a return statement")
      statements.add(statement)
    }

    if (!AllPathsReturnVisitor.test(statements)) {
      if (methodeNode.returnType == JavaType.void) {
        statements.add(SemanticHelper.returnVoid(methodeNode))
      } else if (scriptRunMethod) {
        statements.add(SemanticHelper.returnNull(methodeNode))
      } else {
        throw MarcelSemanticException(methodeNode.token, "Not all paths return a value")
      }
    }
    methodeNode.blockStatement = BlockStatementNode(statements, methodeNode.tokenStart, methodeNode.tokenEnd)
  }


  private inline fun <T: Scope, U> useScope(scope: T, consumer: (T) -> U): U {
    scopeQueue.push(scope)
    val u = consumer.invoke(scope)
    scopeQueue.pop()
    return u
  }

  private fun visit(node: TypeCstNode): JavaType = currentScope.resolveTypeOrThrow(node)

  /*
   * node visits
   */
  override fun visit(node: DoubleCstNode) = DoubleConstantNode(node.token, node.value)

  override fun visit(node: FloatCstNode) = FloatConstantNode(node.token, node.value)

  override fun visit(node: IntCstNode) = IntConstantNode(node.token, node.value)

  override fun visit(node: LongCstNode) = LongConstantNode(node.token, node.value)
  override fun visit(node: NullCstNode) = NullValueNode(node.token)
  override fun visit(node: StringCstNode) = StringConstantNode(node.value, node)

  override fun visit(node: TemplateStringNode): ExpressionNode {
    val expressions = node.expressions.map { it.accept(exprVisitor) }
    return if (expressions.isEmpty()) StringConstantNode("", node)
    else if (expressions.size == 1 && expressions.first() is StringConstantNode) expressions.first()
    else StringNode(expressions, node)
  }

  override fun visit(node: ClassReferenceCstNode) = ClassReferenceNode(node.value, node.token)
  override fun visit(node: ThisReferenceCstNode) = ThisReferenceNode(currentScope.classType, node.token)
  override fun visit(node: SuperReferenceCstNode) = SuperReferenceNode(currentScope.classType.superType!!, node.token)

  override fun visit(node: NewInstanceCstNode): ExpressionNode {
    val type = visit(node.type)
    if (node.namedArgumentNodes.isNotEmpty()) TODO()
    val arguments = node.positionalArgumentNodes.map { it.accept(exprVisitor) }
    val constructorMethod = typeResolver.findMethodOrThrow(type, JavaMethod.CONSTRUCTOR_NAME, arguments, node.token)
    return NewInstanceNode(type, constructorMethod, castedArguments(constructorMethod, arguments), node.token)
  }

  override fun visit(node: DirectFieldReferenceCstNode): ExpressionNode {
    TODO("Not yet implemented")
  }

  override fun visit(node: ArrayCstNode) = ArrayNode(
    elements = node.elements.map { it.accept(exprVisitor) }.toMutableList(),
    node = node
  )

  override fun visit(node: MapCstNode) = MapNode(
    entries = node.entries.map { Pair(
      // need objects (not primitive) to call function Map.put(key, value)
      caster.cast(JavaType.Object, it.first.accept(exprVisitor)),
      caster.cast(JavaType.Object, it.second.accept(exprVisitor))) },
    node = node
  )

  override fun visit(node: IncrCstNode): ExpressionNode {
    TODO("Not yet implemented")
  }

  override fun visit(node: IndexAccessCstNode): ExpressionNode {
    TODO("Not yet implemented")
  }

  override fun visit(node: BinaryOperatorCstNode): ExpressionNode {
    val owner = node.leftOperand.accept(exprVisitor)
    val leftOperand = node.leftOperand
    val rightOperand = node.rightOperand
    return when (node.tokenType) {
      TokenType.ASSIGNMENT -> when (leftOperand) {
        is ReferenceCstNode -> {
          val variable = currentScope.findVariableOrThrow(leftOperand.value, leftOperand.token)
          checkVariableAccess(variable, node, checkSet = true)
          VariableAssignmentNode(variable,
            caster.cast(variable.type, rightOperand.accept(exprVisitor)), node.tokenStart, node.tokenEnd)
        }
        else -> throw MarcelSemanticException(node, "Invalid assignment operator use")
      }
      // TODO handle + strings
      TokenType.PLUS -> arithmeticBinaryOperator(leftOperand, rightOperand, "plus", ::PlusNode)
      TokenType.MINUS -> arithmeticBinaryOperator(leftOperand, rightOperand, "minus", ::MinusNode)
      TokenType.MUL -> arithmeticBinaryOperator(leftOperand, rightOperand, "multiply", ::MulNode)
      TokenType.DIV -> arithmeticBinaryOperator(leftOperand, rightOperand, "div", ::DivNode)
      TokenType.MODULO -> arithmeticBinaryOperator(leftOperand, rightOperand, "mod", ::ModNode)
      TokenType.DOT -> when (rightOperand) {
        is FunctionCallCstNode -> {
          val arguments = getArguments(rightOperand)
          val method = typeResolver.findMethodOrThrow(owner.type, rightOperand.value, arguments, node.token)
          val castType = if (rightOperand.castType != null) visit(rightOperand.castType!!) else null
          FunctionCallNode(method, owner, castType,
            castedArguments(method, arguments)
            , node.token)
        }
        is ReferenceCstNode -> {
          val variable = typeResolver.findFieldOrThrow(owner.type, rightOperand.value, rightOperand.token)
          checkVariableAccess(variable, node)
          ReferenceNode(owner, variable, rightOperand.token)
        }
        else -> throw MarcelSemanticException(node, "Invalid dot operator use")
      }
      else -> throw MarcelSemanticException(node, "Doesn't handle operator ${node.tokenType}")
    }
  }

  private inline fun arithmeticBinaryOperator(leftOperand: CstExpressionNode, rightOperand: CstExpressionNode,
                                       operatorMethodName: String,
                                       nodeSupplier: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode): ExpressionNode {
    val left = leftOperand.accept(exprVisitor)
    val right = rightOperand.accept(exprVisitor)
    val commonType = JavaType.commonType(left, right)
    return if (commonType.isPrimitiveOrObjectPrimitive) {
      val commonPrimitiveType = commonType.asPrimitiveType
      if (!commonPrimitiveType.isNumber) throw MarcelSemanticException("Cannot apply operator on non number types")
      nodeSupplier.invoke(caster.cast(commonPrimitiveType, left), caster.cast(commonPrimitiveType, right))
    } else {
      val arguments = listOf(right)
      val method = typeResolver.findMethodOrThrow(left.type, operatorMethodName, arguments, left.token)
      FunctionCallNode(method, left, null,
        castedArguments(method, arguments)
        , left.token)
    }
  }

  override fun visit(node: ReferenceCstNode): ExpressionNode {
    val variable = currentScope.findVariableOrThrow(node.value, node.token)
    checkVariableAccess(variable, node, checkGet = true)
    return ReferenceNode(null, variable, node.token)
  }

  override fun visit(node: FunctionCallCstNode): ExpressionNode {
    val arguments = getArguments(node)
    val method = currentScope.findMethodOrThrow(node.value, arguments, node)
    val castType = if (node.castType != null) visit(node.castType!!) else null
    val owner = if (method.isStatic) null else ThisReferenceNode(currentScope.classType, node.token)
    return FunctionCallNode(method, owner, castType,
      castedArguments(method, arguments)
      , node.token)
  }

  private fun getArguments(node: FunctionCallCstNode): List<ExpressionNode> {
    if (node.namedArgumentNodes.isNotEmpty()) TODO("Doesn't handle named arguments yet")
    return node.positionalArgumentNodes.map { it.accept(exprVisitor) }
  }

  private fun castedArguments(method: JavaMethod, arguments: List<ExpressionNode>) =
    arguments.mapIndexed { index, expressionNode -> caster.cast(method.parameters[index].type, expressionNode) }

  override fun visit(node: SuperConstructorCallCstNode): ExpressionNode {
    val arguments = node.arguments.map { it.accept(exprVisitor) }
    val method = currentScope.findMethodOrThrow(JavaMethod.CONSTRUCTOR_NAME, arguments, node)
    return FunctionCallNode(method, SuperReferenceNode(currentScope.classType.superType!!, node.token), null, arguments, node.token)
  }

  override fun visit(node: ExpressionStatementCstNode) = ExpressionStatementNode(node.expressionNode.accept(exprVisitor), node.tokenStart, node.tokenEnd)
  override fun visit(node: ReturnCstNode): StatementNode {
    // TODO test error cases of this
    val scope = currentMethodScope
    val expression = node.expressionNode?.accept(exprVisitor)?.let { caster.cast(scope.method.returnType, it) }
    if (expression != null && expression.type != JavaType.void && scope.method.returnType == JavaType.void) {
      throw MarcelSemanticException(node, "Cannot return expression in void function")
    } else if (expression == null && scope.method.returnType != JavaType.void) {
      throw MarcelSemanticException(node, "Must return expression in non void function")
    }
    return ReturnStatementNode(node.expressionNode?.accept(exprVisitor), node.tokenStart, node.tokenEnd)
  }

  override fun visit(node: VariableDeclarationCstNode): StatementNode {
    val variable = currentMethodScope.addLocalVariable(visit(node.type), node.value)
    checkVariableAccess(variable, node, checkSet = true)
    return ExpressionStatementNode(
      VariableAssignmentNode(variable,
        node.expressionNode?.accept(exprVisitor)?.let { caster.cast(variable.type, it) }
          ?: variable.type.getDefaultValueExpression(node.token), node.tokenStart, node.tokenEnd)
    )
  }

  private fun checkVariableAccess(variable: Variable, node: CstNode, checkGet: Boolean = false, checkSet: Boolean = false) {
    if (!variable.isAccessibleFrom(currentScope.classType)) {
      throw MarcelSemanticException(node, "Cannot access variable ${variable.name} from ${currentScope.classType}")
    }
    if (checkGet && !variable.isGettable) {
      throw MarcelSemanticException(node, "Cannot get value of variable ${variable.name}")
    }
    if (checkSet && !variable.isSettable) {
      throw MarcelSemanticException(node, "Cannot set value for variable ${variable.name}")
    }
  }
}