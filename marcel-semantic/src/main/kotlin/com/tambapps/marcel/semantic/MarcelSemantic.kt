package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.AbstractMethodCstNode
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
import com.tambapps.marcel.parser.cst.expression.TemplateStringCstNode
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
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.NotCstNode
import com.tambapps.marcel.parser.cst.expression.SwitchCstNode
import com.tambapps.marcel.parser.cst.expression.TernaryCstNode
import com.tambapps.marcel.parser.cst.expression.TruthyVariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusCstNode
import com.tambapps.marcel.parser.cst.expression.WhenCstNode
import com.tambapps.marcel.parser.cst.expression.literal.BoolCstNode
import com.tambapps.marcel.parser.cst.expression.literal.CharCstNode
import com.tambapps.marcel.parser.cst.expression.literal.RegexCstNode
import com.tambapps.marcel.parser.cst.imprt.ImportCstVisitor
import com.tambapps.marcel.parser.cst.imprt.SimpleImportCstNode
import com.tambapps.marcel.parser.cst.imprt.StaticImportCstNode
import com.tambapps.marcel.parser.cst.imprt.WildcardImportCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.BreakCstNode
import com.tambapps.marcel.parser.cst.statement.ContinueCstNode
import com.tambapps.marcel.parser.cst.statement.ForInCstNode
import com.tambapps.marcel.parser.cst.statement.ForVarCstNode
import com.tambapps.marcel.parser.cst.statement.IfCstStatementNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.ThrowCstNode
import com.tambapps.marcel.parser.cst.statement.TryCatchCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.WhileCstNode
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstVariableNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.ast.SimpleImportNode
import com.tambapps.marcel.semantic.ast.StaticImportNode
import com.tambapps.marcel.semantic.ast.WildcardImportNode
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
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.TernaryNode
import com.tambapps.marcel.semantic.ast.expression.ThisConstructorCallNode
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
import com.tambapps.marcel.semantic.ast.expression.operator.IncrLocalVariableNode
import com.tambapps.marcel.semantic.ast.expression.operator.IncrNode
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
import com.tambapps.marcel.semantic.ast.statement.BreakNode
import com.tambapps.marcel.semantic.ast.statement.CatchNode
import com.tambapps.marcel.semantic.ast.statement.ContinueNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.ast.statement.TryCatchNode
import com.tambapps.marcel.semantic.ast.statement.WhileNode
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
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.Variable
import com.tambapps.marcel.semantic.variable.field.JavaClassFieldImpl
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.visitor.AllPathsReturnVisitor
import com.tambapps.marcel.semantic.visitor.ReturningWhenIfBranchTransformer
import marcel.lang.DelegatedObject
import marcel.lang.IntRanges
import marcel.lang.LongRanges
import marcel.lang.Script
import marcel.lang.compile.BooleanDefaultValue
import marcel.lang.compile.CharacterDefaultValue
import marcel.lang.compile.DoubleDefaultValue
import marcel.lang.compile.FloatDefaultValue
import marcel.lang.compile.IntDefaultValue
import marcel.lang.compile.LongDefaultValue
import marcel.lang.compile.NullDefaultValue
import marcel.lang.compile.StringDefaultValue
import marcel.lang.primitives.iterators.CharacterIterator
import marcel.lang.primitives.iterators.DoubleIterator
import marcel.lang.primitives.iterators.FloatIterator
import marcel.lang.primitives.iterators.IntIterator
import marcel.lang.primitives.iterators.LongIterator
import marcel.lang.runtime.BytecodeHelper
import java.io.Closeable
import java.lang.annotation.ElementType
import java.util.LinkedList
import java.util.regex.Pattern

