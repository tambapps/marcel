package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.ConstructorCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.FieldCstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
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
import com.tambapps.marcel.parser.cst.expression.BinaryTypeOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.CstExpressionNode
import com.tambapps.marcel.parser.cst.expression.NotCstNode
import com.tambapps.marcel.parser.cst.expression.TernaryCstNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusCstNode
import com.tambapps.marcel.parser.cst.expression.literal.BoolCstNode
import com.tambapps.marcel.parser.cst.expression.literal.CharCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.ForInCstNode
import com.tambapps.marcel.parser.cst.statement.ForVarCstNode
import com.tambapps.marcel.parser.cst.statement.IfCstStatementNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ArrayAccessNode
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.DupNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.PopNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.TernaryNode
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
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.JavaConstantExpression
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.expression.operator.AndNode
import com.tambapps.marcel.semantic.ast.expression.operator.ArrayIndexAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.operator.BinaryOperatorNode
import com.tambapps.marcel.semantic.ast.expression.operator.DivNode
import com.tambapps.marcel.semantic.ast.expression.operator.ElvisNode
import com.tambapps.marcel.semantic.ast.expression.operator.GeNode
import com.tambapps.marcel.semantic.ast.expression.operator.GtNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsNotEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeftShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.LtNode
import com.tambapps.marcel.semantic.ast.expression.operator.MinusNode
import com.tambapps.marcel.semantic.ast.expression.operator.ModNode
import com.tambapps.marcel.semantic.ast.expression.operator.MulNode
import com.tambapps.marcel.semantic.ast.expression.operator.NotNode
import com.tambapps.marcel.semantic.ast.expression.operator.OrNode
import com.tambapps.marcel.semantic.ast.expression.operator.PlusNode
import com.tambapps.marcel.semantic.ast.expression.operator.RightShiftNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaConstructorImpl
import com.tambapps.marcel.semantic.method.JavaMethodImpl
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.scope.ImportScope
import com.tambapps.marcel.semantic.scope.MethodInnerScope
import com.tambapps.marcel.semantic.scope.MethodScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaAnnotation
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.variable.Variable
import com.tambapps.marcel.semantic.variable.field.JavaClassFieldImpl
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.visitor.AllPathsReturnVisitor
import marcel.lang.IntRanges
import marcel.lang.LongRanges
import marcel.lang.Script
import marcel.lang.primitives.iterators.CharacterIterator
import marcel.lang.primitives.iterators.DoubleIterator
import marcel.lang.primitives.iterators.FloatIterator
import marcel.lang.primitives.iterators.IntIterator
import marcel.lang.primitives.iterators.LongIterator
import marcel.lang.runtime.BytecodeHelper
import java.lang.annotation.ElementType
import java.util.LinkedList

