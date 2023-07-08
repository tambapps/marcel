package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.expression.*
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
import com.tambapps.marcel.parser.scope.ClassField
import com.tambapps.marcel.parser.scope.DynamicMethodField
import com.tambapps.marcel.parser.scope.JavaField
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.NotLoadedJavaType
import marcel.lang.IntRange
import marcel.lang.LongRange
import marcel.lang.MarcelClassLoader
import marcel.lang.lambda.*
import java.util.regex.Matcher
import java.util.regex.Pattern

// useful for pulled dumbbells
open class AstNodeTypeResolver constructor(
  private val classLoader: MarcelClassLoader?
): AstNodeVisitor<JavaType> {

  constructor(): this(null)

  private val _definedTypes = mutableMapOf<String, JavaType>()
  val definedTypes get() = _definedTypes.values.toList()

  fun getInterfaceLambdaMethod(type: JavaType): JavaMethod {
    return getDeclaredMethods(type).first { it.isAbstract }
  }

  // TODO add node to get token when throwing exceptions
  fun defineClass(node: AstNode? = null, className: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>): JavaType {
    return defineClass(node, null, className, superClass, isInterface, interfaces)
  }
  fun defineClass(node: AstNode? = null, outerClassType: JavaType?, cName: String, superClass: JavaType, isInterface: Boolean, interfaces: List<JavaType>): JavaType {
    val className = if (outerClassType != null) "${outerClassType.className}\$$cName" else cName
    try {
      Class.forName(className)
      throw MarcelSemanticException(node?.token, "Class $className is already defined")
    } catch (e: ClassNotFoundException) {
      // ignore
    }
    if (_definedTypes.containsKey(className)) throw MarcelSemanticException(node?.token, "Class $className is already defined")
    val type = NotLoadedJavaType(className, emptyList(), emptyList(),  superClass, isInterface, interfaces.toMutableSet())
    _definedTypes[className] = type
    return type
  }

  fun registerClass(classNode: ClassNode) {
    _definedTypes[classNode.type.className] = classNode.type
    classNode.methods.forEach { defineMethod(classNode.type, it) }
    classNode.fields.forEach { defineField(classNode.type, it) }
    classNode.innerClasses.forEach { registerClass(it) }
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
    _definedTypes.clear()
  }

  open fun defineMethod(javaType: JavaType, method: JavaMethod) {
  }

  open fun defineField(javaType: JavaType, field: JavaField) {
  }

  open fun disposeClass(scriptNode: ClassNode) {
    _definedTypes.remove(scriptNode.type.className)
  }

  open fun getDeclaredMethods(javaType: JavaType): List<JavaMethod> {
    return emptyList()
  }

  open fun getDeclaredFields(javaType: JavaType): Collection<MarcelField> {
    return emptyList()
  }

  open fun getClassField(javaType: JavaType, fieldName: String, node: AstNode? = null): ClassField {
    throw MarcelSemanticException(node?.token, "Class field $javaType.$fieldName is not defined")
  }

  open fun getMethods(javaType: JavaType): List<JavaMethod> {
    return emptyList()
  }

  open fun of(className: String, genericTypes: List<JavaType>): JavaType {
    return _definedTypes[className] ?: JavaType.of(classLoader, className, genericTypes)
  }

  fun findMethodByParametersOrThrow(javaType: JavaType, name: String,
                                    positionalArgumentTypes: List<AstTypedObject>,
                                    namedParameters: Collection<MethodParameter>, node: AstNode? = null): JavaMethod {
    return findMethodByParameters(javaType, name, positionalArgumentTypes, namedParameters, false, node)
      ?: throw MarcelSemanticException(node?.token, "Method $javaType.$name with parameters $namedParameters is not defined")
  }
  fun findMethodByParameters(javaType: JavaType, name: String,
                             positionalArgumentTypes: List<AstTypedObject>,
                             namedParameters: Collection<MethodParameter>, excludeInterfaces: Boolean = false, node: AstNode? = null): JavaMethod? {
    val m = doFindMethodByParameters(javaType, name, positionalArgumentTypes, namedParameters, excludeInterfaces, node) ?: return null
    return if (javaType.genericTypes.isNotEmpty()) m.withGenericTypes(javaType.genericTypes)
    else m
  }

  protected open fun doFindMethodByParameters(javaType: JavaType, name: String,
                                              positionalArgumentTypes: List<AstTypedObject>,
                                              namedParameters: Collection<MethodParameter>, excludeInterfaces: Boolean = false, node: AstNode? = null): JavaMethod? {
    return null
  }

  fun findMethodOrThrow(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, node: AstNode? = null): JavaMethod {
    // TODO pass token as argument to be able to have line and column on exception
    return findMethod(javaType, name, argumentTypes, false, node) ?: throw MarcelSemanticException(node?.token, "Method $javaType.$name with parameters ${argumentTypes.map { it.type }} is not defined")
  }

  fun findMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean = false, node: AstNode? = null): JavaMethod? {
    val m = doFindMethod(javaType, name, argumentTypes, excludeInterfaces, node) ?: return null
    return if (javaType.genericTypes.isNotEmpty()) m.withGenericTypes(javaType.genericTypes)
    else m
  }

  protected open fun doFindMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean = false, node: AstNode? = null): JavaMethod? {
    return null
  }

  open fun findMatchingMethod(methods: List<JavaMethod>, name: String, argumentTypes: List<AstTypedObject>): JavaMethod? {
    return null
  }
  fun findFieldOrThrow(javaType: JavaType, name: String, node: AstNode? = null): MarcelField {
    return findField(javaType, name) ?: throw MarcelSemanticException(node?.token, "Field $name was not found")
  }

  open fun findField(javaType: JavaType, name: String, node: AstNode? = null): MarcelField? {
    return null
  }

  fun resolve(node: ExpressionNode) = node.accept(this)
  fun resolve(node: StatementNode) = node.accept(this)


  override fun visit(node: IntConstantNode) = JavaType.int

  override fun visit(node: LongConstantNode) = JavaType.long

  override fun visit(node: FloatConstantNode) = JavaType.float
  override fun visit(node: DoubleConstantNode) = JavaType.double
  override fun visit(node: CharConstantNode) = JavaType.char

  override fun visit(node: BooleanConstantNode) = JavaType.boolean

  override fun visit(node: StringNode) = JavaType.String

  override fun visit(node: StringConstantNode) = JavaType.String

  override fun visit(node: ToStringNode) = JavaType.String

  override fun visit(node: MulOperator) = visitBinaryOperator(node)

  private fun visitBinaryOperator(binaryOperatorNode: BinaryOperatorNode): JavaType {
    val commonType = JavaType.commonType(binaryOperatorNode.leftOperand.accept(this), binaryOperatorNode.rightOperand.accept(this))
    if (commonType.primitive || commonType.isPrimitiveObjectType) return commonType
    if (binaryOperatorNode.operatorMethodName == null) return commonType
    return findMethodOrThrow(binaryOperatorNode.leftOperand.accept(this), binaryOperatorNode.operatorMethodName, listOf(binaryOperatorNode.rightOperand.accept(this)), binaryOperatorNode).returnType
  }

  override fun visit(node: TernaryNode): JavaType {
    return JavaType.commonType(node.trueExpression.accept(this), node.falseExpression.accept(this))
  }

  override fun visit(node: ElvisOperator) = visitBinaryOperator(node)


  override fun visit(node: ConstructorCallNode) = node.type

  override fun visit(node: NamedParametersConstructorCallNode) = node.type

  override fun visit(node: SuperConstructorCallNode) = JavaType.void

  override fun visit(node: DivOperator) = visitBinaryOperator(node)

  override fun visit(node: PlusOperator) =
    if (node.leftOperand.accept(this) == JavaType.String
      || node.rightOperand.accept(this) == JavaType.String) JavaType.String
    else visitBinaryOperator(node)

  override fun visit(node: MinusOperator) = visitBinaryOperator(node)

  override fun visit(node: PowOperator) = visitBinaryOperator(node)

  override fun visit(node: RightShiftOperator) =
    findMethodOrThrow(node.leftOperand.accept(this), node.operatorMethodName!!, listOf(node.rightOperand.accept(this)), node).returnType

  override fun visit(node: LeftShiftOperator) =
    findMethodOrThrow(node.leftOperand.accept(this), node.operatorMethodName!!, listOf(node.rightOperand.accept(this)), node).returnType

  override fun visit(node: VariableAssignmentNode) = node.expression.accept(this)

  override fun visit(node: FieldAssignmentNode) = node.expression.accept(this)
  override fun visit(node: IndexedVariableAssignmentNode) = node.expression.accept(this)

  override fun visit(node: ReferenceExpression) =
    try {
      val v = node.scope.findVariableOrThrow(node.name, node)
      if (v is DynamicMethodField && node.scope.classType.implements(JavaType.DynamicObject))
        node.scope.getTypeOrNull(node.name) ?: throw MarcelSemanticException(node.token, "No variable or class named ${node.name} was found")
      else node.scope.findVariableOrThrow(node.name, node).type
    } catch (e: MarcelSemanticException) {
      // for static function calls
      node.scope.getTypeOrNull(node.name) ?: throw MarcelSemanticException(node.token, "No variable or class named ${node.name} was found")
    }

  override fun visit(node: IndexedReferenceExpression): JavaType {
    val elementType = if (node.variable.type.isArray) (node.variable.type.asArrayType).elementsType
    else findMethodOrThrow(node.variable.type, "getAt", node.indexArguments.map { it.accept(this) }, node).actualReturnType
    // need object class for safe index because returned elements are nullable
    return if (node.isSafeIndex) elementType.objectType else elementType
  }

  private fun visitUnaryOperator(unaryOperator: UnaryOperator) = unaryOperator.operand.accept(this)

  override fun visit(node: UnaryMinus) = visitUnaryOperator(node)

  override fun visit(node: UnaryPlus) = visitUnaryOperator(node)

  override fun visit(node: BlockNode) = node.statements.lastOrNull()?.accept(this) ?: JavaType.void

  override fun visit(node: FunctionBlockNode) = visit(node as BlockNode)

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
        2 -> JavaType.of(Lambda2::class.java).withGenericTypes(lambdaNode.parameters.map { it.type } + returnType)
        3 -> JavaType.of(Lambda3::class.java).withGenericTypes(lambdaNode.parameters.map { it.type } + returnType)
        4 -> JavaType.of(Lambda4::class.java).withGenericTypes(lambdaNode.parameters.map { it.type } + returnType)
        5 -> JavaType.of(Lambda5::class.java).withGenericTypes(lambdaNode.parameters.map { it.type } + returnType)
        6 -> JavaType.of(Lambda6::class.java).withGenericTypes(lambdaNode.parameters.map { it.type } + returnType)
        7 -> JavaType.of(Lambda7::class.java).withGenericTypes(lambdaNode.parameters.map { it.type } + returnType)
        8 -> JavaType.of(Lambda8::class.java).withGenericTypes(lambdaNode.parameters.map { it.type } + returnType)
        9 -> JavaType.of(Lambda9::class.java).withGenericTypes(lambdaNode.parameters.map { it.type } + returnType)
        10 -> JavaType.of(Lambda10::class.java).withGenericTypes(lambdaNode.parameters.map { it.type } + returnType)
        else -> throw MarcelSemanticException(lambdaNode.token, "Doesn't handle lambdas with more than 10 parameters")
      }
    }
  }
  override fun visit(node: LambdaNode) = node.interfaceType ?: getLambdaType(this, node)

  override fun visit(node: ExpressionStatementNode) = node.expression.accept(this)

  override fun visit(node: VariableDeclarationNode) = node.type

  override fun visit(node: TruthyVariableDeclarationNode) = JavaType.boolean
  override fun visit(node: MultiVariableDeclarationNode) = JavaType.void

  override fun visit(node: ReturnNode) = node.expression.accept(this)

  override fun visit(node: VoidExpression) = JavaType.void

  override fun visit(node: AsNode) = node.type

  override fun visit(node: ComparisonOperatorNode) = JavaType.boolean

  override fun visit(node: AndOperator) = JavaType.boolean

  override fun visit(node: OrOperator) = JavaType.boolean

  override fun visit(node: NotNode) = JavaType.boolean

  override fun visit(node: IfStatementNode) = JavaType.void

  override fun visit(node: ForStatement) = JavaType.void
  override fun visit(node: TryCatchNode) = JavaType.void

  override fun visit(node: ForInStatement) = JavaType.void
  override fun visit(node: WhileStatement) = JavaType.void

  override fun visit(node: BooleanExpressionNode) = JavaType.boolean

  override fun visit(node: NullValueNode) = node.type ?: JavaType.Object

  override fun visit(node: IncrNode) = visitUnaryOperator(node)

  override fun visit(node: BreakLoopNode) = JavaType.void
  override fun visit(node: ContinueLoopNode) = JavaType.void

  override fun visit(node: RangeNode): JavaType {
    val fromType = node.from.accept(this)
    val toType = node.to.accept(this)

    return if (fromType == JavaType.long || fromType == JavaType.Long
      || toType == JavaType.long || toType == JavaType.Long) JavaType.of(LongRange::class.java)
    else JavaType.of(IntRange::class.java)
  }

  // the below methods can't guess type without class info, so they just return objects
  override fun visit(node: LiteralMapNode): JavaType = JavaType.of(Map::class.java)

  override fun visit(node: FunctionCallNode): JavaType = findMethodOrThrow(node.methodOwnerType?.accept(this) ?: node.scope.classType,
    node.name, node.getArguments(this).map { it.accept(this) }, node).actualReturnType

  // it is object because we need type resolver in order to be able to get the real type. that's why it is overridden in JavaTypeResolver
  override fun visit(node: GetFieldAccessOperator): JavaType = JavaType.Object
  override fun visit(node: GetIndexFieldAccessOperator): JavaType = JavaType.Object

  override fun visit(node: InvokeAccessOperator) =
    if (node.nullSafe) node.rightOperand.accept(this).objectType
    else node.rightOperand.accept(this)

  override fun visit(node: LiteralArrayNode): JavaArrayType = JavaType.objectArray

  override fun visit(node: SwitchNode) = JavaType.commonType(node.branches.map { it.accept(this) })

  override fun visit(node: SwitchBranchNode): JavaType = node.statementNode.accept(this)

  override fun visit(node: WhenNode) = JavaType.commonType(node.branches.map { it.accept(this) })

  override fun visit(node: WhenBranchNode) = node.statementNode.accept(this)

  override fun visit(node: IsOperator) = JavaType.boolean
  override fun visit(node: IsNotOperator) = JavaType.boolean

  override fun visit(node: ByteConstantNode) = JavaType.byte

  override fun visit(node: ShortConstantNode) = JavaType.short

  override fun visit(node: ThisReference) =
    if (node.scope.staticContext && node.scope.hasVariable("self")) node.scope
      .findVariableOrThrow("self", node).type
  else node.scope.classType
  override fun visit(node: SuperReference) = node.scope.superClass

  override fun visit(node: LiteralPatternNode) = JavaType.of(Pattern::class.java)
  override fun visit(node: FindOperator) = JavaType.of(Matcher::class.java)

  override fun visit(node: ClassExpressionNode) = JavaType.of(Class::class.java, listOf(node.clazz))

  override fun visit(node: DirectFieldAccessNode) = getClassField(node.scope.classType, node.name, node).type
}