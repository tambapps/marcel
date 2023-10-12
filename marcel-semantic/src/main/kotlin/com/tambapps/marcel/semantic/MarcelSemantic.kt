package com.tambapps.marcel.semantic

import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.SuperConstructorCallCstNode
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
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.MethodParameterNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
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

  private val caster = AstNodeCaster(typeResolver)

  val exprVisitor = this as ExpressionCstNodeVisitor<ExpressionNode>
  val stmtVisitor = this as StatementCstNodeVisitor<StatementNode>

  internal val scopeQueue = LinkedList<Scope>()
  // FIFO
  private val currentScope get() = scopeQueue.peek()
  fun apply(): ModuleNode {
    val imports = Scope.DEFAULT_IMPORTS.toMutableList()
    // TODO parse package if any

    // TODO check return type of all functions, as the compiler should not check anything

    val className = cst.fileName
    if (cst.statements.isNotEmpty()) {
      return scriptModule(className, imports)
    } else {
      TODO()
    }
  }

  private fun scriptModule(className: String, imports: List<ImportNode>): ModuleNode {
    val classType = typeResolver.defineClass(cst.statements.first().tokenStart, Visibility.PUBLIC, className, Script::class.javaType, false, emptyList())
    val classScope = ClassScope(classType, typeResolver, imports)
    scopeQueue.push(classScope)
    val classNode = ClassNode(classType, Visibility.PUBLIC, cst.tokenStart, cst.tokenEnd)
    val runMethod =
      MethodNode(name = "run",
        visibility = Visibility.PUBLIC, returnType = JavaType.Object,
        isStatic = false,
        ownerClass = classType,
        tokenStart = cst.statements.first().tokenStart, tokenEnd = cst.statements.last().tokenEnd)
    runMethod.parameters.add(MethodParameterNode(classNode.token, JavaType.String.arrayType, "args"))
    fillClassNode(classNode, classScope, cst.methods)
    fillMethodNode(classScope, runMethod, cst.statements)
    classNode.methods.add(runMethod)

    val moduleNode = ModuleNode(cst.tokenStart, cst.tokenEnd)
    moduleNode.classes.add(classNode)
    return moduleNode
  }

  private fun fillClassNode(classNode: ClassNode, classScope: ClassScope, methods: List<MethodCstNode>) {
    for (methodCst in methods) {
      val methodNode = MethodNode(
        name = methodCst.name,
        visibility = Visibility.fromTokenType(methodCst.accessNode.visibility),
        returnType = type(methodCst.returnTypeCstNode),
        isStatic = methodCst.accessNode.isStatic,
        tokenStart = methodCst.tokenStart,
        tokenEnd = methodCst.tokenEnd,
        ownerClass = classNode.type
      )
      fillMethodNode(classScope, methodNode, methodCst.statements)
    }

    if (classNode.constructorCount == 0) {
      // add a default constructor
      val defaultConstructorNode = MethodNode(JavaMethod.CONSTRUCTOR_NAME, Visibility.PUBLIC, JavaType.void, false, classNode.tokenStart, classNode.tokenEnd, JavaType.void)
      val superConstructorMethod = typeResolver.findMethodOrThrow(classNode.superType, JavaMethod.CONSTRUCTOR_NAME, emptyList(), classNode)
      defaultConstructorNode.blockStatement = BlockStatementNode(listOf(
        ExpressionStatementNode(SuperConstructorCallNode(classNode.superType, superConstructorMethod, emptyList(), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)),
        ReturnStatementNode(VoidExpressionNode(defaultConstructorNode.token), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)
      ), defaultConstructorNode.tokenStart, defaultConstructorNode.tokenEnd)
      classNode.methods.add(defaultConstructorNode)
    }
  }

  private fun fillMethodNode(classScope: ClassScope, methodeNode: MethodNode, cstStatements: List<StatementCstNode>) =     useScope(MethodScope(classScope, methodeNode)) {
    val statements = mutableListOf<StatementNode>()
    for (i in cstStatements.indices) {
      val statement = cstStatements[i].accept(stmtVisitor)
      // TODO add this check in all block/list of statements
      if (statement is ReturnStatementNode && i < cstStatements.lastIndex)
        throw MarcelSemanticException("Cannot have statements after a return statement")
      statements.add(statement)
    }

    if (methodeNode.returnType != JavaType.void && statements.lastOrNull() !is ReturnStatementNode) {
      // TODO ALL FUNCTOINS SHOULD RETURN, EVEN VOID
      statements.add(ReturnStatementNode(NullValueNode(methodeNode.token), methodeNode.tokenStart, methodeNode.tokenEnd))
    }
    // TODO check all path returns if returnType is not void
    methodeNode.blockStatement = BlockStatementNode(statements, methodeNode.tokenStart, methodeNode.tokenEnd)
  }


  private inline fun useScope(scope: Scope, consumer: () -> Unit) {
    scopeQueue.push(scope)
    consumer.invoke()
    scopeQueue.pop()
  }

  private fun type(node: TypeCstNode): JavaType = currentScope.resolveTypeOrThrow(node)

  /*
   * node visits
   */
  override fun visit(node: DoubleCstNode) = DoubleConstantNode(node.token, node.value)

  override fun visit(node: FloatCstNode) = FloatConstantNode(node.token, node.value)

  override fun visit(node: IntCstNode) = IntConstantNode(node.token, node.value)

  override fun visit(node: LongCstNode) = LongConstantNode(node.token, node.value)
  override fun visit(node: NullCstNode) = NullValueNode(node.token)

  override fun visit(node: ClassReferenceCstNode) = ClassReferenceNode(node.value, node.token)
  override fun visit(node: ThisReferenceCstNode) = ThisReferenceNode(currentScope.classType, node.token)
  override fun visit(node: SuperReferenceCstNode) = SuperReferenceNode(currentScope.classType.superType!!, node.token)

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
    val owner = if (method.isStatic) null else ThisReferenceNode(currentScope.classType, node.token)
    return FunctionCallNode(method, owner, castType,
      arguments.mapIndexed { index, expressionNode -> caster.cast(method.parameters[index].type, expressionNode) }
      , node.token)
  }

  override fun visit(node: SuperConstructorCallCstNode): ExpressionNode {
    val arguments = node.arguments.map { it.accept(exprVisitor) }
    val method = currentScope.findMethodOrThrow(JavaMethod.CONSTRUCTOR_NAME, arguments, node)
    return FunctionCallNode(method, SuperReferenceNode(currentScope.classType.superType!!, node.token), null, arguments, node.token)
  }

  override fun visit(node: ExpressionStatementCstNode) = ExpressionStatementNode(node.expressionNode.accept(exprVisitor), node.tokenStart, node.tokenEnd)
  override fun visit(node: ReturnCstNode): StatementNode {
    // TODO test error cases of this
    val scope = currentScope as? MethodScope ?: throw MarcelSemanticException("Cannot return outside of a function")
    val expression = node.expressionNode?.accept(exprVisitor)?.let { caster.cast(scope.method.returnType, it) }
    if (expression != null && expression.type != JavaType.void && scope.method.returnType == JavaType.void) {
      throw MarcelSemanticException(node, "Cannot return expression in void function")
    } else if (expression == null && scope.method.returnType != JavaType.void) {
      throw MarcelSemanticException(node, "Must return expression in non void function")
    }
    return ReturnStatementNode(node.expressionNode?.accept(exprVisitor), node.tokenStart, node.tokenEnd)
  }

}