// TODO implement multiple errors like in parser2
class MarcelSemantic(
  private val typeResolver: JavaTypeResolver,
  private val cst: SourceFileCstNode
): ExpressionCstNodeVisitor<ExpressionNode>, StatementCstNodeVisitor<StatementNode> {

  companion object {
    const val PUT_AT_METHOD_NAME = "putAt"
    const val PUT_AT_SAFE_METHOD_NAME = "putAtSafe"
    const val GET_AT_METHOD_NAME = "getAt"
    const val GET_AT_SAFE_METHOD_NAME = "getAtSafe"
  }
  private val caster = AstNodeCaster(typeResolver)

  val exprVisitor = this as ExpressionCstNodeVisitor<ExpressionNode>
  val stmtVisitor = this as StatementCstNodeVisitor<StatementNode>

  internal val scopeQueue = LinkedList<Scope>()
  // FIFO
  private val currentScope get() = scopeQueue.peek()
  private val currentMethodScope get() = currentScope as? MethodScope ?: throw MarcelSemanticException("Not in a method")

  fun apply(): ModuleNode {
    val imports = Scope.DEFAULT_IMPORTS.toMutableList()
    // TODO analyse package and imports if any

    val moduleNode = ModuleNode(cst.tokenStart, cst.tokenEnd)
    val scriptCstNode = cst.script

    scopeQueue.push(ImportScope(typeResolver, imports))
    // define everything
    cst.classes.forEach { defineClass(it) }

    if (scriptCstNode != null) {
      val classType = typeResolver.defineClass(scriptCstNode.tokenStart, Visibility.PUBLIC,
        if (cst.packageName != null) "${cst.packageName}.${scriptCstNode.className}" else scriptCstNode.className,
        Script::class.javaType, false, emptyList())
      // register script class members
      defineClass(scriptCstNode, classType)
      val scriptNode = classNode(classType, scriptCstNode, imports)
      useScope(ClassScope(classType, typeResolver, imports)) {
        // add the run method
        val runMethod = SemanticHelper.scriptRunMethod(classType, cst)
        fillMethodNode(it, runMethod, scriptCstNode.runMethodStatements, emptyList(),  scriptRunMethod = true)
        scriptNode.methods.add(runMethod)
      }
      moduleNode.classes.add(scriptNode)
    }
    return moduleNode
  }

  private fun defineClass(classCstNode: ClassCstNode) {
    // TODO handle custom superType and interfaces
    val classType = typeResolver.defineClass(classCstNode.tokenStart, Visibility.PUBLIC,
      if (cst.packageName != null) "${cst.packageName}.${classCstNode.className}" else classCstNode.className,
      JavaType.Object, false, emptyList())
    defineClass(classCstNode, classType)
  }

  private fun defineClass(classCstNode: ClassCstNode, classType: JavaType) {
    classCstNode.methods.forEach { typeResolver.defineMethod(classType, toJavaMethod(classType, it)) }
    classCstNode.constructors.forEach { typeResolver.defineMethod(classType, toJavaConstructor(classType, it)) }
    classCstNode.fields.forEach { typeResolver.defineField(classType, toMarcelField(classType, it)) }
    classCstNode.innerClasses.forEach { defineClass(it) }
  }

  private fun classNode(classType: JavaType, node: ClassCstNode, imports: List<ImportNode>): ClassNode
  = useScope(ClassScope(classType, typeResolver, imports)) { classScope ->
    val classNode = ClassNode(classType, Visibility.PUBLIC, cst.tokenStart, cst.tokenEnd)

    node.annotations.forEach { classNode.annotations.add(annotationNode(it, ElementType.TYPE)) }
    node.methods.forEach { classNode.methods.add(methodNode(it, classScope)) }
    // TODO handle fields default value if any
    val fieldInitialValueMap = mutableMapOf<FieldNode, ExpressionNode>()
    node.fields.forEach { cstFieldNode ->
      val fieldNode = FieldNode(visit(cstFieldNode.type), cstFieldNode.name, classType,
        cstFieldNode.annotations.map { annotationNode(it, ElementType.FIELD) },
        cstFieldNode.access.isFinal, Visibility.fromTokenType(cstFieldNode.access.visibility),
        cstFieldNode.access.isStatic, cstFieldNode.tokenStart, cstFieldNode.tokenEnd)
      classNode.fields.add(fieldNode)

      if (cstFieldNode.initialValue != null) {
        fieldInitialValueMap[fieldNode] = caster.cast(fieldNode.type, cstFieldNode.initialValue!!.accept(exprVisitor))
      }
    }

    if (classNode.constructorCount == 0) {
      // default no arg constructor
      classNode.methods.add(SemanticHelper.noArgConstructor(classNode, typeResolver))
    }

    val fieldAssignmentStatements = fieldInitialValueMap.map { entry ->
      val field = entry.key
      val initialValue = entry.value
      ExpressionStatementNode(VariableAssignmentNode(variable = field,
        owner = ThisReferenceNode(classType, field.token),
        expression = initialValue,
        tokenStart = field.tokenStart, tokenEnd = initialValue.tokenEnd))


    }
    for (constructorNode in classNode.constructors) {
      // add at one because the first statement is the super call
      constructorNode.blockStatement.statements.addAll(1,
        fieldAssignmentStatements
        )
    }
    return classNode
  }

  private fun annotationNode(cstAnnotation: AnnotationCstNode, elementType: ElementType): AnnotationNode {
    val annotationType = visit(cstAnnotation.typeCstNode)
    if (!annotationType.isAnnotation) throw MarcelSemanticException("$annotationType is not an annotation")
    val javaAnnotation = JavaAnnotation.of(annotationType)
    if (!javaAnnotation.targets.contains(elementType)) {
      throw MarcelSemanticException(cstAnnotation, "Annotation ${javaAnnotation.type} is not expected on elements of type $elementType")
    }

    val annotation = AnnotationNode(
      type = annotationType,
      tokenStart = cstAnnotation.tokenStart,
      attributeNodes = cstAnnotation.attributes.map { annotationAttribute(cstAnnotation, javaAnnotation, it) },
      tokenEnd = cstAnnotation.tokenEnd
    )

    // check attributes without default values that weren't specified
    for (attr in javaAnnotation.attributes) {
      if (attr.defaultValue == null && annotation.attributeNodes.none { it.name == attr.name }) {
        throw MarcelSemanticException(cstAnnotation, "Attribute ${attr.name} has no default value and was not specified for annotation ${javaAnnotation.type}")
      }
    }

    return annotation
  }

  private fun annotationAttribute(node: AnnotationCstNode, javaAnnotation: JavaAnnotation, specifiedAttr: Pair<String, CstExpressionNode>): AnnotationNode.AttributeNode {
    val attribute = javaAnnotation.attributes.find { it.name == specifiedAttr.first }
      ?: throw MarcelSemanticException(node.token, "Unknown member ${specifiedAttr.first} for annotation $javaAnnotation")
    val specifiedValueNode = specifiedAttr.second.accept(exprVisitor)
        as? JavaConstantExpression ?: throw MarcelSemanticException(node, "Specified a non constant value for attribute ${attribute.name}")
    return if (attribute.type.isEnum) {
      TODO()
    } else {
      val attrValue = (specifiedValueNode.value ?: attribute.defaultValue)
      ?: throw MarcelSemanticException(node, "Attribute value cannot be null${attribute.name}")

      // check type
      when(attribute.type) {
        JavaType.String -> if (attrValue !is String) annotationErrorAttributeTypeError(node, javaAnnotation, attribute, attrValue)
        JavaType.int -> if (attrValue !is Int) annotationErrorAttributeTypeError(node, javaAnnotation, attribute, attrValue)
        JavaType.long -> if (attrValue !is Long) annotationErrorAttributeTypeError(node, javaAnnotation, attribute, attrValue)
        JavaType.float -> if (attrValue !is Float) annotationErrorAttributeTypeError(node, javaAnnotation, attribute, attrValue)
        JavaType.double -> if (attrValue !is Double) annotationErrorAttributeTypeError(node, javaAnnotation, attribute, attrValue)
        JavaType.char -> if (attrValue !is Char) annotationErrorAttributeTypeError(node, javaAnnotation, attribute, attrValue)
        JavaType.boolean -> if (attrValue !is Boolean) annotationErrorAttributeTypeError(node, javaAnnotation, attribute, attrValue)
        JavaType.byte -> if (attrValue !is Byte) annotationErrorAttributeTypeError(node, javaAnnotation, attribute, attrValue)
        JavaType.short -> if (attrValue !is Short) annotationErrorAttributeTypeError(node, javaAnnotation, attribute, attrValue)
        else -> annotationErrorAttributeTypeError(node, javaAnnotation, attribute, attrValue)
      }

      AnnotationNode.AttributeNode(attribute.name, attribute.type, attrValue)
    }
  }

  private fun annotationErrorAttributeTypeError(node: AnnotationCstNode, annotation: JavaAnnotation, attribute: JavaAnnotation.Attribute, attrValue: Any): Nothing
      = throw MarcelSemanticException(node, "Incompatible type for annotation member ${attribute.name} of annotation ${annotation.type}. Wanted ${attribute.type} but got ${attrValue.javaClass}")

  private fun methodNode(methodCst: MethodCstNode, classScope: ClassScope): MethodNode {
    val methodNode = MethodNode(
      name = methodCst.name,
      visibility = Visibility.fromTokenType(methodCst.accessNode.visibility),
      returnType = visit(methodCst.returnTypeCstNode),
      isStatic = methodCst.accessNode.isStatic,
      tokenStart = methodCst.tokenStart,
      tokenEnd = methodCst.tokenEnd,
      parameters = methodCst.parameters.map { toMethodParameter(it) },
      ownerClass = classScope.classType
    )
    fillMethodNode(classScope, methodNode, methodCst.statements, methodCst.annotations)
    return methodNode
  }

  private fun fillMethodNode(classScope: ClassScope, methodeNode: MethodNode,
                             cstStatements: List<StatementCstNode>,
                             annotations: List<AnnotationCstNode>,
                             scriptRunMethod: Boolean = false): Unit
  = useScope(MethodScope(classScope, methodeNode)) {

    // filling annotations
    annotations.forEach { methodeNode.annotations.add(annotationNode(it, ElementType.METHOD)) }

    val statements = blockStatements(cstStatements)
    // for single statement functions
    if (!methodeNode.isConstructor && !scriptRunMethod && statements.size == 1) {
      val statement = statements.first()
      if (methodeNode.returnType != JavaType.void && statement is ExpressionStatementNode) {
        statements[0] = ReturnStatementNode(statement.expressionNode, statement.tokenStart, statement.tokenEnd)
      }
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

  private fun blockStatements(cstStatements: List<StatementCstNode>): MutableList<StatementNode> {
    val statements = mutableListOf<StatementNode>()
    for (i in cstStatements.indices) {
      val statement = cstStatements[i].accept(stmtVisitor)
      // TODO add this check in all block/list of statements
      if (statement is ReturnStatementNode && i < cstStatements.lastIndex)
        throw MarcelSemanticException("Cannot have statements after a return statement")
      statements.add(statement)
    }
    return statements
  }

  private inline fun <T: Scope, U> useScope(scope: T, consumer: (T) -> U): U {
    scopeQueue.push(scope)
    val u = consumer.invoke(scope)
    scope.dispose()
    scopeQueue.pop()
    return u
  }

  private fun visit(node: TypeCstNode): JavaType = currentScope.resolveTypeOrThrow(node)

  /*
   * node visits
   */
  override fun visit(node: DoubleCstNode) = DoubleConstantNode(node.token, node.value)
  override fun visit(node: BoolCstNode) = BoolConstantNode(node.token, node.value)

  override fun visit(node: FloatCstNode) = FloatConstantNode(node.token, node.value)

  override fun visit(node: IntCstNode) = IntConstantNode(node.token, node.value)

  override fun visit(node: LongCstNode) = LongConstantNode(node.token, node.value)
  override fun visit(node: NullCstNode) = NullValueNode(node.token)
  override fun visit(node: StringCstNode) = StringConstantNode(node.value, node)
  override fun visit(node: CharCstNode) = CharConstantNode(node.token, node.value)

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
    val owner = node.ownerNode.accept(exprVisitor)
    val arguments = node.indexNodes.map { it.accept(exprVisitor) }
    return if (owner.type.isArray) {
      if (node.indexNodes.size != 1) throw MarcelSemanticException(node, "Arrays need one index")
      ArrayAccessNode(owner, caster.cast(JavaType.int, node.indexNodes.first().accept(exprVisitor)), node)
    } else {
      val getAtMethod = typeResolver.findMethodOrThrow(owner.type, if (node.isSafeAccess) GET_AT_SAFE_METHOD_NAME else GET_AT_METHOD_NAME, arguments)
      fCall(method = getAtMethod, owner = owner, arguments = arguments, node = node)
    }
  }

  override fun visit(node: TernaryCstNode): ExpressionNode {
    val testExpr = caster.truthyCast(node.testExpressionNode.accept(exprVisitor))
    val trueExpr = node.trueExpressionNode.accept(exprVisitor)
    val falseExpr = node.falseExpressionNode.accept(exprVisitor)

    // trueExpr and falseExpr need to be casted in case they return different types
    val commonType = JavaType.commonType(trueExpr, falseExpr)
    return TernaryNode(testExpr, caster.cast(commonType, trueExpr), caster.cast(commonType, falseExpr), node)
  }

  override fun visit(node: NotCstNode) = NotNode(caster.truthyCast(node.expression.accept(exprVisitor)), node)

  override fun visit(node: UnaryMinusCstNode) = MinusNode(IntConstantNode(node.token, 0), node.expression.accept(exprVisitor))

  override fun visit(node: BinaryOperatorCstNode): ExpressionNode {
    val leftOperand = node.leftOperand
    val rightOperand = node.rightOperand
    return when (node.tokenType) {
      TokenType.ASSIGNMENT -> {
        val left = leftOperand.accept(exprVisitor)
        val right = rightOperand.accept(exprVisitor)
        when (left) {
          is ReferenceNode -> {
            val variable = left.variable
            checkVariableAccess(variable, node, checkSet = true)
            VariableAssignmentNode(variable,
              caster.cast(variable.type, right), left.owner, node)
          }
          is FunctionCallNode -> {
            val owner = left.owner
            if (left.javaMethod.name != GET_AT_METHOD_NAME && left.javaMethod.name != GET_AT_SAFE_METHOD_NAME
              || owner == null)
              throw MarcelSemanticException(node, "Invalid assignment operator use")
            val arguments = left.arguments + right
            val isSafeAccess = left.javaMethod.name == GET_AT_SAFE_METHOD_NAME
            // TODO implement putAtSafe in all primitive collections and List
            val putAtMethod = typeResolver.findMethodOrThrow(owner.type, if (isSafeAccess) PUT_AT_SAFE_METHOD_NAME else PUT_AT_METHOD_NAME, arguments, node.token)
            fCall(method = putAtMethod, owner = owner, arguments = arguments, node = node)
          }
          is ArrayAccessNode -> {
            val owner = left.owner
            val elementType = owner.type.asArrayType.elementsType
            ArrayIndexAssignmentNode(owner, caster.cast(JavaType.int, left.indexNode), caster.cast(elementType, right), node)
          }
          else -> throw MarcelSemanticException(node, "Invalid assignment operator use")
        }
      }
      TokenType.PLUS -> {
        val left = leftOperand.accept(exprVisitor)
        val right = rightOperand.accept(exprVisitor)
        if (left.type == JavaType.String || right.type == JavaType.String) StringNode(listOf(left, right), node)
        else arithmeticBinaryOperator(leftOperand, rightOperand, "plus", ::PlusNode)
      }
      TokenType.ELVIS -> {
        val left = leftOperand.accept(exprVisitor)
        val right = rightOperand.accept(exprVisitor)
        val type = JavaType.commonType(left.type, right.type)
        // using DupNode to help compiler write better code than we would with a temp local variable
        ElvisNode(caster.truthyCast(DupNode(caster.cast(type, left))), caster.cast(type, right), type)
      }
      TokenType.MINUS -> arithmeticBinaryOperator(leftOperand, rightOperand, "minus", ::MinusNode)
      TokenType.MUL -> arithmeticBinaryOperator(leftOperand, rightOperand, "multiply", ::MulNode)
      TokenType.DIV -> arithmeticBinaryOperator(leftOperand, rightOperand, "div", ::DivNode)
      TokenType.MODULO -> arithmeticBinaryOperator(leftOperand, rightOperand, "mod", ::ModNode)
      TokenType.RIGHT_SHIFT -> shiftOperator(leftOperand, rightOperand, "rightShift", ::RightShiftNode)
      TokenType.LEFT_SHIFT -> shiftOperator(leftOperand, rightOperand, "leftShift", ::LeftShiftNode)
      TokenType.PLUS_ASSIGNMENT -> arithmeticAssignmentBinaryOperator(leftOperand, rightOperand, TokenType.PLUS)
      TokenType.MINUS_ASSIGNMENT -> arithmeticAssignmentBinaryOperator(leftOperand, rightOperand, TokenType.MINUS)
      TokenType.MUL_ASSIGNMENT -> arithmeticAssignmentBinaryOperator(leftOperand, rightOperand, TokenType.MUL)
      TokenType.DIV_ASSIGNMENT -> arithmeticAssignmentBinaryOperator(leftOperand, rightOperand, TokenType.DIV)
      TokenType.QUESTION_DOT -> {
        val left = leftOperand.accept(exprVisitor)
        if (left.type.primitive) throw MarcelSemanticException(node, "Cannot use safe access operator on primitive type as it cannot be null")

        var dotNode = dotOperator(node, left, rightOperand, discardOwnerInReturned = true)
        if (dotNode.type != JavaType.void && dotNode.type.primitive) dotNode = caster.cast(dotNode.type.objectType, dotNode) // needed as the result can be null

        TernaryNode(
          testExpressionNode = IsNotEqualNode(DupNode(left), NullValueNode(node.token, left.type)),
          trueExpressionNode = dotNode,
          // void because the DUP value is null in false case, so we can just use it
          // POP if return type is void as we still have a null value on the stack that won't be used as this expression is supposed to return nothing, void
          falseExpressionNode = if (dotNode.type != JavaType.void) VoidExpressionNode(node.token) else PopNode(left.type, node),
          node = node
        )
      }
      TokenType.DOT -> dotOperator(node, node.leftOperand.accept(exprVisitor), rightOperand)
      TokenType.TWO_DOTS -> rangeNode(leftOperand, rightOperand, "of")
      TokenType.TWO_DOTS_END_EXCLUSIVE -> rangeNode(leftOperand, rightOperand, "ofToExclusive")
      TokenType.AND -> AndNode(caster.truthyCast(leftOperand.accept(exprVisitor)), caster.truthyCast(rightOperand.accept(exprVisitor)))
      TokenType.OR -> OrNode(caster.truthyCast(leftOperand.accept(exprVisitor)), caster.truthyCast(rightOperand.accept(exprVisitor)))
      TokenType.EQUAL -> comparisonOperatorNode(leftOperand, rightOperand, ::IsEqualNode) { left, right ->
        val method = typeResolver.findMethodOrThrow(BytecodeHelper::class.javaType, "objectsEqual", listOf(JavaType.Object, JavaType.Object))
        fCall(node = node, method = method, arguments = listOf(left, right))
      }
      TokenType.NOT_EQUAL -> comparisonOperatorNode(leftOperand, rightOperand, ::IsNotEqualNode) { left, right ->
        val method = typeResolver.findMethodOrThrow(BytecodeHelper::class.javaType, "objectsEqual", listOf(JavaType.Object, JavaType.Object))
        NotNode(fCall(node = node, method = method, arguments = listOf(left, right)), node)
      }
      TokenType.GOE -> numberComparisonOperatorNode(leftOperand, rightOperand, ::GeNode) { left, right ->
        TODO("compareTo comparison")
      }
      TokenType.GT -> numberComparisonOperatorNode(leftOperand, rightOperand, ::GtNode) { left, right ->
        TODO("compareTo comparison")
      }
      TokenType.LOE -> numberComparisonOperatorNode(leftOperand, rightOperand, ::LeNode) { left, right ->
        TODO("compareTo comparison")
      }
      TokenType.LT -> numberComparisonOperatorNode(leftOperand, rightOperand, ::LtNode) { left, right ->
        TODO("compareTo comparison")
      }
      TokenType.IS -> {
        val left = leftOperand.accept(exprVisitor)
        val right = rightOperand.accept(exprVisitor)
        if (left.type.primitive || right.type.primitive) throw MarcelSemanticException(leftOperand, "=== operator is reserved for object comparison")
        IsEqualNode(left, right)
      }
      TokenType.IS_NOT -> {
        val left = leftOperand.accept(exprVisitor)
        val right = rightOperand.accept(exprVisitor)
        if (left.type.primitive || right.type.primitive) throw MarcelSemanticException(leftOperand, "=== operator is reserved for object comparison")
        IsNotEqualNode(left, right)
      }
      else -> throw MarcelSemanticException(node, "Doesn't handle operator ${node.tokenType}")
    }
  }

  private fun dotOperator(node: CstNode, owner: ExpressionNode, rightOperand: CstExpressionNode,
                          // useful for ternaryNode which duplicate value to avoid using local variable
                          discardOwnerInReturned: Boolean = false): ExpressionNode = when (rightOperand) {
    is FunctionCallCstNode -> {
      val arguments = getArguments(rightOperand)
      val method = typeResolver.findMethodOrThrow(owner.type, rightOperand.value, arguments, node.token)
      val castType = if (rightOperand.castType != null) visit(rightOperand.castType!!) else null
      fCall(method = method, owner = if (discardOwnerInReturned) null else owner, castType = castType,
        arguments = arguments, node = node)
    }
    is ReferenceCstNode -> {
      val variable = typeResolver.findFieldOrThrow(owner.type, rightOperand.value, rightOperand.token)
      checkVariableAccess(variable, node)
      ReferenceNode(if (discardOwnerInReturned) null else owner, variable, rightOperand.token)
    }
    else -> throw MarcelSemanticException(node, "Invalid dot operator use")
  }

  override fun visit(node: BinaryTypeOperatorCstNode): ExpressionNode {
    val left = node.leftOperand.accept(exprVisitor)
    val right = visit(node.rightOperand)


    return when(val tokenType = node.tokenType) {
      TokenType.AS -> caster.cast(right, left)
      TokenType.INSTANCEOF, TokenType.NOT_INSTANCEOF -> {
        if (left.type.primitive || right.primitive) throw MarcelSemanticException(left.token, "Primitive aren't instance of anything")
        val instanceOfNode = InstanceOfNode(right, left, node)
        // TODO document !instanceof
        if (tokenType == TokenType.NOT_INSTANCEOF) NotNode(instanceOfNode, node) else instanceOfNode
      }
      else -> throw MarcelSemanticException(node, "Doesn't handle operator ${node.tokenType}")
    }
  }
  private fun comparisonOperatorNode(
    leftOperand: CstExpressionNode,
    rightOperand: CstExpressionNode,
    nodeCreator: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode,
    objectComparisonNodeCreator: (ExpressionNode, ExpressionNode) -> ExpressionNode): ExpressionNode {
    val left = leftOperand.accept(exprVisitor)
    val right = rightOperand.accept(exprVisitor)

    // TODO can do better, e.g. if is object but object of primitive, comparison is still feasible
    return if (left.type.primitive && right.type.primitive) {
      val type = if (left.type != JavaType.int) right.type else left.type
      nodeCreator.invoke(caster.cast(type, left), caster.cast(type, right))
    } else objectComparisonNodeCreator.invoke(caster.cast(JavaType.Object, left), caster.cast(JavaType.Object, right))
  }

  private fun numberComparisonOperatorNode(
    leftOperand: CstExpressionNode,
    rightOperand: CstExpressionNode,
    nodeCreator: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode,
    objectComparisonNodeCreator: (ExpressionNode, ExpressionNode) -> ExpressionNode): ExpressionNode {
    val left = leftOperand.accept(exprVisitor)
    val right = rightOperand.accept(exprVisitor)

    if (left.type == JavaType.boolean || right.type == JavaType.boolean) {
      throw MarcelSemanticException(leftOperand, "Cannot compare non number primitives")
    }
    return comparisonOperatorNode(leftOperand, rightOperand, nodeCreator, objectComparisonNodeCreator)
  }

  private fun rangeNode(leftOperand: CstExpressionNode, rightOperand: CstExpressionNode, methodName: String): ExpressionNode {
    val left = leftOperand.accept(exprVisitor)
    val right = rightOperand.accept(exprVisitor)

    val rangeElementType = if (left.type == JavaType.Long || left.type == JavaType.long || right.type == JavaType.Long || right.type == JavaType.long) JavaType.long
    else if (left.type == JavaType.Integer || left.type == JavaType.int || right.type == JavaType.Integer || right.type == JavaType.int) JavaType.int
    else throw MarcelSemanticException(leftOperand, "Ranges can only be of int or long")

    val rangeType = if (rangeElementType == JavaType.long) LongRanges::class.javaType else IntRanges::class.javaType

    val method = typeResolver.findMethodOrThrow(rangeType, methodName, listOf(rangeElementType, rangeElementType))
    return fCall(method = method, arguments = listOf(left, right), node = leftOperand)
  }

  private fun shiftOperator(leftOperand: CstExpressionNode, rightOperand: CstExpressionNode,
                            operatorMethodName: String,
                            nodeSupplier: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode): ExpressionNode {
    val left = leftOperand.accept(exprVisitor)
    val right = rightOperand.accept(exprVisitor)
    val node = arithmeticBinaryOperator(left, right, operatorMethodName, nodeSupplier)
    if (JavaType.commonType(left, right).isPrimitiveOrObjectPrimitive && node.type.primitive && node.type != JavaType.long && node.type != JavaType.int) {
      throw MarcelSemanticException(node.token, "Can only shift ints or longs")
    }
    return node
  }
  private inline fun arithmeticBinaryOperator(leftOperand: CstExpressionNode, rightOperand: CstExpressionNode,
                                       operatorMethodName: String,
                                       nodeSupplier: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode)
  = arithmeticBinaryOperator(leftOperand.accept(exprVisitor), rightOperand.accept(exprVisitor), operatorMethodName, nodeSupplier)

  private fun arithmeticAssignmentBinaryOperator(leftOperand: CstExpressionNode, rightOperand: CstExpressionNode,
                                                        tokenType: TokenType): ExpressionNode {
    return visit(BinaryOperatorCstNode(TokenType.ASSIGNMENT,
      leftOperand = leftOperand,
      rightOperand = BinaryOperatorCstNode(tokenType, leftOperand, rightOperand, leftOperand.parent, leftOperand.tokenStart, rightOperand.tokenEnd),
      leftOperand.parent, leftOperand.tokenStart, rightOperand.tokenEnd))
  }

  private inline fun arithmeticBinaryOperator(left: ExpressionNode, right: ExpressionNode,
                                       operatorMethodName: String,
                                       nodeSupplier: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode): ExpressionNode {
    val commonType = JavaType.commonType(left, right)
    return if (commonType.isPrimitiveOrObjectPrimitive) {
      val commonPrimitiveType = commonType.asPrimitiveType
      if (!commonPrimitiveType.isNumber) throw MarcelSemanticException("Cannot apply operator on non number types")
      nodeSupplier.invoke(caster.cast(commonPrimitiveType, left), caster.cast(commonPrimitiveType, right))
    } else {
      val arguments = listOf(right)
      val method = typeResolver.findMethodOrThrow(left.type, operatorMethodName, arguments, left.token)
      fCall(method = method, owner = left, castType = null, arguments = arguments, token = left.token)
    }
  }

  override fun visit(node: ReferenceCstNode): ExpressionNode {
    val localVariable = currentMethodScope.findLocalVariable(node.value)
    if (localVariable != null) {
      return ReferenceNode(null, localVariable, node.token)
    }
    val field = currentScope.findFieldOrThrow(node.value, node.token)
    // TODO this check should not be here as this node can appear to be used for a set.
    //  remove this check here, create a CanGetVisitor and CanSetVisitor that we'll use only
    //  when we're sure of what to do with the expression
    checkVariableAccess(field, node, checkGet = true)
    val owner = if (!field.isStatic) ThisReferenceNode(currentScope.classType, node.token) else null
    return ReferenceNode(owner, field, node.token)
  }

  override fun visit(node: FunctionCallCstNode): ExpressionNode {
    val arguments = getArguments(node)
    val method = currentScope.findMethodOrThrow(node.value, arguments, node)
    val castType = if (node.castType != null) visit(node.castType!!) else null
    val owner = if (method.isStatic) null else ThisReferenceNode(currentScope.classType, node.token)
    return fCall(
      node = node,
      method = method,
      owner = owner,
      castType = castType,
      arguments = arguments)
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
    return fCall(node = node,
      method = method,
      owner =  SuperReferenceNode(currentScope.classType.superType!!, node.token),
      arguments = arguments)
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
          ?: variable.type.getDefaultValueExpression(node.token), null, node.tokenStart, node.tokenEnd)
    )
  }

  override fun visit(node: IfCstStatementNode) = useScope(MethodInnerScope(currentMethodScope)) {
    IfStatementNode(caster.truthyCast(node.condition.accept(exprVisitor)),
      node.trueStatementNode.accept(stmtVisitor),
      node.falseStatementNode?.accept(stmtVisitor), node)
  }

  override fun visit(node: ForInCstNode) = useScope(MethodInnerScope(currentMethodScope)) {
    val variable = it.addLocalVariable(visit(node.varType), node.varName)

    val inNode = node.inNode.accept(exprVisitor)

    val iteratorExpression = when {
      // TODO charsequence iterator
      inNode.type.implements(Iterable::class.javaType) -> fCall(node, inNode.type, "iterator", emptyList(), inNode)
      inNode.type.implements(Iterator::class.javaType) -> inNode
      else -> throw MarcelSemanticException(node.token, "Cannot iterate over an expression of type ${inNode.type}")
    }
    val iteratorExpressionType = iteratorExpression.type
    it.useTempLocalVariable(iteratorExpressionType) { iteratorVariable ->
      val (nextMethodOwnerType, nextMethodName) = if (IntIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(IntIterator::class.javaType, "nextInt")
      else if (LongIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(LongIterator::class.javaType, "nextLong")
      else if (FloatIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(FloatIterator::class.javaType, "nextFloat")
      else if (DoubleIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(DoubleIterator::class.javaType, "nextDouble")
      else if (CharacterIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(CharacterIterator::class.javaType, "nextCharacter")
      else if (Iterator::class.javaType.isAssignableFrom(iteratorExpressionType)) Pair(Iterator::class.javaType, "next")
      else throw UnsupportedOperationException("wtf")

      val iteratorVarReference = ReferenceNode(owner = null, iteratorVariable, node.token)

      val nextMethod = typeResolver.findMethodOrThrow(nextMethodOwnerType, nextMethodName, emptyList())
      // cast to fit the declared variable type
      val nextMethodCall = caster.cast(variable.type, fCall(node = node, method = nextMethod, arguments = emptyList(), owner = iteratorVarReference))
      ForInIteratorStatementNode(node, variable, iteratorVariable, iteratorExpression, nextMethodCall, node.statementNode.accept(stmtVisitor))
    }
  }

  override fun visit(node: ForVarCstNode): StatementNode {
    TODO("Not yet implemented")
  }

  override fun visit(node: BlockCstNode) = useScope(MethodInnerScope(currentMethodScope)) {
    val statements = blockStatements(node.statements)
    BlockStatementNode(statements, node.tokenStart, node.tokenEnd)
  }

  private fun fCall(node: CstNode, ownerType: JavaType, name: String, arguments: List<ExpressionNode>,
                    owner: ExpressionNode? = null,
                    castType: JavaType? = null): FunctionCallNode {
    val method = typeResolver.findMethodOrThrow(ownerType, name, arguments, node.token)
    return fCall(node, method, arguments, owner, castType)
  }

  private fun fCall(
    node: CstNode,
    method: JavaMethod,
    arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null) = fCall(node.token, method, arguments, owner, castType)
  private fun fCall(
    token: LexToken,
    method: JavaMethod,
    arguments: List<ExpressionNode>,
    owner: ExpressionNode? = null,
    castType: JavaType? = null) = FunctionCallNode(method, owner, castType, castedArguments(method, arguments), token)

  private fun checkVariableAccess(variable: Variable, node: CstNode, checkGet: Boolean = false, checkSet: Boolean = false) {
    if (checkGet && !variable.isVisibleFrom(currentScope.classType, Variable.Access.GET)
      || checkSet && !variable.isVisibleFrom(currentScope.classType, Variable.Access.SET)) {
      throw MarcelSemanticException(node, "Cannot access variable ${variable.name} from ${currentScope.classType}")
    }
    if (checkGet && !variable.isGettable) {
      throw MarcelSemanticException(node, "Cannot get value of variable ${variable.name}")
    }
    if (checkSet && !variable.isSettable) {
      throw MarcelSemanticException(node, "Cannot set value for variable ${variable.name}")
    }
  }

  private fun toJavaMethod(ownerType: JavaType, node: MethodCstNode): JavaMethod {
    return JavaMethodImpl(ownerType, Visibility.fromTokenType(node.accessNode.visibility), node.name,
      node.parameters.map(this::toMethodParameter), visit(node.returnTypeCstNode), false, false, false, false)
  }

  private fun toJavaConstructor(ownerType: JavaType, node: ConstructorCstNode): JavaMethod {
    return JavaConstructorImpl(Visibility.fromTokenType(node.accessNode.visibility), ownerType, node.parameters.map(this::toMethodParameter))
  }

  private fun toMarcelField(ownerType: JavaType, fieldNode: FieldCstNode): MarcelField {
   return JavaClassFieldImpl(visit(fieldNode.type), fieldNode.name, ownerType, fieldNode.access.isFinal,
      Visibility.fromTokenType(fieldNode.access.visibility), fieldNode.access.isStatic)
  }

  private fun toMethodParameter(node: MethodParameterCstNode) =
    // TODO doesn't handle thisParameter
    MethodParameter(visit(node.type), node.name, node.annotations.map { annotationNode(it, ElementType.PARAMETER) }, node.defaultValue?.accept(exprVisitor))
}