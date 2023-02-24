package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.expression.AndOperator
import com.tambapps.marcel.parser.ast.expression.AsNode
import com.tambapps.marcel.parser.ast.expression.BinaryOperatorNode
import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.BooleanConstantNode
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.ByteConstantNode
import com.tambapps.marcel.parser.ast.expression.CharConstantNode
import com.tambapps.marcel.parser.ast.expression.ComparisonOperatorNode
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.DivOperator
import com.tambapps.marcel.parser.ast.expression.DoubleConstantNode
import com.tambapps.marcel.parser.ast.expression.ElvisOperator
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.FieldAssignmentNode
import com.tambapps.marcel.parser.ast.expression.FindOperator
import com.tambapps.marcel.parser.ast.expression.FloatConstantNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.GetFieldAccessOperator
import com.tambapps.marcel.parser.ast.expression.IncrNode
import com.tambapps.marcel.parser.ast.expression.IndexedReferenceExpression
import com.tambapps.marcel.parser.ast.expression.IndexedVariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.InvokeAccessOperator
import com.tambapps.marcel.parser.ast.expression.IsNotOperator
import com.tambapps.marcel.parser.ast.expression.IsOperator
import com.tambapps.marcel.parser.ast.expression.LambdaNode
import com.tambapps.marcel.parser.ast.expression.LeftShiftOperator
import com.tambapps.marcel.parser.ast.expression.LiteralArrayNode
import com.tambapps.marcel.parser.ast.expression.LiteralMapNode
import com.tambapps.marcel.parser.ast.expression.LongConstantNode
import com.tambapps.marcel.parser.ast.expression.MinusOperator
import com.tambapps.marcel.parser.ast.expression.MulOperator
import com.tambapps.marcel.parser.ast.expression.NotNode
import com.tambapps.marcel.parser.ast.expression.NullValueNode
import com.tambapps.marcel.parser.ast.expression.OrOperator
import com.tambapps.marcel.parser.ast.expression.LiteralPatternNode
import com.tambapps.marcel.parser.ast.expression.NamedParametersConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.PlusOperator
import com.tambapps.marcel.parser.ast.expression.PowOperator
import com.tambapps.marcel.parser.ast.expression.RangeNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.ReturnNode
import com.tambapps.marcel.parser.ast.expression.RightShiftOperator
import com.tambapps.marcel.parser.ast.expression.ShortConstantNode
import com.tambapps.marcel.parser.ast.expression.StringConstantNode
import com.tambapps.marcel.parser.ast.expression.StringNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.SuperReference
import com.tambapps.marcel.parser.ast.expression.SwitchBranchNode
import com.tambapps.marcel.parser.ast.expression.SwitchNode
import com.tambapps.marcel.parser.ast.expression.TernaryNode
import com.tambapps.marcel.parser.ast.expression.ThisReference
import com.tambapps.marcel.parser.ast.expression.ToStringNode
import com.tambapps.marcel.parser.ast.expression.TruthyVariableDeclarationNode
import com.tambapps.marcel.parser.ast.expression.UnaryMinus
import com.tambapps.marcel.parser.ast.expression.UnaryOperator
import com.tambapps.marcel.parser.ast.expression.UnaryPlus
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.VoidExpression
import com.tambapps.marcel.parser.ast.expression.WhenBranchNode
import com.tambapps.marcel.parser.ast.expression.WhenNode
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.MultiVariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.NotLoadedJavaType
import marcel.lang.IntRange
import marcel.lang.LongRange
import marcel.lang.lambda.CharacterLambda1
import marcel.lang.lambda.DoubleLambda1
import marcel.lang.lambda.FloatLambda1
import marcel.lang.lambda.IntLambda1
import marcel.lang.lambda.Lambda1
import marcel.lang.lambda.LongLambda1
import java.util.regex.Matcher
import java.util.regex.Pattern

open class AstNodeTypeResolver: AstNodeVisitor<JavaType> {

  private val definedTypes = mutableMapOf<String, JavaType>()

  fun getInterfaceLambdaMethod(type: JavaType): JavaMethod {
    return getDeclaredMethods(type).first { it.isAbstract }
  }