// TODO implement multiple errors like in parser2
class MarcelSemantic(
  private val typeResolver: JavaTypeResolver,
  private val cst: SourceFileCstNode
): ExpressionCstNodeVisitor<ExpressionNode, JavaType>, StatementCstNodeVisitor<StatementNode>, ImportCstVisitor<ImportNode> {

  companion object {
    const val PUT_AT_METHOD_NAME = "putAt"
    const val PUT_AT_SAFE_METHOD_NAME = "putAtSafe"
    const val GET_AT_METHOD_NAME = "getAt"
    const val GET_AT_SAFE_METHOD_NAME = "getAtSafe"
  }
  private val caster = AstNodeCaster(typeResolver)

  internal val scopeQueue = LinkedList<Scope>()
  private val classNodeMap = mutableMapOf<JavaType, ClassNode>() // useful to add methods while performing analysis

  private val imports = Scope.DEFAULT_IMPORTS.toMutableList() // will be updated while performing analysis
  private val methodResolver = MethodResolver(typeResolver, caster, imports)
  private val currentScope get() = scopeQueue.peek() // FIFO
  private val currentMethodScope get() = currentScope as? MethodScope ?: throw MarcelSemanticException("Not in a method")
  private val currentInnerMethodScope get() = currentScope as? MethodInnerScope ?: throw MarcelSemanticException("Not in a inner scope")

  fun apply(): ModuleNode {
    imports.addAll(
      cst.imports.map { it.accept(this) }
    )

    val moduleNode = ModuleNode(cst.tokenStart, cst.tokenEnd)
    val scriptCstNode = cst.script

    scopeQueue.push(ImportScope(typeResolver, imports))
    // define everything
    cst.classes.forEach { defineClass(it) }

    if (scriptCstNode != null) {
      if (scriptCstNode.constructors.isNotEmpty()) {
        throw MarcelSemanticException(scriptCstNode.constructors.first().token, "Cannot define constructors for scripts")
      }
      val classType = typeResolver.defineClass(scriptCstNode.tokenStart, Visibility.PUBLIC,
        if (cst.packageName != null) "${cst.packageName}.${scriptCstNode.className}" else scriptCstNode.className,
        Script::class.javaType, false, emptyList())
      // register script class members
      defineClassMembers(scriptCstNode, classType)
      val scriptNode = classNode(classType, scriptCstNode)
      useScope(ClassScope(classType, typeResolver, imports)) {
        // add the run method
        val runMethod = SemanticHelper.scriptRunMethod(classType, cst)
        fillMethodNode(it, runMethod, scriptCstNode.runMethodStatements, emptyList(),  scriptRunMethod = true)
        scriptNode.methods.add(runMethod)
      }
      moduleNode.classes.add(scriptNode)
    }

    for (cstClass in cst.classes) {
      moduleNode.classes.add(classNode(typeResolver.of(cstClass.className, emptyList()), cstClass))
    }

    return moduleNode
  }

  private fun defineClass(classCstNode: ClassCstNode) {
    val superType = classCstNode.superType?.let { visit(it) } ?: JavaType.Object
    val interfaces = classCstNode.interfaces.map { visit(it) }
    val classType = typeResolver.defineClass(classCstNode.tokenStart, Visibility.fromTokenType(classCstNode.access.visibility),
      classCstNode.className, superType, false, interfaces)
    defineClassMembers(classCstNode, classType)
  }

  private fun defineClassMembers(classCstNode: ClassCstNode, classType: JavaType) {
    classCstNode.methods.forEach { typeResolver.defineMethod(classType, toJavaMethod(classType, it)) }
    classCstNode.constructors.forEach { typeResolver.defineMethod(classType, toJavaConstructor(classType, it)) }
    classCstNode.fields.forEach { typeResolver.defineField(classType, toMarcelField(classType, it)) }
    classCstNode.innerClasses.forEach { defineClass(it) }
  }

  private fun classNode(classType: JavaType, node: ClassCstNode): ClassNode
  = useScope(ClassScope(classType, typeResolver, imports)) { classScope ->
    val classNode = ClassNode(classType, Visibility.PUBLIC, cst.tokenStart, cst.tokenEnd)
    classNodeMap[classType] = classNode

    node.annotations.forEach { classNode.annotations.add(annotationNode(it, ElementType.TYPE)) }
    // iterating with i because we might add methods while
    node.methods.forEach { classNode.methods.add(methodNode(it, classScope)) }
    node.constructors.forEach { classNode.methods.add(constructorNode(it, classScope)) }
    val staticFieldInitialValueMap = mutableMapOf<FieldNode, ExpressionNode>()
    val fieldInitialValueMap = mutableMapOf<FieldNode, ExpressionNode>()
    node.fields.forEach { cstFieldNode ->
      val fieldNode = FieldNode(visit(cstFieldNode.type), cstFieldNode.name, classType,
        cstFieldNode.annotations.map { annotationNode(it, ElementType.FIELD) },
        cstFieldNode.access.isFinal, Visibility.fromTokenType(cstFieldNode.access.visibility),
        cstFieldNode.access.isStatic, cstFieldNode.tokenStart, cstFieldNode.tokenEnd)
      classNode.fields.add(fieldNode)

      if (cstFieldNode.initialValue != null) {
        if (fieldNode.isStatic) {
          val stInitMethod = classNode.getOrCreateStaticInitialisationMethod()
          useScope(MethodScope(classScope, stInitMethod)) {
            staticFieldInitialValueMap[fieldNode] = cstFieldNode.initialValue!!.accept(this, fieldNode.type)
          }
        } else {
          fieldInitialValueMap[fieldNode] = caster.cast(fieldNode.type, cstFieldNode.initialValue!!.accept(this, fieldNode.type))
        }
      }
    }

    node.innerClasses.forEach {
      classNode.innerClasses.add(classNode(typeResolver.of(it.className, emptyList()), it))
    }

    if (classNode.constructorCount == 0) {
      // default no arg constructor
      classNode.methods.add(SemanticHelper.noArgConstructor(classNode, typeResolver))
    }

    if (fieldInitialValueMap.isNotEmpty()) {
      val fieldAssignmentStatements = toFieldAssignmentStatements(classType, fieldInitialValueMap, false)
      for (constructorNode in classNode.constructors) {
        // add at one because the first statement is the super call
        constructorNode.blockStatement.statements.addAll(1,
          fieldAssignmentStatements
        )
      }
    }
    if (staticFieldInitialValueMap.isNotEmpty()) {
      val staticInitMethod = classNode.getOrCreateStaticInitialisationMethod()
      staticInitMethod.blockStatement.statements.addAll(0,
        toFieldAssignmentStatements(classType, staticFieldInitialValueMap, true)
      )
    }
    return classNode
  }

  private fun toFieldAssignmentStatements(classType: JavaType, map: Map<FieldNode, ExpressionNode>, isStaticContext: Boolean): List<ExpressionStatementNode> {
    return map.map { entry ->
      val field = entry.key
      val initialValue = entry.value
      ExpressionStatementNode(VariableAssignmentNode(variable = field,
        owner = if (isStaticContext) null else ThisReferenceNode(classType, field.token),
        expression = caster.cast(field.type, initialValue),
        tokenStart = field.tokenStart, tokenEnd = initialValue.tokenEnd))
    }

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

  private fun annotationAttribute(node: AnnotationCstNode, javaAnnotation: JavaAnnotation, specifiedAttr: Pair<String, ExpressionCstNode>): AnnotationNode.AttributeNode {
    val attribute = javaAnnotation.attributes.find { it.name == specifiedAttr.first }
      ?: throw MarcelSemanticException(node.token, "Unknown member ${specifiedAttr.first} for annotation $javaAnnotation")
    val specifiedValueNode = specifiedAttr.second.accept(this, attribute.type)
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
    val methodNode = toMethodNode(methodCst, methodCst.name, visit(methodCst.returnTypeCstNode), classScope)
    fillMethodNode(classScope, methodNode, methodCst.statements, methodCst.annotations)
    return methodNode
  }

  private fun constructorNode(methodCst: ConstructorCstNode, classScope: ClassScope): MethodNode {
    val methodNode = toMethodNode(methodCst, JavaMethod.CONSTRUCTOR_NAME, JavaType.void, classScope)
    fillMethodNode(classScope, methodNode, methodCst.statements, methodCst.annotations)
    val firstStatement = methodNode.blockStatement.statements.firstOrNull()
    if (firstStatement == null || firstStatement !is ExpressionStatementNode
      || firstStatement.expressionNode !is ThisConstructorCallNode && firstStatement.expressionNode !is SuperConstructorCallNode) {
      val superType = classScope.classType.superType!!
      val superConstructorMethod = typeResolver.findMethod(superType, JavaMethod.CONSTRUCTOR_NAME, emptyList())
        ?: throw MarcelSemanticException(methodNode.token, "Class $superType doesn't have a no-arg constructor")
      methodNode.blockStatement.statements.add(0,
        ExpressionStatementNode(SuperConstructorCallNode(superType, superConstructorMethod, emptyList(), methodNode.tokenStart, methodNode.tokenEnd)))
    }
    // TODO check for this/super constructor calls that aren't the first statement and throw error if any
    //  also check recursive constructor calls (see RecursiveConstructorCheck class)
    return methodNode
  }

  private fun toMethodNode(methodCst: AbstractMethodCstNode, methodName: String, returnType: JavaType, classScope: ClassScope): MethodNode {
    val visibility = Visibility.fromTokenType(methodCst.accessNode.visibility)
    val isStatic = methodCst.accessNode.isStatic
    return MethodNode(
      name = methodName,
      visibility = visibility,
      returnType = returnType,
      isStatic = isStatic,
      tokenStart = methodCst.tokenStart,
      tokenEnd = methodCst.tokenEnd,
      parameters = methodCst.parameters.mapIndexed { index, methodParameterCstNode ->
        toMethodParameterNode(classScope.classType, visibility, isStatic, index, methodName, methodParameterCstNode) },
      ownerClass = classScope.classType
    )
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
      val statement = cstStatements[i].accept(this)
      if (statement is ReturnStatementNode && i < cstStatements.lastIndex)
        throw MarcelSemanticException("Cannot have statements after a return statement")
      statements.add(statement)
    }
    return statements
  }

  private fun newInnerScope() = MethodInnerScope(currentMethodScope)
  private inline fun <U> useInnerScope(consumer: (MethodInnerScope) -> U)
  = useScope(newInnerScope(), consumer)

  private inline fun <T: Scope, U> useScope(scope: T, consumer: (T) -> U): U {
    scopeQueue.push(scope)
    val u = consumer.invoke(scope)
    scope.dispose()
    scopeQueue.pop()
    return u
  }

  private fun newMethodScope(classType: JavaType, method: JavaMethod) = MethodScope(ClassScope(classType, typeResolver, imports), method)
  private fun visit(node: TypeCstNode): JavaType = currentScope.resolveTypeOrThrow(node)

  /*
   * node visits
   */
  override fun visit(node: DoubleCstNode, smartCastType: JavaType?) = DoubleConstantNode(node.token, node.value)
  override fun visit(node: BoolCstNode, smartCastType: JavaType?) = BoolConstantNode(node.token, node.value)

  override fun visit(node: FloatCstNode, smartCastType: JavaType?) = FloatConstantNode(node.token, node.value)

  override fun visit(node: IntCstNode, smartCastType: JavaType?) = IntConstantNode(node.token, node.value)

  override fun visit(node: LongCstNode, smartCastType: JavaType?) = LongConstantNode(node.token, node.value)
  override fun visit(node: NullCstNode, smartCastType: JavaType?) = NullValueNode(node.token)
  override fun visit(node: StringCstNode, smartCastType: JavaType?) = StringConstantNode(node.value, node)

  override fun visit(node: RegexCstNode, smartCastType: JavaType?): ExpressionNode {
    val arguments =
      if (node.flags.isNotEmpty()) listOf(StringConstantNode(node.value, node), IntConstantNode(node.token, node.flags.reduce { acc, i -> acc or i }))
    else listOf(StringConstantNode(node.value, node))
    return fCall(node = node, arguments = arguments, ownerType = Pattern::class.javaType, name = "compile")
  }

  override fun visit(node: CharCstNode, smartCastType: JavaType?) = CharConstantNode(node.token, node.value)

  override fun visit(node: TemplateStringCstNode, smartCastType: JavaType?): ExpressionNode {
    val expressions = node.expressions.map { it.accept(this) }
    return if (expressions.isEmpty()) StringConstantNode("", node)
    else if (expressions.size == 1 && expressions.first() is StringConstantNode) expressions.first()
    else StringNode(expressions, node)
  }

  override fun visit(node: ClassReferenceCstNode, smartCastType: JavaType?) = ClassReferenceNode(visit(node.type), node.token)
  override fun visit(node: ThisReferenceCstNode, smartCastType: JavaType?): ExpressionNode {
    if (currentMethodScope.staticContext) throw MarcelSemanticException(node, "Cannot reference this in a static context")
    return ThisReferenceNode(currentScope.classType, node.token)
  }

  override fun visit(node: SuperReferenceCstNode, smartCastType: JavaType?) = SuperReferenceNode(currentScope.classType.superType!!, node.token)

  override fun visit(node: NewInstanceCstNode, smartCastType: JavaType?): ExpressionNode {
    val type = visit(node.type)
    val (constructorMethod, arguments) = methodResolver.resolveMethodOrThrow(node, type,
      JavaMethod.CONSTRUCTOR_NAME, node.positionalArgumentNodes.map { it.accept(this) },
      node.namedArgumentNodes.map { Pair(it.first, it.second.accept(this)) })
    return NewInstanceNode(type, constructorMethod, castedArguments(constructorMethod, arguments), node.token)
  }

  override fun visit(node: DirectFieldReferenceCstNode, smartCastType: JavaType?): ExpressionNode {
    val field = currentScope.findFieldOrThrow(node.value, node.token)
    val owner = if (!field.isStatic) ThisReferenceNode(currentScope.classType, node.token) else null
    return ReferenceNode(owner = owner, variable = field, token = node.token)
  }

  override fun visit(node: ArrayCstNode, smartCastType: JavaType?) = ArrayNode(
    elements = node.elements.map { it.accept(this) }.toMutableList(),
    node = node
  )

  override fun visit(node: MapCstNode, smartCastType: JavaType?) = MapNode(
    entries = node.entries.map { Pair(
      // need objects (not primitive) to call function Map.put(key, value)
      caster.cast(JavaType.Object, it.first.accept(this)),
      caster.cast(JavaType.Object, it.second.accept(this))) },
    node = node
  )

  override fun visit(node: IncrCstNode, smartCastType: JavaType?): ExpressionNode {
    val (variable, owner) = findVariableAndOwner(node.value, node)
    val varType = variable.type
    if (varType != JavaType.int && varType != JavaType.long && varType != JavaType.float && varType != JavaType.double
      && varType != JavaType.short && varType != JavaType.byte) {
      throw MarcelSemanticException(node, "Can only increment primitive number variables")
    }
    checkVariableAccess(variable, node, checkGet = true, checkSet = true)
    return if (variable is LocalVariable) IncrLocalVariableNode(node, variable, node.amount, node.returnValueBefore)
     else {
      val incrExpression = PlusNode(ReferenceNode(
        owner = owner,
        variable = variable,
        token = node.token
      ),
        caster.cast(varType.type, IntConstantNode(value = node.amount, token = node.token)))
      IncrNode(node, variable, owner, incrExpression, node.returnValueBefore)
    }
  }

  override fun visit(node: IndexAccessCstNode, smartCastType: JavaType?): ExpressionNode {
    val owner = node.ownerNode.accept(this)
    val arguments = node.indexNodes.map { it.accept(this) }
    return if (owner.type.isArray) {
      if (node.indexNodes.size != 1) throw MarcelSemanticException(node, "Arrays need one index")
      ArrayAccessNode(owner, caster.cast(JavaType.int, node.indexNodes.first().accept(this, JavaType.int)), node)
    } else {
      val getAtMethod = typeResolver.findMethodOrThrow(owner.type, if (node.isSafeAccess) GET_AT_SAFE_METHOD_NAME else GET_AT_METHOD_NAME, arguments)
      fCall(method = getAtMethod, owner = owner, arguments = arguments, node = node)
    }
  }

  override fun visit(node: TernaryCstNode, smartCastType: JavaType?): ExpressionNode {
    val testExpr = caster.truthyCast(node.testExpressionNode.accept(this))
    val trueExpr = node.trueExpressionNode.accept(this)
    val falseExpr = node.falseExpressionNode.accept(this)

    // trueExpr and falseExpr need to be casted in case they return different types
    val commonType = JavaType.commonType(trueExpr, falseExpr)
    return TernaryNode(testExpr, caster.cast(commonType, trueExpr), caster.cast(commonType, falseExpr), node)
  }

  override fun visit(node: NotCstNode, smartCastType: JavaType?) = NotNode(caster.truthyCast(node.expression.accept(
    this,
  )), node)

  override fun visit(node: UnaryMinusCstNode, smartCastType: JavaType?) = MinusNode(IntConstantNode(node.token, 0), node.expression.accept(
    this,
  ))

  override fun visit(node: BinaryOperatorCstNode, smartCastType: JavaType?): ExpressionNode {
    val leftOperand = node.leftOperand
    val rightOperand = node.rightOperand
    return when (node.tokenType) {
      TokenType.ASSIGNMENT -> {
        val left = leftOperand.accept(this)
        val right = rightOperand.accept(this, left.type)
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
        val left = leftOperand.accept(this)
        val right = rightOperand.accept(this)
        if (left.type == JavaType.String || right.type == JavaType.String) StringNode(listOf(left, right), node)
        else arithmeticBinaryOperator(leftOperand, rightOperand, "plus", ::PlusNode)
      }
      TokenType.ELVIS -> {
        val left = leftOperand.accept(this)
        val right = rightOperand.accept(this)
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
        val left = leftOperand.accept(this)
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
      TokenType.DOT -> dotOperator(node, node.leftOperand.accept(this), rightOperand)
      TokenType.TWO_DOTS -> rangeNode(leftOperand, rightOperand, "of")
      TokenType.TWO_DOTS_END_EXCLUSIVE -> rangeNode(leftOperand, rightOperand, "ofToExclusive")
      TokenType.AND -> AndNode(caster.truthyCast(leftOperand.accept(this)), caster.truthyCast(rightOperand.accept(
        this,
      )))
      TokenType.OR -> OrNode(caster.truthyCast(leftOperand.accept(this)), caster.truthyCast(rightOperand.accept(
        this,
      )))
      TokenType.EQUAL -> equalityComparisonOperatorNode(leftOperand, rightOperand, ::IsEqualNode) { left, right ->
        val method = typeResolver.findMethodOrThrow(BytecodeHelper::class.javaType, "objectsEqual", listOf(JavaType.Object, JavaType.Object))
        fCall(node = node, method = method, arguments = listOf(left, right))
      }
      TokenType.NOT_EQUAL -> equalityComparisonOperatorNode(leftOperand, rightOperand, ::IsNotEqualNode) { left, right ->
        val method = typeResolver.findMethodOrThrow(BytecodeHelper::class.javaType, "objectsEqual", listOf(JavaType.Object, JavaType.Object))
        NotNode(fCall(node = node, method = method, arguments = listOf(left, right)), node)
      }
      TokenType.GOE -> numberComparisonOperatorNode(leftOperand, rightOperand, ::GeNode)
      TokenType.GT -> numberComparisonOperatorNode(leftOperand, rightOperand, ::GtNode)
      TokenType.LOE -> numberComparisonOperatorNode(leftOperand, rightOperand, ::LeNode)
      TokenType.LT -> numberComparisonOperatorNode(leftOperand, rightOperand, ::LtNode)
      TokenType.IS -> {
        val left = leftOperand.accept(this)
        val right = rightOperand.accept(this)
        if (left.type.primitive || right.type.primitive) throw MarcelSemanticException(leftOperand, "=== operator is reserved for object comparison")
        IsEqualNode(left, right)
      }
      TokenType.FIND -> {
        val left = leftOperand.accept(this)
        val right = rightOperand.accept(this)

        if (!CharSequence::class.javaType.isAssignableFrom(left.type)) {
          throw MarcelSemanticException(node, "FIND operator left operand must be a CharSequence")
        }
        if (!Pattern::class.javaType.isAssignableFrom(right.type)) {
          throw MarcelSemanticException(node, "FIND operator right operand must be a Pattern")
        }
        fCall(owner = right, ownerType = Pattern::class.javaType,
          name = "matcher", arguments = listOf(left), node = node)
      }
      TokenType.IS_NOT -> {
        val left = leftOperand.accept(this)
        val right = rightOperand.accept(this)
        if (left.type.primitive || right.type.primitive) throw MarcelSemanticException(leftOperand, "=== operator is reserved for object comparison")
        IsNotEqualNode(left, right)
      }
      else -> throw MarcelSemanticException(node, "Doesn't handle operator ${node.tokenType}")
    }
  }

  private fun dotOperator(node: CstNode, owner: ExpressionNode, rightOperand: ExpressionCstNode,
                          // useful for ternaryNode which duplicate value to avoid using local variable
                          discardOwnerInReturned: Boolean = false): ExpressionNode = when (rightOperand) {
    is FunctionCallCstNode -> {
      val (method, arguments) = methodResolver.resolveMethod(node, owner.type, rightOperand.value,
        rightOperand.positionalArgumentNodes.map { it.accept(this) },
        rightOperand.namedArgumentNodes.map { Pair(it.first, it.second.accept(this)) })
        ?: throw MarcelSemanticException(node.token, "Method ${owner.type}.${rightOperand.value} couldn't be resolved")
      val castType = rightOperand.castType?.let { visit(it) }
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

  override fun visit(node: BinaryTypeOperatorCstNode, smartCastType: JavaType?): ExpressionNode {
    val left = node.leftOperand.accept(this)
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
    leftOperand: ExpressionCstNode,
    rightOperand: ExpressionCstNode,
    nodeCreator: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode): ExpressionNode {
    var left = leftOperand.accept(this)
    var right = rightOperand.accept(this)

    if (!left.type.primitive || !right.type.primitive) {
      // compare left.compareTo(right) with 0
      if (!left.type.implements(Comparable::class.javaType)) {
        throw MarcelSemanticException(leftOperand, "Cannot compare non comparable type")
      }
      left = fCall(owner = left, ownerType = left.type, name = "compareTo", arguments = listOf(right), node = leftOperand)
      right = IntConstantNode(leftOperand.token, 0)
    }

    val type = if (left.type != JavaType.int) right.type else left.type
    return nodeCreator.invoke(caster.cast(type, left), caster.cast(type, right))
  }

  private fun equalityComparisonOperatorNode(
    leftOperand: ExpressionCstNode,
    rightOperand: ExpressionCstNode,
    nodeCreator: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode,
    objectComparisonNodeCreator: (ExpressionNode, ExpressionNode) -> ExpressionNode): ExpressionNode {
    val left = leftOperand.accept(this)
    val right = rightOperand.accept(this)

    return if (left.type.primitive && right.type.primitive) {
      val type = if (left.type != JavaType.int) right.type else left.type
      nodeCreator.invoke(caster.cast(type, left), caster.cast(type, right))
    } else objectComparisonNodeCreator.invoke(caster.cast(JavaType.Object, left), caster.cast(JavaType.Object, right))
  }

  private fun numberComparisonOperatorNode(
    leftOperand: ExpressionCstNode,
    rightOperand: ExpressionCstNode,
    nodeCreator: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode): ExpressionNode {
    val left = leftOperand.accept(this)
    val right = rightOperand.accept(this)

    if (left.type == JavaType.boolean || right.type == JavaType.boolean) {
      throw MarcelSemanticException(leftOperand, "Cannot compare non number primitives")
    }
    return comparisonOperatorNode(leftOperand, rightOperand, nodeCreator)
  }

  private fun rangeNode(leftOperand: ExpressionCstNode, rightOperand: ExpressionCstNode, methodName: String): ExpressionNode {
    val left = leftOperand.accept(this)
    val right = rightOperand.accept(this)

    val rangeElementType = if (left.type == JavaType.Long || left.type == JavaType.long || right.type == JavaType.Long || right.type == JavaType.long) JavaType.long
    else if (left.type == JavaType.Integer || left.type == JavaType.int || right.type == JavaType.Integer || right.type == JavaType.int) JavaType.int
    else throw MarcelSemanticException(leftOperand, "Ranges can only be of int or long")

    val rangeType = if (rangeElementType == JavaType.long) LongRanges::class.javaType else IntRanges::class.javaType

    val method = typeResolver.findMethodOrThrow(rangeType, methodName, listOf(rangeElementType, rangeElementType))
    return fCall(method = method, arguments = listOf(left, right), node = leftOperand)
  }

  private fun shiftOperator(leftOperand: ExpressionCstNode, rightOperand: ExpressionCstNode,
                            operatorMethodName: String,
                            nodeSupplier: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode): ExpressionNode {
    val left = leftOperand.accept(this)
    val right = rightOperand.accept(this)
    val node = arithmeticBinaryOperator(left, right, operatorMethodName, nodeSupplier)
    if (JavaType.commonType(left, right).isPrimitiveOrObjectPrimitive && node.type.primitive && node.type != JavaType.long && node.type != JavaType.int) {
      throw MarcelSemanticException(node.token, "Can only shift ints or longs")
    }
    return node
  }
  private inline fun arithmeticBinaryOperator(leftOperand: ExpressionCstNode, rightOperand: ExpressionCstNode,
                                              operatorMethodName: String,
                                              nodeSupplier: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode)
  = arithmeticBinaryOperator(leftOperand.accept(this), rightOperand.accept(this), operatorMethodName, nodeSupplier)

  private fun arithmeticAssignmentBinaryOperator(leftOperand: ExpressionCstNode, rightOperand: ExpressionCstNode,
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

  override fun visit(node: ReferenceCstNode, smartCastType: JavaType?): ExpressionNode {
    val (variable, owner) = findVariableAndOwner(node.value, node)
    return ReferenceNode(owner, variable, node.token)
  }

  private fun findVariableAndOwner(name: String, node: CstNode): Pair<Variable, ThisReferenceNode?> {
    val localVariable = currentScope.findLocalVariable(name)
    if (localVariable != null) {
      return Pair(localVariable, null)
    }
    val field = currentScope.findFieldOrThrow(name, node.token)

    // TODO this check should not be here as this node can appear to be used for a set.
    //  remove this check here, create a CanGetVisitor and CanSetVisitor that we'll use only
    //  when we're sure of what to do with the expression
    checkVariableAccess(field, node, checkGet = true)
    return Pair(field, if (!field.isStatic) ThisReferenceNode(currentScope.classType, node.token) else null)
  }

  override fun visit(node: FunctionCallCstNode, smartCastType: JavaType?): ExpressionNode {
    val positionalArguments = node.positionalArgumentNodes.map { it.accept(this) }
    val namedArguments = node.namedArgumentNodes.map { Pair(it.first, it.second.accept(this)) }
    val methodResolve = methodResolver.resolveMethod(node, currentScope.classType, node.value, positionalArguments, namedArguments)
      ?: methodResolver.resolveMethodFromImports(node, node.value, positionalArguments, namedArguments)

    // look for delegate if any
    if (methodResolve == null && currentScope.classType.implements(DelegatedObject::class.javaType)) {
      val delegateGetter = typeResolver.findMethod(currentScope.classType, "getDelegate", emptyList())
      if (delegateGetter != null) {
        val delegateMethodResolve = methodResolver.resolveMethod(node, delegateGetter.returnType, node.value, positionalArguments, namedArguments)
        if (delegateMethodResolve != null) {
          val owner = fCall(node=node, method = delegateGetter, arguments = emptyList(), owner = ThisReferenceNode(currentScope.classType, node.token))
          return fCall(node = node, method = delegateMethodResolve.first, arguments = delegateMethodResolve.second, owner = owner)
        }

      }
    }

    if (methodResolve == null) {
      throw MarcelSemanticException(node.token, "Method ${currentScope.classType}.${node.value} couldn't be resolved")
    }

    val method = methodResolve.first
    val castType = if (node.castType != null) visit(node.castType!!) else null
    val owner = if (method.isStatic) null else ThisReferenceNode(currentScope.classType, node.token)
    return fCall(
      node = node,
      method = method,
      owner = owner,
      castType = castType,
      arguments = methodResolve.second)
  }

  private fun castedArguments(method: JavaMethod, arguments: List<ExpressionNode>) =
    arguments.mapIndexed { index, expressionNode -> caster.cast(method.parameters[index].type, expressionNode) }

  override fun visit(node: SuperConstructorCallCstNode, smartCastType: JavaType?): ExpressionNode {
    val arguments = node.arguments.map { it.accept(this) }
    val superType = currentScope.classType.superType!!
    val superConstructorMethod = typeResolver.findMethodOrThrow(superType, JavaMethod.CONSTRUCTOR_NAME, arguments, node.token)

    return SuperConstructorCallNode(superType, superConstructorMethod, emptyList(), node.tokenStart, node.tokenEnd)
  }

  override fun visit(node: ExpressionStatementCstNode)
  = ExpressionStatementNode(node.expressionNode.accept(this, JavaType.void), node.tokenStart, node.tokenEnd)

  override fun visit(node: ReturnCstNode): StatementNode {
    // TODO test error cases of this
    val scope = currentMethodScope
    val expression = node.expressionNode?.accept(this, scope.method.returnType)?.let { caster.cast(scope.method.returnType, it) }
    if (expression != null && expression.type != JavaType.void && scope.method.returnType == JavaType.void) {
      throw MarcelSemanticException(node, "Cannot return expression in void function")
    } else if (expression == null && scope.method.returnType != JavaType.void) {
      throw MarcelSemanticException(node, "Must return expression in non void function")
    }
    return ReturnStatementNode(expression, node.tokenStart, node.tokenEnd)
  }

  override fun visit(node: VariableDeclarationCstNode): StatementNode {
    val variable = currentMethodScope.addLocalVariable(visit(node.type), node.value)
    checkVariableAccess(variable, node, checkSet = true)
    return ExpressionStatementNode(
      VariableAssignmentNode(variable,
        node.expressionNode?.accept(this, variable.type)?.let { caster.cast(variable.type, it) }
          ?: variable.type.getDefaultValueExpression(node.token), null, node.tokenStart, node.tokenEnd)
    )
  }

  override fun visit(node: MultiVarDeclarationCstNode): StatementNode {
    if (node.declarations.isEmpty() || node.declarations.all { it == null }) {
      throw MarcelSemanticException(node, "Need to declare at least one variable")
    }
    val expression = node.expressionNode.accept(this)
    val blockNode = BlockStatementNode(mutableListOf(), node.tokenStart, node.tokenEnd)
    currentMethodScope.useTempLocalVariable(expression.type) { expressionVariable: LocalVariable ->
      val expressionRef = ReferenceNode(owner = null, variable = expressionVariable, token = node.token)

      // put the expression in a local variable
      blockNode.statements.add(
        ExpressionStatementNode(VariableAssignmentNode(
          localVariable = expressionVariable,
          expression = expression,
          node = node
        ))
      )

      // declare all variables
      val variableMap = mutableMapOf<Int, LocalVariable>()
      node.declarations.forEachIndexed { index, pair ->
        if (pair != null) variableMap[index] = currentMethodScope.addLocalVariable(visit(pair.first), pair.second)
      }
      // then assign
      when {
        expressionVariable.type.implements(List::class.javaType) -> {
          val getAtMethod = typeResolver.findMethodOrThrow(expressionVariable.type, GET_AT_METHOD_NAME, listOf(JavaType.int))
          variableMap.forEach { (index, variable) ->
            blockNode.statements.add(
              ExpressionStatementNode(
                VariableAssignmentNode(
                  localVariable = variable,
                  expression = fCall(method = getAtMethod, arguments = listOf(IntConstantNode(node.token, index)), owner = expressionRef, node = node, castType = variable.type),
                  node = node
                )
              )
            )
          }
        }
        expression.type.isArray -> {
          variableMap.forEach { (index, variable) ->
            blockNode.statements.add(
              ExpressionStatementNode(
                VariableAssignmentNode(
                  localVariable = variable,
                  expression = caster.cast(variable.type, ArrayAccessNode(expressionRef, IntConstantNode(node.token, index), node)),
                  node = node
                )
              )
            )
          }
        }
        else -> throw MarcelSemanticException(node, "Multi variable declarations only works on List or arrays")
      }
    }
    return blockNode
  }

  override fun visit(node: IfCstStatementNode) = useInnerScope {
    IfStatementNode(caster.truthyCast(node.condition.accept(this)),
      node.trueStatementNode.accept(this),
      node.falseStatementNode?.accept(this), node)
  }

  override fun visit(node: SwitchCstNode, smartCastType: JavaType?): ExpressionNode {
    // transforming the switch into a when
    val switchExpression = node.switchExpression.accept(this,)
    return switchWhen(node, smartCastType, switchExpression)
  }

  override fun visit(node: WhenCstNode, smartCastType: JavaType?) = switchWhen(node, smartCastType)

  private fun switchWhen(node: WhenCstNode, smartCastType: JavaType?, switchExpression: ExpressionNode? = null): ExpressionNode {
    val shouldReturnValue = smartCastType != null && smartCastType != JavaType.void
    val elseStatement = node.elseStatement
    if (shouldReturnValue && elseStatement == null) {
      throw MarcelSemanticException(node, "When/switch expression should have an else branch")
    }
    if (node.branches.isEmpty()) {
      if (elseStatement == null || shouldReturnValue) throw MarcelSemanticException("Switch/When should have at least 1 non else branch")
      node.branches.add(
        Pair(BoolCstNode(node.parent, false, node.token), BlockCstNode(emptyList(), node.parent, node.tokenStart, node.tokenEnd))
      )
    }

    val whenReturnType = smartCastType ?: computeWhenReturnType(node)

    val switchExpressionRef = ReferenceCstNode(node.parent, "__switch_expression", node.token)
    val switchExpressionLocalVariable = currentMethodScope.addLocalVariable(switchExpression?.type ?: JavaType.Object, switchExpressionRef.value)

    val rootIfCstNode = node.branches.first().let {
      toIf(it, switchExpression, switchExpressionRef, node)
    }
    var ifCstNode = rootIfCstNode
    for (i in 1 until node.branches.size) {
      val branch = node.branches[i]
      val ifBranch = toIf(branch, switchExpression, switchExpressionRef, node)
      ifCstNode.falseStatementNode = ifBranch
      ifCstNode = ifBranch
    }
    if (elseStatement != null) ifCstNode.falseStatementNode = elseStatement

    val whenStatement = useInnerScope { visit(rootIfCstNode) }
    if (shouldReturnValue) {
      var tmpIfNode: IfStatementNode? = whenStatement
      val branchTransformer = ReturningWhenIfBranchTransformer(node) { caster.cast(whenReturnType, it) }
      while (tmpIfNode != null) {
        tmpIfNode.trueStatementNode = tmpIfNode.trueStatementNode.accept(branchTransformer)
        if (tmpIfNode.falseStatementNode is IfStatementNode || tmpIfNode.falseStatementNode  == null) {
          tmpIfNode = tmpIfNode.falseStatementNode as? IfStatementNode
        } else {
          tmpIfNode.falseStatementNode = tmpIfNode.falseStatementNode!!.accept(branchTransformer)
          break
        }
      }
    }

    /*
     * Search for all local variables used, they will need to be passed to the generated
     * when function
     */
    val localVariableReferencesMap = LinkedHashMap<String, LocalVariable>()

    whenStatement.forEach(AstVariableNode::class) {
      val variable = it.variable
      if (variable is LocalVariable) {
        localVariableReferencesMap[variable.name] = variable
      }
    }
    val whenLocalVariables = localVariableReferencesMap.map { it.value }

    // now re-set local variable to set the right index
    whenStatement.forEach(AstVariableNode::class) { variableNode ->
      val variable = variableNode.variable
      if (variable is LocalVariable) {
        val index = whenLocalVariables.indexOfFirst { it.name == variable.name }
        variableNode.variable = LocalVariable(variable.type, variable.name, variable.nbSlots,
          if (currentMethodScope.staticContext) index else index + 1, variable.isFinal
        )
      }
    }

    val whenMethodParameters = whenLocalVariables.map { MethodParameter(it.type, it.name) }.toMutableList()
    val whenMethodArguments = whenLocalVariables.map {
      // need to replace the switch local variable by the actual expression since it wasn't yet really passed to the Ast tree
      if (switchExpression != null && it.name == switchExpressionLocalVariable.name) switchExpression
      // we don't want the whenLocalVariable because the index of it is specific for IN the when/switch method
      else ReferenceNode(owner = null, variable = localVariableReferencesMap.getValue(it.name), token = node.token)
    }.toMutableList()
    /*
     * generating method
     */
    val whenMethod = generateOrGetWhenMethod(whenMethodParameters, whenReturnType, node)
    whenMethod.blockStatement = BlockStatementNode(mutableListOf(whenStatement), whenStatement.tokenStart, whenStatement.tokenEnd).apply {
      // if it is not void, statements already have return nodes because of ReturningWhenIfBranchTransformer
      if (whenReturnType == JavaType.void) statements.add(SemanticHelper.returnVoid(this))
    }

    // dispose switch expression variable
    currentMethodScope.freeLocalVariable(switchExpressionLocalVariable.name)

    // now calling the method
    return fCall(node = node, owner = ThisReferenceNode(currentScope.classType, node.token),
      arguments = whenMethodArguments, method = whenMethod)
  }

  private fun computeWhenReturnType(node: WhenCstNode): JavaType = useInnerScope {
    val branchTransformer = ReturningWhenIfBranchTransformer(node)
    node.branches.forEach {
      it.second.accept(this).accept(branchTransformer)
    }
    node.elseStatement?.accept(this)?.accept(branchTransformer)
    return JavaType.commonType(branchTransformer.collectedTypes)
  }

  private fun generateOrGetWhenMethod(parameters: List<MethodParameter>, returnType: JavaType, node: CstNode): MethodNode {
    val classType = currentScope.classType
    val classNode = classNodeMap.getValue(classType)
    val methodName = "__when_" + node.hashCode().toString().replace('-', '_') + "_" + currentMethodScope.method.name
    val existingMethodNode = classNode.methods.find { it.name == methodName }
    /// we don't want to define the same mehod twice, so we find it if we already registered it
    if (existingMethodNode != null) return existingMethodNode
    val methodNode = MethodNode(methodName, parameters, Visibility.PRIVATE, returnType,
      currentMethodScope.staticContext, node.tokenStart, node.tokenEnd, classType)
    typeResolver.defineMethod(classType, methodNode)
    classNode.methods.add(methodNode)
    return methodNode
  }

  private fun toIf(it: Pair<ExpressionCstNode, StatementCstNode>, switchExpression: ExpressionNode?, switchExpressionRef: ExpressionCstNode, node: CstNode): IfCstStatementNode {
    return if (switchExpression != null) IfCstStatementNode(
      BinaryOperatorCstNode(
        TokenType.EQUAL, switchExpressionRef, it.first, node.parent, node.tokenStart, node.tokenEnd
      ), it.second, null, it.first.parent, it.first.tokenStart, it.second.tokenEnd)
    else IfCstStatementNode(it.first, it.second, null, it.first.parent, node.tokenStart, node.tokenEnd)
  }

  override fun visit(node: WhileCstNode) = useScope(MethodInnerScope(currentMethodScope, isInLoop = true)) {
    val condition = caster.truthyCast(node.condition.accept(this))
    val statement = node.statement.accept(this)
    WhileNode(node, condition, statement)
  }

  override fun visit(node: ForInCstNode) = useScope(MethodInnerScope(currentMethodScope, isInLoop = true)) {
    val variable = it.addLocalVariable(visit(node.varType), node.varName)

    val inNode = node.inNode.accept(this)

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
      ForInIteratorStatementNode(node, variable, iteratorVariable, iteratorExpression, nextMethodCall, node.statementNode.accept(this))
    }
  }

  override fun visit(node: TruthyVariableDeclarationCstNode, smartCastType: JavaType?): ExpressionNode {
    val variable = currentMethodScope.addLocalVariable(visit(node.type), node.value)
    return VariableAssignmentNode(localVariable = variable, expression = caster.cast(variable.type, node.expression.accept(this)), node = node)
  }

  override fun visit(node: ForVarCstNode): StatementNode = useScope(MethodInnerScope(currentMethodScope, isInLoop = true)) {
    val initStatement = node.varDecl.accept(this)
    val condition = caster.truthyCast(node.condition.accept(this))
    val iteratorStatement = node.iteratorStatement.accept(this)
    val bodyStatement = node.bodyStatement.accept(this)
    ForStatementNode(
      node = node,
      initStatement = initStatement,
      condition = condition,
      iteratorStatement = iteratorStatement,
      bodyStatement = bodyStatement
    )
  }

  override fun visit(node: BreakCstNode): StatementNode {
    if (!currentInnerMethodScope.isInLoop) {
      throw MarcelSemanticException(node, "Cannot break outside of a loop")
    }
    return BreakNode(node)
  }

  override fun visit(node: ContinueCstNode): StatementNode {
    if (!currentInnerMethodScope.isInLoop) {
      throw MarcelSemanticException(node, "Cannot continue outside of a loop")
    }
    return ContinueNode(node)
  }

  // TODO do elvis or throw
  override fun visit(node: ThrowCstNode): StatementNode {
    val expression = caster.cast(Throwable::class.javaType, node.expression.accept(this, Throwable::class.javaType))
    return ThrowNode(node, expression)
  }

  override fun visit(node: TryCatchCstNode): StatementNode {
    if (node.finallyNode == null && node.catchNodes.isEmpty() && node.resources.isEmpty()) {
      throw MarcelSemanticException(node, "Try statement must have a finally, catch and/or resources")
    }
    val resourceScope = newInnerScope()

    val tryBlock = BlockStatementNode(mutableListOf(), node.tryNode.tokenStart, node.tryNode.tokenEnd)
    val finallyBlock = BlockStatementNode(mutableListOf(), node.finallyNode?.tokenStart ?: node.tokenStart,
      node.finallyNode?.tokenEnd ?: node.tokenEnd)

    node.resources.forEach {
      val resourceType = visit(it.type)
      if (!resourceType.implements(Closeable::class.javaType)) {
        throw MarcelSemanticException(node, "Try resources need to implement Closeable")
      }
      val resourceVar = resourceScope.addLocalVariable(resourceType, it.value)
      val resourceRef = ReferenceNode(null, resourceVar, node.token)

      if (it.expressionNode == null) throw MarcelSemanticException(it, "Resource declarations need to be initialised")
      // assign the resource in the try block
      tryBlock.statements.add(
        ExpressionStatementNode(VariableAssignmentNode(resourceVar,
          caster.cast(resourceType.type, it.expressionNode!!.accept(this, resourceType.type)),
          it.tokenStart, it.tokenEnd))
      )

      // close the resources
      finallyBlock.statements.add(
        IfStatementNode(IsNotEqualNode(resourceRef, NullValueNode(node.token)),
          ExpressionStatementNode(fCall(owner = resourceRef, arguments = emptyList(),
            method = typeResolver.findMethodOrThrow(resourceType, "close", emptyList()), node = node))
          , null, node)
      )
    }

    useScope(MethodInnerScope(resourceScope)) {
      tryBlock.statements.add(node.tryNode.accept(this))
    }

    if (node.finallyNode != null) useInnerScope {
      finallyBlock.statements.add(node.finallyNode!!.accept(this))
    }

    val catchNodes = node.catchNodes.map { triple ->
      val throwableTypes = triple.first.map(this::visit)
      if (throwableTypes.any { !Throwable::class.javaType.isAssignableFrom(it) }) {
        throw MarcelSemanticException(node.token, "Can only catch throwable types")
      }
      if (throwableTypes.isEmpty()) {
        throw MarcelSemanticException(node.token, "Need to catch at least one exception")
      }

      val (throwableVar, catchStatement) = useInnerScope {
        val v = it.addLocalVariable(JavaType.commonType(throwableTypes), triple.second)
        Pair(v, triple.third.accept(this))
      }
      CatchNode(throwableTypes, throwableVar, catchStatement)
    }

    val finallyNode = if (finallyBlock.statements.isNotEmpty())
      useInnerScope { CatchNode(listOf(Throwable::class.javaType), it.addLocalVariable(Throwable::class.javaType), finallyBlock) }
    else null
    return TryCatchNode(node, tryBlock, catchNodes, finallyNode)
  }

  override fun visit(node: SimpleImportCstNode) = SimpleImportNode(node.className, node.asName, node.tokenStart, node.tokenEnd)

  override fun visit(node: StaticImportCstNode) = StaticImportNode(node.className, node.methodName, node.tokenStart, node.tokenEnd)

  override fun visit(node: WildcardImportCstNode) = WildcardImportNode(node.prefix, node.tokenStart, node.tokenEnd)
  override fun visit(node: BlockCstNode) = useInnerScope {
    val statements = blockStatements(node.statements)
    BlockStatementNode(statements, node.tokenStart, node.tokenEnd)
  }

  private fun fCall(node: CstNode, ownerType: JavaType, name: String, arguments: List<ExpressionNode>,
                    owner: ExpressionNode? = null,
                    castType: JavaType? = null): ExpressionNode {
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
    castType: JavaType? = null): ExpressionNode {
    val node = FunctionCallNode(method, owner, castedArguments(method, arguments), token)
    return if (castType != null) caster.cast(castType, node) else node
  }

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
    val visibility = Visibility.fromTokenType(node.accessNode.visibility)
    val isStatic = node.accessNode.isStatic
    return JavaMethodImpl(ownerType, Visibility.fromTokenType(node.accessNode.visibility), node.name,
      node.parameters.mapIndexed { index, methodParameterCstNode -> toMethodParameter(ownerType, visibility, isStatic, index, node.name, methodParameterCstNode) }, visit(node.returnTypeCstNode), false, false, isStatic, false)
  }

  private fun toJavaConstructor(ownerType: JavaType, node: ConstructorCstNode): JavaMethod {
    val visibility = Visibility.fromTokenType(node.accessNode.visibility)
    return JavaConstructorImpl(visibility, ownerType, node.parameters.mapIndexed { index, methodParameterCstNode -> toMethodParameter(
      ownerType,
      visibility,
      false,
      index,
      "constructor",
      methodParameterCstNode
    )})
  }

  private fun toMarcelField(ownerType: JavaType, fieldNode: FieldCstNode): MarcelField {
   return JavaClassFieldImpl(visit(fieldNode.type), fieldNode.name, ownerType, fieldNode.access.isFinal,
      Visibility.fromTokenType(fieldNode.access.visibility), fieldNode.access.isStatic)
  }

  private fun toMethodParameter(ownerType: JavaType, visibility: Visibility,
                                isStatic: Boolean, parameterIndex: Int,
                                methodName: String, node: MethodParameterCstNode): MethodParameter {
    val parameterType = visit(node.type)
    val defaultValue = if (node.defaultValue != null) {
      val defaultValueMethod = defaultParameterMethod(node, ownerType, visibility, isStatic, methodName, parameterType, parameterIndex)
      useScope(newMethodScope(ownerType, defaultValueMethod)) { caster.cast(parameterType, node.defaultValue!!.accept(this)) }
    } else null
    return MethodParameter(parameterType, node.name, node.annotations.map { annotationNode(it, ElementType.PARAMETER) }, defaultValue)
  }

  private fun defaultParameterMethod(node: CstNode, ownerClass: JavaType, visibility: Visibility, isStatic: Boolean, methodName: String, type: JavaType, parameterIndex: Int): MethodNode {
    return MethodNode("${methodName}_defaultParam${parameterIndex}", emptyList(), visibility, type,
      // always static because it can be called from outside this class
      //  if it weren't static we would have to push the owner when calling the method, which can make the semantic analysis more complex
      isStatic= true, node.tokenStart, node.tokenEnd, ownerClass)
  }

  // add annotation if necessary to the node, e.g. for default parameters. To use on to be compiled methods
  private fun toMethodParameterNode(ownerType: JavaType, visibility: Visibility,
                                    isStatic: Boolean, parameterIndex: Int,
                                    methodName: String, node: MethodParameterCstNode): MethodParameter {
    val annotations = node.annotations.map { annotationNode(it, ElementType.PARAMETER) }.toMutableList()
    val parameterType = visit(node.type)
    val parameterName = node.name
    val defaultValue =
      if (node.defaultValue != null) {
        val defaultValueMethod = defaultParameterMethod(node, ownerType, visibility, isStatic, methodName, parameterType, parameterIndex)
        useScope(newMethodScope(ownerType, defaultValueMethod)) { caster.cast(parameterType, node.defaultValue!!.accept(this)) }
      } else null
    if (defaultValue != null) {
      when {
        defaultValue is NullValueNode -> {
          if (parameterType.primitive) {
            throw MarcelSemanticException(node.token, "Primitive types cannot have null default value")
          }
          annotations.add(AnnotationNode(NullDefaultValue::class.javaType, emptyList(), node.tokenStart, node.tokenEnd))
        }
        (parameterType == JavaType.int || parameterType == JavaType.Integer)
            && defaultValue is IntConstantNode -> annotations.add(AnnotationNode(IntDefaultValue::class.javaType, listOf(AnnotationNode.AttributeNode("value", JavaType.int, defaultValue.value)), node.tokenStart, node.tokenEnd))
        (parameterType == JavaType.long || parameterType == JavaType.Long)
            && defaultValue is LongConstantNode -> annotations.add(AnnotationNode(LongDefaultValue::class.javaType, listOf(AnnotationNode.AttributeNode("value", JavaType.long, defaultValue.value)), node.tokenStart, node.tokenEnd))
        (parameterType == JavaType.float || parameterType == JavaType.Float)
            && defaultValue is FloatConstantNode -> annotations.add(AnnotationNode(FloatDefaultValue::class.javaType, listOf(AnnotationNode.AttributeNode("value", JavaType.float, defaultValue.value)), node.tokenStart, node.tokenEnd))
        (parameterType == JavaType.double || parameterType == JavaType.Double)
            && defaultValue is DoubleConstantNode -> annotations.add(AnnotationNode(DoubleDefaultValue::class.javaType, listOf(AnnotationNode.AttributeNode("value", JavaType.double, defaultValue.value)), node.tokenStart, node.tokenEnd))
        (parameterType == JavaType.char || parameterType == JavaType.Character)
            && defaultValue is CharConstantNode -> annotations.add(AnnotationNode(CharacterDefaultValue::class.javaType, listOf(AnnotationNode.AttributeNode("value", JavaType.char, defaultValue.value)), node.tokenStart, node.tokenEnd))
        (parameterType == JavaType.boolean || parameterType == JavaType.Boolean)
            && defaultValue is BoolConstantNode -> annotations.add(AnnotationNode(BooleanDefaultValue::class.javaType, listOf(AnnotationNode.AttributeNode("value", JavaType.boolean, defaultValue.value)), node.tokenStart, node.tokenEnd))
        parameterType == JavaType.String
            && defaultValue is StringConstantNode -> annotations.add(AnnotationNode(StringDefaultValue::class.javaType, listOf(AnnotationNode.AttributeNode("value", JavaType.String, defaultValue.value)), node.tokenStart, node.tokenEnd))
        else -> {
          TODO("Doesn't handle complex method default parameters yet")
          /*

          // always static because it can be called from outside this class
          val defaultParameterMethodNode = if (defaultValue is FunctionCallNode
            && defaultValue.getMethod(typeResolver).let { it.isStatic && it.parameters.isEmpty() && it.ownerClass == methodNode.ownerClass && it.isStatic }
            && classNode.methods.find { it.parameters.isEmpty() && it.isStatic && it.name == defaultValue.name } != null)
            classNode.methods.find { it.parameters.isEmpty() && it.isStatic && it.name == defaultValue.name }!!
          else MethodNode.from(parameter.token, classNode.scope, classNode.type, JavaMethod.defaultParameterMethodName(methodNode, parameter), emptyList(), parameterType, emptyList(), true).apply {
            block.statements.add(
              ReturnNode(parameter.token, scope, defaultValue)
            )
            classNode.addMethod(this)
            // important so that the method can be found by type resolver
            typeResolver.defineMethod(classNode.type, this)
                      }

          // now writing annotation
          mv.visitParameterAnnotation(i, MethodCallDefaultValue::class.javaType.descriptor, true)
            .visit("methodName", defaultParameterMethodNode.name)

           */
        }
      }
    }
    return MethodParameter(parameterType, parameterName, annotations, defaultValue)
  }
}