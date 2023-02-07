package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.expression.AndOperator
import com.tambapps.marcel.parser.ast.expression.AsNode
import com.tambapps.marcel.parser.ast.expression.BinaryOperatorNode
import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.BooleanConstantNode
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.ComparisonOperatorNode
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.DivOperator
import com.tambapps.marcel.parser.ast.expression.DoubleConstantNode
import com.tambapps.marcel.parser.ast.expression.ElvisOperator
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.FloatConstantNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.GetFieldAccessOperator
import com.tambapps.marcel.parser.ast.expression.IncrNode
import com.tambapps.marcel.parser.ast.expression.IndexedReferenceExpression
import com.tambapps.marcel.parser.ast.expression.IndexedVariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.InvokeAccessOperator
import com.tambapps.marcel.parser.ast.expression.LambdaNode
import com.tambapps.marcel.parser.ast.expression.LeftShiftOperator
import com.tambapps.marcel.parser.ast.expression.LiteralArrayNode
import com.tambapps.marcel.parser.ast.expression.LiteralMapNode
import com.tambapps.marcel.parser.ast.expression.LongConstantNode
import com.tambapps.marcel.parser.ast.expression.MinusOperator
import com.tambapps.marcel.parser.ast.expression.MulOperator
import com.tambapps.marcel.parser.ast.expression.NotNode
import com.tambapps.marcel.parser.ast.expression.NullSafeGetFieldAccessOperator
import com.tambapps.marcel.parser.ast.expression.NullSafeInvokeAccessOperator
import com.tambapps.marcel.parser.ast.expression.NullValueNode
import com.tambapps.marcel.parser.ast.expression.OrOperator
import com.tambapps.marcel.parser.ast.expression.PlusOperator
import com.tambapps.marcel.parser.ast.expression.PowOperator
import com.tambapps.marcel.parser.ast.expression.RangeNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.ReturnNode
import com.tambapps.marcel.parser.ast.expression.RightShiftOperator
import com.tambapps.marcel.parser.ast.expression.StringConstantNode
import com.tambapps.marcel.parser.ast.expression.StringNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.TernaryNode
import com.tambapps.marcel.parser.ast.expression.ToStringNode
import com.tambapps.marcel.parser.ast.expression.TruthyVariableDeclarationNode
import com.tambapps.marcel.parser.ast.expression.UnaryMinus
import com.tambapps.marcel.parser.ast.expression.UnaryOperator
import com.tambapps.marcel.parser.ast.expression.UnaryPlus
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression
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
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.NotLoadedJavaType
import marcel.lang.IntRange
import marcel.lang.lambda.Lambda1

open class AstNodeTypeResolver: AstNodeVisitor<JavaType> {

  private val definedTypes = mutableMapOf<String, JavaType>()

  fun defineClass(className: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>): JavaType {
    return defineClass(null, className, superClass, isInterface, interfaces)
  }
  fun defineClass(outerClassType: JavaType?, cName: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>): JavaType {
    val className = if (outerClassType != null) "${outerClassType.className}\$$cName" else cName
    try {
      Class.forName(className)
      throw SemanticException("Class $className is already defined")
    } catch (e: ClassNotFoundException) {
      // ignore
    }
    if (definedTypes.containsKey(className)) throw SemanticException("Class $className is already defined")
    // TODO only handle interfaces that are already loaded, as the type is not lazily loaded
    val type = NotLoadedJavaType(className, emptyList(), superClass, isInterface, interfaces)
    definedTypes[className] = type
    return type
  }

  fun isDefined(className: String): Boolean {
    return try {
      of(className, emptyList())
      true
    } catch (e: SemanticException) {
      false
    }
  }

  fun clear() {
    definedTypes.clear()
  }

  open fun getDeclaredMethods(interfaze: JavaType): List<JavaMethod> {
    return emptyList()
  }

    open fun of(className: String, genericTypes: List<JavaType>): JavaType {
    return definedTypes[className] ?: JavaType.of(className, genericTypes)
  }

  fun findMethodOrThrow(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>): JavaMethod {
    return findMethod(javaType, name, argumentTypes) ?: throw SemanticException("Method $javaType.$name with parameters ${argumentTypes.map { it.type }} is not defined")
  }