  fun defineClass(className: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>): JavaType {
    return defineClass(null, className, superClass, isInterface, interfaces)
  }
  fun defineClass(outerClassType: JavaType?, cName: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>): JavaType {
    val className = if (outerClassType != null) "${outerClassType.className}\$$cName" else cName
    try {
      Class.forName(className)
      throw MarcelSemanticException("Class $className is already defined")
    } catch (e: ClassNotFoundException) {
      // ignore
    }
    if (definedTypes.containsKey(className)) throw MarcelSemanticException("Class $className is already defined")
    // TODO only handle interfaces that are already loaded, as the type is not lazily loaded
    val type = NotLoadedJavaType(className, emptyList(), emptyList(),  superClass, isInterface, interfaces)
    definedTypes[className] = type
    return type
  }

  fun isDefined(className: String): Boolean {
    return try {
      of(className, emptyList())
      true
    } catch (e: MarcelSemanticException) {
      false
    }
  }

  fun clear() {
    definedTypes.clear()
  }

  open fun defineMethod(javaType: JavaType, method: JavaMethod) {
  }

  open fun defineField(javaType: JavaType, field: MarcelField) {
  }

  open fun disposeClass(scriptNode: ClassNode) {
    definedTypes.remove(scriptNode.type.className)
  }

  fun defineClassMembers(classNode: ClassNode) {
    classNode.methods.forEach { defineMethod(classNode.type, it) }
    classNode.fields.forEach { defineField(classNode.type, it) }
    classNode.innerClasses.forEach { defineClassMembers(it) }
  }

    open fun getDeclaredMethods(javaType: JavaType): List<JavaMethod> {
    return emptyList()
  }

  open fun of(className: String, genericTypes: List<JavaType>): JavaType {
    return definedTypes[className] ?: JavaType.of(className, genericTypes)
  }

  fun findMethodByParametersOrThrow(javaType: JavaType, name: String, namedParameters: Collection<MethodParameter>): JavaMethod {
    return findMethodByParameters(javaType, name, namedParameters)
      ?: throw MarcelSemanticException("Method $javaType.$name with parameters $namedParameters is not defined")
  }
  fun findMethodByParameters(javaType: JavaType, name: String, namedParameters: Collection<MethodParameter>, excludeInterfaces: Boolean = false): JavaMethod? {
    val m = doFindMethodByParameters(javaType, name, namedParameters, excludeInterfaces) ?: return null
    return if (javaType.genericTypes.isNotEmpty()) m.withGenericTypes(javaType.genericTypes)
    else m
  }

  protected open fun doFindMethodByParameters(javaType: JavaType, name: String, namedParameters: Collection<MethodParameter>, excludeInterfaces: Boolean = false): JavaMethod? {
    return null
  }

  fun findMethodOrThrow(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>): JavaMethod {
    return findMethod(javaType, name, argumentTypes) ?: throw MarcelSemanticException("Method $javaType.$name with parameters ${argumentTypes.map { it.type }} is not defined")
  }