  open fun findMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean = false): JavaMethod? {
    return null
  }

  fun findFieldOrThrow(javaType: JavaType, name: String, declared: Boolean = true): MarcelField {
    return findField(javaType, name, declared) ?: throw SemanticException("Field $name was not found")
  }

  open fun findField(javaType: JavaType, name: String, declared: Boolean): MarcelField? {
    return null
  }

  fun resolve(node: ExpressionNode) = node.accept(this)
  fun resolve(node: StatementNode) = node.accept(this)


  override fun visit(integer: IntConstantNode) = JavaType.int

  override fun visit(longConstantNode: LongConstantNode) = JavaType.long

  override fun visit(floatConstantNode: FloatConstantNode) = JavaType.float
  override fun visit(doubleConstantNode: DoubleConstantNode) = JavaType.double

  override fun visit(booleanConstantNode: BooleanConstantNode) = JavaType.boolean

  override fun visit(stringNode: StringNode) = JavaType.String

  override fun visit(stringConstantNode: StringConstantNode) = JavaType.String

  override fun visit(toStringNode: ToStringNode) = JavaType.String

  override fun visit(operator: MulOperator) = visitBinaryOperator(operator)

  private fun visitBinaryOperator(binaryOperatorNode: BinaryOperatorNode) =
    JavaType.commonType(binaryOperatorNode.leftOperand.accept(this), binaryOperatorNode.rightOperand.accept(this))

  override fun visit(operator: TernaryNode): JavaType {
    if (operator is NullSafeInvokeAccessOperator || operator is NullSafeGetFieldAccessOperator) {
      (operator.falseExpression as NullValueNode).type = operator.trueExpression.accept(this)
    }
    return JavaType.commonType(operator.trueExpression.accept(this), operator.falseExpression.accept(this))
  }

  override fun visit(elvisOperator: ElvisOperator) = visitBinaryOperator(elvisOperator)


  override fun visit(fCall: ConstructorCallNode) = fCall.type

  override fun visit(fCall: SuperConstructorCallNode) = JavaType.void

  override fun visit(operator: DivOperator) = visitBinaryOperator(operator)

  override fun visit(operator: PlusOperator) = visitBinaryOperator(operator)

  override fun visit(operator: MinusOperator) = visitBinaryOperator(operator)

  override fun visit(operator: PowOperator) = visitBinaryOperator(operator)

  override fun visit(rightShiftOperator: RightShiftOperator) = JavaType.Object

  override fun visit(leftShiftOperator: LeftShiftOperator) = JavaType.Object

  override fun visit(variableAssignmentNode: VariableAssignmentNode) = variableAssignmentNode.expression.accept(this)

  override fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode) = indexedVariableAssignmentNode.expression.accept(this)

  override fun visit(referenceExpression: ReferenceExpression) =
    try {
      referenceExpression.scope.findVariable(referenceExpression.name).type
    } catch (e: SemanticException) {
      // for static function calls
      referenceExpression.scope.getTypeOrNull(referenceExpression.name) ?: throw e
    }

  override fun visit(indexedReferenceExpression: IndexedReferenceExpression) =
    if (indexedReferenceExpression.variable.type.isArray) (indexedReferenceExpression.variable.type.asArrayType).elementsType
    else findMethodOrThrow(indexedReferenceExpression.variable.type, "getAt", indexedReferenceExpression.indexArguments.map { it.accept(this) }).returnType

  private fun visitUnaryOperator(unaryOperator: UnaryOperator) = unaryOperator.operand.accept(this)

  override fun visit(unaryMinus: UnaryMinus) = visitUnaryOperator(unaryMinus)

  override fun visit(unaryPlus: UnaryPlus) = visitUnaryOperator(unaryPlus)

  override fun visit(blockNode: BlockNode) = blockNode.statements.lastOrNull()?.accept(this) ?: JavaType.void

  override fun visit(blockNode: FunctionBlockNode) = visit(blockNode as BlockNode)

  companion object {
    fun getLambdaType(lambdaNode: LambdaNode): JavaType {
      return when (lambdaNode.parameters.size) {
        0, 1 -> JavaType.of(Lambda1::class.java)
        else -> TODO("Doesn't handle lambda with such parameters for now")
      }
    }
  }
  override fun visit(lambdaNode: LambdaNode) = lambdaNode.interfaceType ?: getLambdaType(lambdaNode)

  override fun visit(expressionStatementNode: ExpressionStatementNode) = expressionStatementNode.expression.accept(this)

  override fun visit(variableDeclarationNode: VariableDeclarationNode) = variableDeclarationNode.type

  override fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode) = JavaType.boolean

  override fun visit(returnNode: ReturnNode) = returnNode.expression.accept(this)

  override fun visit(voidExpression: VoidExpression) = JavaType.void

  override fun visit(asNode: AsNode) = asNode.type

  override fun visit(accessOperator: InvokeAccessOperator) = accessOperator.rightOperand.accept(this)




  override fun visit(comparisonOperatorNode: ComparisonOperatorNode) = JavaType.boolean

  override fun visit(andOperator: AndOperator) = JavaType.boolean

  override fun visit(orOperator: OrOperator) = JavaType.boolean

  override fun visit(notNode: NotNode) = JavaType.boolean

  override fun visit(ifStatementNode: IfStatementNode) =
    if (ifStatementNode.falseStatementNode != null) JavaType.commonType(ifStatementNode.trueStatementNode.accept(this),
      ifStatementNode.falseStatementNode!!.accept(this))
    else ifStatementNode.trueStatementNode.accept(this)

  override fun visit(forStatement: ForStatement) = JavaType.void

  override fun visit(forInStatement: ForInStatement) = JavaType.void
  override fun visit(whileStatement: WhileStatement) = JavaType.void

  override fun visit(booleanExpression: BooleanExpressionNode) = JavaType.boolean

  override fun visit(nullValueNode: NullValueNode) = nullValueNode.type ?: JavaType.Object

  override fun visit(incrNode: IncrNode) = visitUnaryOperator(incrNode)

  override fun visit(breakLoopNode: BreakLoopNode) = JavaType.void
  override fun visit(continueLoopNode: ContinueLoopNode) = JavaType.void


  // TODO change when supporting other primitive ranges
  override fun visit(rangeNode: RangeNode) = JavaType.of(IntRange::class.java)

  // the below methods can't guess type without class info, so they just return objects
  override fun visit(literalMapNode: LiteralMapNode): JavaType = JavaType.of(Map::class.java)

  override fun visit(fCall: FunctionCallNode): JavaType = JavaType.Object

  override fun visit(getFieldAccessOperator: GetFieldAccessOperator): JavaType = JavaType.Object

  override fun visit(literalListNode: LiteralArrayNode): JavaArrayType = JavaType.objectArray
}