  fun findMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean = false): JavaMethod? {
    val m = doFindMethod(javaType, name, argumentTypes, excludeInterfaces) ?: return null
    return if (javaType.genericTypes.isNotEmpty()) m.withGenericTypes(javaType.genericTypes)
    else m
  }

  protected open fun doFindMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean = false): JavaMethod? {
    return null
  }

  fun findFieldOrThrow(javaType: JavaType, name: String, declared: Boolean = true): MarcelField {
    return findField(javaType, name, declared) ?: throw MarcelSemanticException("Field $name was not found")
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
  override fun visit(charNode: CharConstantNode) = JavaType.char

  override fun visit(booleanConstantNode: BooleanConstantNode) = JavaType.boolean

  override fun visit(stringNode: StringNode) = JavaType.String

  override fun visit(stringConstantNode: StringConstantNode) = JavaType.String

  override fun visit(toStringNode: ToStringNode) = JavaType.String

  override fun visit(operator: MulOperator) = visitBinaryOperator(operator)

  private fun visitBinaryOperator(binaryOperatorNode: BinaryOperatorNode) =
    JavaType.commonType(binaryOperatorNode.leftOperand.accept(this), binaryOperatorNode.rightOperand.accept(this))

  override fun visit(operator: TernaryNode): JavaType {
    return JavaType.commonType(operator.trueExpression.accept(this), operator.falseExpression.accept(this))
  }

  override fun visit(elvisOperator: ElvisOperator) = visitBinaryOperator(elvisOperator)


  override fun visit(fCall: ConstructorCallNode) = fCall.type

  override fun visit(fCall: NamedParametersConstructorCallNode) = fCall.type

  override fun visit(fCall: SuperConstructorCallNode) = JavaType.void

  override fun visit(operator: DivOperator) = visitBinaryOperator(operator)

  override fun visit(operator: PlusOperator) = visitBinaryOperator(operator)

  override fun visit(operator: MinusOperator) = visitBinaryOperator(operator)

  override fun visit(operator: PowOperator) = visitBinaryOperator(operator)

  override fun visit(rightShiftOperator: RightShiftOperator) = JavaType.Object

  override fun visit(leftShiftOperator: LeftShiftOperator) = JavaType.Object

  override fun visit(variableAssignmentNode: VariableAssignmentNode) = variableAssignmentNode.expression.accept(this)

  override fun visit(fieldAssignmentNode: FieldAssignmentNode) = fieldAssignmentNode.expression.accept(this)
  override fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode) = indexedVariableAssignmentNode.expression.accept(this)

  override fun visit(referenceExpression: ReferenceExpression) =
    try {
      referenceExpression.scope.findVariableOrThrow(referenceExpression.name).type
    } catch (e: MarcelSemanticException) {
      // for static function calls
      referenceExpression.scope.getTypeOrNull(referenceExpression.name) ?: throw MarcelSemanticException("No variable or class named ${referenceExpression.name} was found")
    }

  override fun visit(indexedReferenceExpression: IndexedReferenceExpression): JavaType {
    val elementType = if (indexedReferenceExpression.variable.type.isArray) (indexedReferenceExpression.variable.type.asArrayType).elementsType
    else findMethodOrThrow(indexedReferenceExpression.variable.type, "getAt", indexedReferenceExpression.indexArguments.map { it.accept(this) }).actualReturnType
    // need object class for safe index because returned elements are nullable
    return if (indexedReferenceExpression.isSafeIndex) elementType.objectType else elementType
  }

  private fun visitUnaryOperator(unaryOperator: UnaryOperator) = unaryOperator.operand.accept(this)

  override fun visit(unaryMinus: UnaryMinus) = visitUnaryOperator(unaryMinus)

  override fun visit(unaryPlus: UnaryPlus) = visitUnaryOperator(unaryPlus)

  override fun visit(blockNode: BlockNode) = blockNode.statements.lastOrNull()?.accept(this) ?: JavaType.void

  override fun visit(blockNode: FunctionBlockNode) = visit(blockNode as BlockNode)

  companion object {
    fun getLambdaType(typeResolver: AstNodeTypeResolver, lambdaNode: LambdaNode): JavaType {
      val returnType = if (lambdaNode.interfaceType != null)
        typeResolver.getDeclaredMethods(lambdaNode.interfaceType!!).first { it.isAbstract }.actualReturnType.objectType
      else JavaType.Object

      return when (lambdaNode.parameters.size) {
        0 -> JavaType.of(Lambda1::class.java).withGenericTypes(JavaType.Object)
        1 -> when(lambdaNode.parameters.first().type) {
            JavaType.int -> JavaType.of(IntLambda1::class.java).withGenericTypes(returnType)
            JavaType.long -> JavaType.of(LongLambda1::class.java).withGenericTypes(returnType)
            JavaType.float -> JavaType.of(FloatLambda1::class.java).withGenericTypes(returnType)
            JavaType.double -> JavaType.of(DoubleLambda1::class.java).withGenericTypes(returnType)
            JavaType.char -> JavaType.of(CharacterLambda1::class.java).withGenericTypes(returnType)
            else -> JavaType.of(Lambda1::class.java).withGenericTypes(lambdaNode.parameters.first().type.objectType, returnType)
          }
        else -> TODO("Doesn't handle lambda with such parameters for now")
      }
    }
  }
  override fun visit(lambdaNode: LambdaNode) = lambdaNode.interfaceType ?: getLambdaType(this, lambdaNode)

  override fun visit(expressionStatementNode: ExpressionStatementNode) = expressionStatementNode.expression.accept(this)

  override fun visit(variableDeclarationNode: VariableDeclarationNode) = variableDeclarationNode.type

  override fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode) = JavaType.boolean
  override fun visit(multiVariableDeclarationNode: MultiVariableDeclarationNode) = JavaType.void

  override fun visit(returnNode: ReturnNode) = returnNode.expression.accept(this)

  override fun visit(voidExpression: VoidExpression) = JavaType.void

  override fun visit(asNode: AsNode) = asNode.type

  override fun visit(comparisonOperatorNode: ComparisonOperatorNode) = JavaType.boolean

  override fun visit(andOperator: AndOperator) = JavaType.boolean

  override fun visit(orOperator: OrOperator) = JavaType.boolean

  override fun visit(notNode: NotNode) = JavaType.boolean

  override fun visit(ifStatementNode: IfStatementNode) = JavaType.void

  override fun visit(forStatement: ForStatement) = JavaType.void

  override fun visit(forInStatement: ForInStatement) = JavaType.void
  override fun visit(whileStatement: WhileStatement) = JavaType.void

  override fun visit(booleanExpression: BooleanExpressionNode) = JavaType.boolean

  override fun visit(nullValueNode: NullValueNode) = nullValueNode.type ?: JavaType.Object

  override fun visit(incrNode: IncrNode) = visitUnaryOperator(incrNode)

  override fun visit(breakLoopNode: BreakLoopNode) = JavaType.void
  override fun visit(continueLoopNode: ContinueLoopNode) = JavaType.void

  override fun visit(rangeNode: RangeNode): JavaType {
    val fromType = rangeNode.from.accept(this)
    val toType = rangeNode.to.accept(this)

    return if (fromType == JavaType.long || fromType == JavaType.Long
      || toType == JavaType.long || toType == JavaType.Long) JavaType.of(LongRange::class.java)
    else JavaType.of(IntRange::class.java)
  }

  // the below methods can't guess type without class info, so they just return objects
  override fun visit(literalMapNode: LiteralMapNode): JavaType = JavaType.of(Map::class.java)

  override fun visit(fCall: FunctionCallNode): JavaType = findMethodOrThrow(fCall.methodOwnerType?.accept(this) ?: fCall.scope.classType,
    fCall.name, fCall.arguments.map { it.accept(this) }).actualReturnType

  // it is object because we need type resolver in order to be able to get the real type. that's why it is overridden in JavaTypeResolver
  override fun visit(getFieldAccessOperator: GetFieldAccessOperator): JavaType = JavaType.Object

  override fun visit(accessOperator: InvokeAccessOperator) =
    if (accessOperator.nullSafe) accessOperator.rightOperand.accept(this).objectType
    else accessOperator.rightOperand.accept(this)

  override fun visit(literalListNode: LiteralArrayNode): JavaArrayType = JavaType.objectArray

  override fun visit(switchNode: SwitchNode): JavaType {

    val itVariableType = switchNode.expressionNode.accept(this)
    val type = switchNode.scope.simulateVariable(itVariableType, "it") {
      JavaType.commonType(switchNode.branches.map { it.accept(this) })
    }
    return type
  }

  override fun visit(switchBranch: SwitchBranchNode): JavaType = switchBranch.statementNode.accept(this)

  override fun visit(whenNode: WhenNode) = JavaType.commonType(whenNode.branches.map { it.accept(this) })

  override fun visit(whenBranchNode: WhenBranchNode) = whenBranchNode.statementNode.accept(this)

  override fun visit(isOperator: IsOperator) = JavaType.boolean
  override fun visit(isNotOperator: IsNotOperator) = JavaType.boolean

  override fun visit(byteConstantNode: ByteConstantNode) = JavaType.byte

  override fun visit(shortConstantNode: ShortConstantNode) = JavaType.short

  override fun visit(thisReference: ThisReference) = thisReference.scope.classType
  override fun visit(superReference: SuperReference) = superReference.scope.superClass

  override fun visit(patternValueNode: LiteralPatternNode) = JavaType.of(Pattern::class.java)
  override fun visit(findOperator: FindOperator) = JavaType.of(Matcher::class.java)
}