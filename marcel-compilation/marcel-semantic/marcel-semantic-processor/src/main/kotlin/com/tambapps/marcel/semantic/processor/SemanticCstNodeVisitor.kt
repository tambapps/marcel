package com.tambapps.marcel.semantic.processor

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.AllInCstNode
import com.tambapps.marcel.parser.cst.expression.AnyInCstNode
import com.tambapps.marcel.parser.cst.expression.AsyncBlockCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryTypeOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.ElvisThrowCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.FindInCstNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.InOperationCstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.parser.cst.expression.MapFilterCstNode
import com.tambapps.marcel.parser.cst.expression.NewInstanceCstNode
import com.tambapps.marcel.parser.cst.expression.NotCstNode
import com.tambapps.marcel.parser.cst.expression.SuperConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.SwitchCstNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringCstNode
import com.tambapps.marcel.parser.cst.expression.TernaryCstNode
import com.tambapps.marcel.parser.cst.expression.ThisConstructorCallCstNode
import com.tambapps.marcel.parser.cst.expression.TruthyVariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusCstNode
import com.tambapps.marcel.parser.cst.expression.WhenCstNode
import com.tambapps.marcel.parser.cst.expression.literal.ArrayCstNode
import com.tambapps.marcel.parser.cst.expression.literal.BoolCstNode
import com.tambapps.marcel.parser.cst.expression.literal.CharCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.MapCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.literal.RegexCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IncrCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.SuperReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ThisReferenceCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.statement.BreakCstNode
import com.tambapps.marcel.parser.cst.statement.ContinueCstNode
import com.tambapps.marcel.parser.cst.statement.DoWhileStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ForInCstNode
import com.tambapps.marcel.parser.cst.statement.ForInMultiVarCstNode
import com.tambapps.marcel.parser.cst.statement.ForVarCstNode
import com.tambapps.marcel.parser.cst.statement.IfStatementCstNode
import com.tambapps.marcel.parser.cst.statement.MultiVarDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.StatementCstNode
import com.tambapps.marcel.parser.cst.statement.ThrowCstNode
import com.tambapps.marcel.parser.cst.statement.TryCatchCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode
import com.tambapps.marcel.parser.cst.statement.WhileCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.LambdaClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.processor.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ArrayAccessNode
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.DupNode
import com.tambapps.marcel.semantic.ast.expression.ExprErrorNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.GetAtFunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.JavaCastNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.NewLambdaInstanceNode
import com.tambapps.marcel.semantic.ast.expression.OwnableAstNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.TernaryNode
import com.tambapps.marcel.semantic.ast.expression.ThisConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.JavaConstantExpression
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.NewArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.operator.AndNode
import com.tambapps.marcel.semantic.ast.expression.operator.ArrayIndexAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.operator.BinaryOperatorNode
import com.tambapps.marcel.semantic.ast.expression.operator.DivNode
import com.tambapps.marcel.semantic.ast.expression.operator.ElvisNode
import com.tambapps.marcel.semantic.ast.expression.operator.GeNode
import com.tambapps.marcel.semantic.ast.expression.operator.GtNode
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
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.BreakNode
import com.tambapps.marcel.semantic.ast.statement.ContinueNode
import com.tambapps.marcel.semantic.ast.statement.DoWhileNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.ast.statement.TryNode
import com.tambapps.marcel.semantic.ast.statement.WhileNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.processor.exception.TypeNotFoundException
import com.tambapps.marcel.semantic.processor.exception.VariableNotFoundException
import com.tambapps.marcel.semantic.processor.extensions.getDefaultValueExpression
import com.tambapps.marcel.semantic.extensions.javaAnnotationType
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.symbol.method.ExtensionMarcelMethod
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.method.MethodParameter
import com.tambapps.marcel.semantic.processor.exception.TypeCastException
import com.tambapps.marcel.semantic.processor.exception.VariableAccessException
import com.tambapps.marcel.semantic.processor.exception.VariableRelatedException
import com.tambapps.marcel.semantic.processor.scope.AsyncScope
import com.tambapps.marcel.semantic.processor.scope.CatchBlockScope
import com.tambapps.marcel.semantic.processor.scope.ClassScope
import com.tambapps.marcel.semantic.processor.scope.ImportScope
import com.tambapps.marcel.semantic.processor.scope.LambdaMethodScope
import com.tambapps.marcel.semantic.processor.scope.MethodInnerScope
import com.tambapps.marcel.semantic.processor.scope.MethodScope
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.processor.visitor.AllPathsReturnVisitor
import com.tambapps.marcel.semantic.processor.visitor.ReturningBranchTransformer
import com.tambapps.marcel.semantic.processor.visitor.ReturningWhenIfBranchTransformer
import com.tambapps.marcel.semantic.symbol.type.annotation.JavaAnnotation
import com.tambapps.marcel.semantic.symbol.type.JavaAnnotationType
import com.tambapps.marcel.semantic.symbol.type.JavaPrimitiveType
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.type.PrimitiveCollectionTypes
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import com.tambapps.marcel.semantic.symbol.variable.Variable
import com.tambapps.marcel.semantic.symbol.variable.field.BoundField
import marcel.lang.Delegable
import marcel.lang.IntRange
import marcel.lang.IntRanges
import marcel.lang.LongRanges
import marcel.lang.compile.BooleanDefaultValue
import marcel.lang.compile.CharDefaultValue
import marcel.lang.compile.DoubleDefaultValue
import marcel.lang.compile.FloatDefaultValue
import marcel.lang.compile.IntDefaultValue
import marcel.lang.compile.LongDefaultValue
import marcel.lang.compile.MethodCallDefaultValue
import marcel.lang.compile.NullDefaultValue
import marcel.lang.compile.StringDefaultValue
import marcel.lang.lambda.Lambda
import marcel.lang.runtime.BytecodeHelper
import marcel.util.concurrent.Threadmill
import java.io.Closeable
import java.lang.annotation.ElementType
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Class implementing all visitors of CST nodes.
 * NOT thread-safe
 */
abstract class SemanticCstNodeVisitor constructor(
  final override val symbolResolver: MarcelSymbolResolver,
  packageName: String?,
  val fileName: String
) : AbstractMarcelSemantic(),
  CstSymbolSemantic,
  ExpressionCstNodeVisitor<ExpressionNode, JavaType>,
  StatementCstNodeVisitor<StatementNode> {

  private companion object {
    const val PUT_AT_METHOD_NAME = "putAt"
    const val PUT_AT_SAFE_METHOD_NAME = "putAtSafe"
    const val GET_AT_METHOD_NAME = "getAt"
    const val GET_AT_SAFE_METHOD_NAME = "getAtSafe"
    const val WHEN_METHOD_PREFIX = "__when_"
    const val ASYNC_METHOD_PREFIX = "__async_"
  }

  val imports = ImportResolver.DEFAULT_IMPORTS.toImports()
  protected val errors = mutableListOf<MarcelSemanticException.Error>()

  protected val classNodeMap = mutableMapOf<JavaType, ClassNode>() // useful to add methods while performing analysis
  protected val lambdaMap = mutableMapOf<LambdaCstNode, LambdaClassNode>()
  protected val methodResolver = MethodResolver(symbolResolver, this)
  protected val currentClassNode: ClassNode? get() = currentScope.let { it as? MethodScope }?.let { classNodeMap[it.classType] }

  // never use this field directly, otherwise you won't collect error, you will just throw the first error encountered
  private val caster = AstNodeCaster(symbolResolver)
  // for extension classes
  private val selfLocalVariable: LocalVariable?
    get() = currentMethodScope.findLocalVariable(ExtensionMarcelMethod.THIS_PARAMETER_NAME)

  init {
    scopeQueue.push(ImportScope(symbolResolver, imports, packageName))
  }

  override fun visit(node: DoubleCstNode, smartCastType: JavaType?) = DoubleConstantNode(node.token, node.value)
  override fun visit(node: BoolCstNode, smartCastType: JavaType?) = BoolConstantNode(node.token, node.value)

  override fun visit(node: FloatCstNode, smartCastType: JavaType?) = FloatConstantNode(node.token, node.value)

  override fun visit(node: IntCstNode, smartCastType: JavaType?) = IntConstantNode(node.token, node.value)

  override fun visit(node: LongCstNode, smartCastType: JavaType?) = LongConstantNode(node.token, node.value)
  override fun visit(node: NullCstNode, smartCastType: JavaType?) = NullValueNode(node.token)
  override fun visit(node: StringCstNode, smartCastType: JavaType?) = StringConstantNode(node.value, node)

  override fun visit(node: RegexCstNode, smartCastType: JavaType?): ExpressionNode {
    val arguments =
      if (node.flags.isNotEmpty()) listOf(
        StringConstantNode(node.value, node),
        IntConstantNode(node.token, node.flags.reduce { acc, i -> acc or i })
      )
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

  override fun visit(node: ClassReferenceCstNode, smartCastType: JavaType?) =
    ClassReferenceNode(resolve(node.type), node.token)

  override fun visit(node: ThisReferenceCstNode, smartCastType: JavaType?): ExpressionNode {
    return if (currentClassNode?.isExtensionClass == true)
      selfLocalVariable?.let { ReferenceNode(variable = it, token = node.token) } ?: exprError(node.token, "Cannot reference this in a static context")
    else if (!currentMethodScope.staticContext) ThisReferenceNode(currentScope.classType, node.token)
    else exprError(node, "Cannot reference this in a static context", smartCastType)
  }

  override fun visit(node: SuperReferenceCstNode, smartCastType: JavaType?) =
    SuperReferenceNode(currentScope.classType.superType!!, node.token)

  override fun visit(node: NewInstanceCstNode, smartCastType: JavaType?): ExpressionNode {
    val type = resolve(node.type)

    val positionalArguments = node.positionalArgumentNodes.map { it.accept(this) }
    val namedArguments = node.namedArgumentNodes.map { Pair(it.first, it.second.accept(this)) }

    // handling inner class arguments

    var resolve = methodResolver.resolveMethod(
      node, type,
      MarcelMethod.CONSTRUCTOR_NAME, positionalArguments, namedArguments
    )

    // didn't find the constructor? maybe it was for an inner class with outer parameters
    if (resolve == null && currentScope.classType.isOuterTypeOf(type) && !currentMethodScope.staticContext) {
      val positionalArguments = positionalArguments.toMutableList()
      val namedArguments = namedArguments.toMutableList()
      val outerLevelAndType = outerLevel(node.token, type, currentScope.classType)
      if (outerLevelAndType == null) {
        return exprError(node, "Lambda cannot be generated in this context", smartCastType)
      }
      val (outerLevel, _) = outerLevelAndType
      if (namedArguments.isNotEmpty()) {
        for (i in 0..outerLevel) {
          val argument = getInnerOuterReference(node.token, outerLevel)
          if (argument == null) {
            return exprError(node, "Lambda cannot be generated in this context", smartCastType)
          }
          namedArguments.add(Pair("this$$i", argument))
        }
      } else {
        // going in reverse order to add arguments in right order
        for (i in outerLevel downTo 0) {
          val argument = getInnerOuterReference(node.token, outerLevel)
          if (argument == null) {
            return exprError(node, "Lambda cannot be generated in this context", smartCastType)
          }
          positionalArguments.add(0, argument)
        }
      }
      resolve =
        methodResolver.resolveMethod(node, type, MarcelMethod.CONSTRUCTOR_NAME, positionalArguments, namedArguments)
    }
    if (resolve != null) {
      return NewInstanceNode(type, resolve.first, castedArguments(resolve.first, resolve.second), node.token)
    } else {
      val allParametersString = mutableListOf<String>()
      positionalArguments.forEach {
        allParametersString.add(it.type.simpleName)
      }
      namedArguments.forEach {
        allParametersString.add("${it.first}: ${it.second.type.simpleName}")
      }

      val displayedName = "Constructor $type"

      return exprError(
        node, allParametersString.joinToString(
          separator = ", ",
          prefix = "$displayedName(", postfix = ") is not defined"
        ), smartCastType
      )
    }
  }

  override fun visit(node: DirectFieldReferenceCstNode, smartCastType: JavaType?): ExpressionNode {
    val field = symbolResolver.getClassField(currentScope.classType, node.value, node.token)
    val owner = if (!field.isStatic) ThisReferenceNode(currentScope.classType, node.token) else null
    return ReferenceNode(owner = owner, variable = field, token = node.token)
  }

  override fun visit(node: ArrayCstNode, smartCastType: JavaType?): ExpressionNode {
    if (smartCastType == null) {
      val elements = node.elements.map { it.accept(this) }
      val elementsType = if (elements.isEmpty()) JavaType.objectArray else JavaType.commonType(elements)
      return ArrayNode(
        elements = elements.map { cast(elementsType, it) }.toMutableList(),
        node = node,
        type = elementsType.arrayType
      )
    }
    val arrayType = when {
      smartCastType.isArray -> smartCastType.asArrayType
      JavaType.intCollection.isAssignableFrom(smartCastType) -> JavaType.intArray
      JavaType.longCollection.isAssignableFrom(smartCastType) -> JavaType.longArray
      JavaType.floatCollection.isAssignableFrom(smartCastType) -> JavaType.floatArray
      JavaType.doubleCollection.isAssignableFrom(smartCastType) -> JavaType.doubleArray
      JavaType.charCollection.isAssignableFrom(smartCastType) -> JavaType.charArray
      Collection::class.javaType.isAssignableFrom(smartCastType) -> JavaType.objectArray
      smartCastType == JavaType.DynamicObject -> JavaType.objectArray // will be casted yo dynamic array by the asNode visitor
      else -> return exprError(node, "Cannot cast array into $smartCastType", smartCastType)
    }
    val elementsType = arrayType.elementsType

    val arrayNode = ArrayNode(
      elements = node.elements.map { cast(elementsType, it.accept(this, elementsType)) }.toMutableList(),
      node = node,
      type = arrayType)
    // caster will take care of creating the collection if needed
    return if (arrayType == smartCastType) arrayNode else cast(smartCastType, arrayNode)
  }

  override fun visit(node: MapFilterCstNode, smartCastType: JavaType?): ExpressionNode {
    val expectedType = smartCastType ?: List::class.javaType

    var collectionType =
      if (expectedType.isArray) PrimitiveCollectionTypes.fromArrayType(expectedType.asArrayType) ?: List::class.javaType
      else if (expectedType.implements(Collection::class.javaType)) expectedType
      else if (expectedType.isAssignableFrom(List::class.javaType)) List::class.javaType
      else if (expectedType == JavaType.void) return exprError(
        node,
        "mapfilter cannot be used as a statement", smartCastType
      )
      else return exprError(node, "Incompatible type. Expected Collection/array but got $expectedType", smartCastType)

    val bodyCstNodes = mutableListOf<ExpressionCstNode>()
    node.mapExpr?.let(bodyCstNodes::add)
    node.filterExpr?.let(bodyCstNodes::add)

    return inOperation(
      node,
      node.inExpr,
      "_mapFilter_",
      bodyCstNodes,
      expectedType
    ) { mapFilterMethodNode: MethodNode, methodScope: MethodScope ->
      val collectionVar = methodScope.addLocalVariable(collectionType, Nullness.NOT_NULL)
      val collectionRef = ReferenceNode(variable = collectionVar, token = node.token)
      val inNodeVarRef = ReferenceNode(variable = methodScope.getMethodParameterVariable(0), token = node.token)
      var expectedElementType = if (JavaType.intCollection.isAssignableFrom(collectionType)) JavaType.int
      else if (JavaType.longCollection.isAssignableFrom(collectionType)) JavaType.long
      else if (JavaType.floatCollection.isAssignableFrom(collectionType)) JavaType.float
      else if (JavaType.doubleCollection.isAssignableFrom(collectionType)) JavaType.double
      else if (JavaType.charCollection.isAssignableFrom(collectionType)) JavaType.char
      else JavaType.Object

      useInnerScope { forScope ->
        val forVariable = forScope.addLocalVariable(resolve(node.varType), node.varName, Nullness.UNKNOWN)
        val addedExpression = node.mapExpr?.accept(this) ?: ReferenceNode(variable = forVariable, token = node.token)
        if (addedExpression.type.primitive
          && PrimitiveCollectionTypes.hasPrimitiveCollection(addedExpression.type.asPrimitiveType)
          && (collectionVar.type == List::class.javaType || collectionVar.type == Set::class.javaType)
        ) {
          // try to guess a primitive collection type if the addStmt always adds a primitive
          expectedElementType = addedExpression.type
          collectionType =
            if (collectionVar.type == List::class.javaType) PrimitiveCollectionTypes.listFromPrimitiveType(
              expectedElementType.asPrimitiveType
            )!!
            else PrimitiveCollectionTypes.setFromPrimitiveType(expectedElementType.asPrimitiveType)!!
          mapFilterMethodNode.returnType = collectionType
        }

        mapFilterMethodNode.blockStatement.add(
          ExpressionStatementNode(
            VariableAssignmentNode(
              collectionVar,
              expression = cast(
                collectionType,
                NewArrayNode(
                  expectedElementType.arrayType,
                  IntConstantNode(token = node.token, value = 0),
                  token = node.token
                )
              )
            )
          )
        )

        var addStmt: StatementNode = ExpressionStatementNode(
          fCall(
            node = node,
            name = "add",
            arguments = listOf(cast(expectedElementType, addedExpression)),
            owner = collectionRef
          )
        )
        node.filterExpr?.let { filterExpr ->
          addStmt = IfStatementNode(
            conditionNode = truthyCast(filterExpr.accept(this)),
            trueStatementNode = addStmt,
            falseStatementNode = null,
            node = node
          )
        }
        val forStatement = if (inNodeVarRef.type.isArray) {
          val iVar = forScope.addLocalVariable(JavaType.int, Nullness.NOT_NULL, token = node.token)
          forInArrayNode(
            node = node,
            forScope = forScope,
            iVar = iVar,
            inNode = inNodeVarRef,
            forVariable = forVariable,
            statementNode = addStmt
          )
        } else { // iterating over iterable
          forInIteratorNode(
            node = node,
            forScope = forScope,
            variable = forVariable,
            inNode = inNodeVarRef,
            bodtStmt = addStmt
          )
        }
        mapFilterMethodNode.blockStatement.add(forStatement)
      }
      mapFilterMethodNode.blockStatement.add(ReturnStatementNode(cast(expectedType, collectionRef)))
    }
  }

  override fun visit(node: AllInCstNode, smartCastType: JavaType?): ExpressionNode {
    if (node.negate) {
      return AnyInCstNode(
        node.parent, node.tokenStart, node.tokenEnd, node.varType, node.varName, node.inExpr, NotCstNode(
          expression = node.filterExpr,
          parent = node.parent,
          tokenStart = node.tokenStart,
          tokenEnd = node.tokenEnd
        ), false
      ).accept(this)
    }
    return anyAllOperator(
      node, methodPrefix = "_all_", inExpr = node.inExpr, filterExpr = node.filterExpr,
      varType = node.varType, varName = node.varName, finalReturnValue = true
    ) { filterExpr ->
      IfStatementNode(
        conditionNode = truthyCast(NotNode(filterExpr)),
        trueStatementNode = ReturnStatementNode(BoolConstantNode(value = false, token = node.token)),
        falseStatementNode = null,
        node = node
      )
    }
  }

  override fun visit(node: AnyInCstNode, smartCastType: JavaType?): ExpressionNode {
    if (node.negate) {
      return AllInCstNode(
        node.parent, node.tokenStart, node.tokenEnd, node.varType, node.varName, node.inExpr, NotCstNode(
          expression = node.filterExpr,
          parent = node.parent,
          tokenStart = node.tokenStart,
          tokenEnd = node.tokenEnd
        ), false
      ).accept(this)
    }
    return anyAllOperator(
      node, methodPrefix = "_any_", inExpr = node.inExpr, filterExpr = node.filterExpr,
      varType = node.varType, varName = node.varName, finalReturnValue = false
    ) { filterExpr ->
      IfStatementNode(
        conditionNode = truthyCast(filterExpr),
        trueStatementNode = ReturnStatementNode(BoolConstantNode(value = true, token = node.token)),
        falseStatementNode = null,
        node = node
      )
    }
  }


  override fun visit(node: FindInCstNode, smartCastType: JavaType?): ExpressionNode {
    val varType = resolve(node.varType)
    return inOperation(
      node,
      node.inExpr,
      "_find_",
      listOf(node.filterExpr),
      varType.objectType
    ) { methodNode, methodScope ->
      useInnerScope { forScope ->
        val forVariable = forScope.addLocalVariable(varType, node.varName, Nullness.UNKNOWN)
        val inNodeVarRef = ReferenceNode(variable = methodScope.getMethodParameterVariable(0), token = node.token)

        val forStatement = if (inNodeVarRef.type.isArray) {
          val iVar = forScope.addLocalVariable(JavaType.int, Nullness.NOT_NULL, token = node.token)
          forInArrayNode(
            node = node,
            forScope = forScope,
            iVar = iVar,
            inNode = inNodeVarRef,
            forVariable = forVariable
          ) {
            IfStatementNode(
              conditionNode = truthyCast(node.filterExpr.accept(this)),
              trueStatementNode = ReturnStatementNode(
                cast(varType.objectType, ReferenceNode(variable = forVariable, token = node.token))
              ),
              falseStatementNode = null,
              node = node
            )
          }
        } else { // iterating over iterable
          forInIteratorNode(node = node, forScope = forScope, variable = forVariable, inNode = inNodeVarRef) {
            IfStatementNode(
              conditionNode = truthyCast(node.filterExpr.accept(this)),
              trueStatementNode = ReturnStatementNode(
                cast(varType.objectType, ReferenceNode(variable = forVariable, token = node.token))
              ),
              falseStatementNode = null,
              node = node
            )
          }
        }
        methodNode.blockStatement.apply {
          add(forStatement)
          add(ReturnStatementNode(NullValueNode(token = node.token, type = varType.objectType)))
        }
      }
    }
  }

  private inline fun anyAllOperator(
    node: CstNode,
    methodPrefix: String,
    inExpr: ExpressionCstNode?,
    filterExpr: ExpressionCstNode,
    varType: TypeCstNode,
    varName: String,
    finalReturnValue: Boolean,
    forBodyGenerator: (ExpressionNode) -> StatementNode
  ): ExpressionNode {
    return inOperation(node, inExpr, methodPrefix, listOf(filterExpr), JavaType.boolean) { methodNode, methodScope ->

      val forStatement = useInnerScope { forScope ->
        val forVariable = forScope.addLocalVariable(resolve(varType), varName, Nullness.UNKNOWN)
        val inNodeVarRef = ReferenceNode(variable = methodScope.getMethodParameterVariable(0), token = node.token)

        if (inNodeVarRef.type.isArray) {
          val iVar = forScope.addLocalVariable(JavaType.int, Nullness.NOT_NULL, token = node.token)
          forInArrayNode(
            node = node,
            forScope = forScope,
            iVar = iVar,
            inNode = inNodeVarRef,
            forVariable = forVariable
          ) {
            forBodyGenerator.invoke(filterExpr.accept(this))
          }
        } else { // iterating over iterable
          forInIteratorNode(node = node, forScope = forScope, variable = forVariable, inNode = inNodeVarRef) {
            forBodyGenerator.invoke(filterExpr.accept(this))
          }
        }
      }

      methodNode.blockStatement.apply {
        add(forStatement)
        add(ReturnStatementNode(BoolConstantNode(value = finalReturnValue, token = node.token)))
      }
    }
  }

  private inline fun inOperation(
    node: CstNode, inExpr: ExpressionCstNode?,
    methodPrefix: String,
    bodyCstNodes: List<ExpressionCstNode>,
    methodReturnType: JavaType,
    methodFiller: (MethodNode, MethodScope) -> Unit
  ): ExpressionNode {
    if (inExpr == null) {
      return exprError(node, "Invalid use of IN operation: missing IN value", methodReturnType)
    }
    val inNode = inExpr.accept(this)
    if (!inNode.type.isArray && !inNode.type.implements(Iterable::class.javaType) && !inNode.type.implements(
        CharSequence::class.javaType
      )
    ) {
      return exprError(inExpr, "Can only perform IN operation on an Iterable, CharSequence or array", methodReturnType)
    }
    val inValueName = "_inValue" + node.hashCode().toString().replace('-', '_')
    /*
    * Looking for all local variables used from cst node as we'll need to pass them to the 'mapFilter' method
    */
    val referencedLocalVariables =
      LinkedHashSet<LocalVariable>() // want a constant order, so linkedhashset which is retains insertion order
    val consumer: (CstNode) -> Unit = { cstNode ->
      if ((cstNode is ReferenceCstNode || cstNode is IncrCstNode) && currentScope.hasLocalVariable(cstNode.value.toString())) {
        referencedLocalVariables.add(currentScope.findLocalVariable(cstNode.value.toString())!!)
      }
    }
    bodyCstNodes.forEach { bodyCstNode -> bodyCstNode.forEach(consumer) }

    val inOperatorMethodParameters = mutableListOf(MethodParameter(inNode.type,inNode.nullness,  inValueName))
    inOperatorMethodParameters.addAll(referencedLocalVariables.map { MethodParameter(it.type, it.nullness, it.name) })
    val inOperatorMethodNode = generateOrGetMethod(methodPrefix, inOperatorMethodParameters, methodReturnType, node,
      if (methodReturnType.primitive) Nullness.NOT_NULL else Nullness.UNKNOWN)

    useScope(newMethodScope(inOperatorMethodNode)) { methodScope ->
      methodFiller.invoke(inOperatorMethodNode, methodScope)
    }
    val arguments = mutableListOf(inNode)
    for (lv in referencedLocalVariables) {
      arguments.add(ReferenceNode(variable = lv, token = node.token))
    }
    return FunctionCallNode(
      javaMethod = inOperatorMethodNode,
      owner = ThisReferenceNode(currentScope.classType, node.token),
      arguments = arguments,
      tokenStart = node.tokenStart,
      tokenEnd = node.tokenEnd,
    )
  }

  override fun visit(node: MapCstNode, smartCastType: JavaType?) = MapNode(
    entries = node.entries.map {
      Pair(
        // need objects (not primitive) to call function Map.put(key, value)
        cast(JavaType.Object, it.first.accept(this)),
        cast(JavaType.Object, it.second.accept(this))
      )
    },
    node = node
  )

  override fun visit(node: IncrCstNode, smartCastType: JavaType?): ExpressionNode {
    try {
      val (variable, owner) = findVariableAndOwner(node.value, node)
      checkVariableAccess(variable, node, checkGet = true, checkSet = true)
      return incr(node, variable, owner, smartCastType)
    } catch (e: VariableRelatedException) {
      return exprError(node, e.message)
    }
  }

  private fun incr(
    node: IncrCstNode,
    variable: Variable,
    owner: ExpressionNode?,
    smartCastType: JavaType? = null
  ): ExpressionNode {
    val varType = variable.type
    if (varType != JavaType.int && varType != JavaType.long && varType != JavaType.float && varType != JavaType.double
      && varType != JavaType.short && varType != JavaType.byte
    ) {
      return exprError(node, "Can only increment primitive number variables", smartCastType)
    }
    try {
      checkVariableAccess(variable, node, checkGet = true, checkSet = true)
    } catch (e: VariableAccessException) {
      return exprError(node, e.message, varType.type)
    }

    // a local variable is needed when the expression needs to be pushed and owner is not null and value is returned before assignment
    val lv =
      if (owner != null && smartCastType != JavaType.void && node.returnValueBefore) currentMethodScope.addLocalVariable(
        varType, Nullness.NOT_NULL
      )
      else null
    val amount = castNumberConstantOrNull(node.amount, varType.asPrimitiveType)
      ?: return exprError(node, "Cannot convert value ${node.amount} to ${varType.asPrimitiveType}", varType)
    val incrNode = IncrNode(
      node.token, variable, lv, owner, amount, varType.asPrimitiveType, node.returnValueBefore
    )
    if (lv != null) currentMethodScope.freeLocalVariable(lv.name)
    return incrNode
  }

  override fun visit(node: IndexAccessCstNode, smartCastType: JavaType?): ExpressionNode {
    val owner = node.ownerNode.accept(this)
    return indexAccess(owner, node)
  }

  private fun indexAccess(owner: ExpressionNode, node: IndexAccessCstNode): ExpressionNode {
    val arguments = node.indexNodes.map { it.accept(this) }
    return if (owner.type.isArray && !node.isSafeAccess) { // because array safe access is an extension method
      if (node.indexNodes.size != 1) return exprError(node, "Arrays need one index", owner.type.asArrayType.elementsType)
      ArrayAccessNode(
        owner,
        cast(JavaType.int, node.indexNodes.first().accept(this, JavaType.int)),
        node
      )
    } else {
      val getAtMethod = symbolResolver.findMethodOrThrow(
        owner.type,
        if (node.isSafeAccess) GET_AT_SAFE_METHOD_NAME else GET_AT_METHOD_NAME,
        arguments
      )
      GetAtFunctionCallNode(
        javaMethod = getAtMethod,
        ownerNode = owner,
        arguments = castedArguments(getAtMethod, arguments),
        token = node.token
      )
    }
  }

  override fun visit(node: TernaryCstNode, smartCastType: JavaType?): ExpressionNode {
    val testExpr = truthyCast(node.testExpressionNode.accept(this))
    val trueExpr = node.trueExpressionNode.accept(this)
    val falseExpr = node.falseExpressionNode.accept(this)

    // trueExpr and falseExpr need to be casted in case they return different types
    val commonType = JavaType.commonType(trueExpr, falseExpr)
    return TernaryNode(testExpr, cast(commonType, trueExpr), cast(commonType, falseExpr), node)
  }

  override fun visit(node: NotCstNode, smartCastType: JavaType?) = NotNode(
    truthyCast(
      node.expression.accept(
        this,
      )
    ), node
  )

  override fun visit(node: UnaryMinusCstNode, smartCastType: JavaType?): ExpressionNode {
    val expr = node.expression.accept(this)
    return when  {
      expr is IntConstantNode -> IntConstantNode(node.token, -expr.value)
      expr is LongConstantNode -> LongConstantNode(node.token, -expr.value)
      expr is FloatConstantNode -> FloatConstantNode(node.token, -expr.value)
      expr is DoubleConstantNode -> DoubleConstantNode(node.token, -expr.value)
      Number::class.javaType.isAssignableFrom(expr.type) -> arithmeticBinaryOperator(
        IntConstantNode(node.token, 0), expr, "minus",
        ::MinusNode
      )
      else -> fCall(node = node, name = "negate", arguments = emptyList(), owner = expr)
    }
  }

  override fun visit(node: BinaryOperatorCstNode, smartCastType: JavaType?): ExpressionNode {
    val leftOperand = node.leftOperand
    val rightOperand = node.rightOperand
    return when (val tokenType = node.tokenType) {
      TokenType.ASSIGNMENT -> assignmentOperator(node, smartCastType)
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
        ElvisNode(truthyCast(DupNode(cast(type, left))), cast(type, right), type)
      }

      TokenType.MINUS -> arithmeticBinaryOperator(
        leftOperand, rightOperand, "minus",
        ::MinusNode
      )

      TokenType.MUL -> arithmeticBinaryOperator(
        leftOperand, rightOperand, "multiply",
        ::MulNode
      )

      TokenType.DIV -> arithmeticBinaryOperator(
        leftOperand, rightOperand, "div",
        ::DivNode
      )

      TokenType.MODULO -> arithmeticBinaryOperator(
        leftOperand, rightOperand, "mod",
        ::ModNode
      )

      TokenType.RIGHT_SHIFT ->
        if (rightOperand is InOperationCstNode) rightOperand.apply { inExpr = leftOperand }.accept(this)
        else shiftOperator(
          leftOperand, rightOperand, "rightShift",
          ::RightShiftNode,
          smartCastType
        )

      TokenType.LEFT_SHIFT -> shiftOperator(
        leftOperand, rightOperand, "leftShift",
        ::LeftShiftNode,
        smartCastType
      )

      TokenType.PLUS_ASSIGNMENT -> arithmeticAssignmentBinaryOperator(
        leftOperand, rightOperand, "plus",
        ::PlusNode
      )

      TokenType.MINUS_ASSIGNMENT -> arithmeticAssignmentBinaryOperator(
        leftOperand, rightOperand, "minus",
        ::MinusNode
      )

      TokenType.MUL_ASSIGNMENT -> arithmeticAssignmentBinaryOperator(
        leftOperand, rightOperand, "multiply",
        ::MulNode
      )

      TokenType.DIV_ASSIGNMENT -> arithmeticAssignmentBinaryOperator(
        leftOperand, rightOperand, "div",
        ::DivNode
      )

      TokenType.MODULO_ASSIGNMENT -> arithmeticAssignmentBinaryOperator(
        leftOperand, rightOperand, "mod",
        ::ModNode
      )

      TokenType.QUESTION_DOT -> {
        val left = leftOperand.accept(this)
        if (left.type.primitive) error(node, "Cannot use safe access operator on primitive type as it cannot be null")

        currentMethodScope.useTempLocalVariable(left.type, left.nullness) { lv ->
          var dotNode = dotOperator(node, ReferenceNode(variable = lv, token = node.token), rightOperand, smartCastType = smartCastType)
          if (dotNode.type != JavaType.void && dotNode.type.primitive) dotNode =
            cast(dotNode.type.objectType, dotNode) // needed as the result can be null

          TernaryNode(
            testExpressionNode = IsNotEqualNode(VariableAssignmentNode(lv, left), NullValueNode(node.token, left.type)),
            trueExpressionNode = dotNode,
            falseExpressionNode = NullValueNode(node.token, dotNode.type.objectType),
            node = node
          )
        }
      }

      TokenType.DOT -> when (val leftOperand = node.leftOperand) {
        is ReferenceCstNode -> {
          val p = try {
            findVariableAndOwner(leftOperand.value, node)
          } catch (e: VariableNotFoundException) {
            null
          }
          if (p != null) {
            try {
              checkVariableAccess(p.first, node, checkGet = true)
            } catch (e: VariableAccessException) {
              error(leftOperand, e.message)
            }
            dotOperator(node, ReferenceNode(p.second, p.first, node.token), rightOperand, smartCastType = smartCastType)
          } else {
            // it may be a static method call
            val type = try {
              currentScope.resolveTypeOrThrow(
                TypeCstNode(
                  null,
                  leftOperand.value,
                  emptyList(),
                  0,
                  leftOperand.tokenStart,
                  leftOperand.tokenEnd
                )
              )
            } catch (e2: TypeNotFoundException) {
              return exprError(node.token, "Neither a variable nor a class ${leftOperand.value} was found", smartCastType)
            }
            staticDotOperator(node, type, rightOperand, smartCastType)
          }
        }

        else -> dotOperator(node, node.leftOperand.accept(this), rightOperand, smartCastType = smartCastType)
      }

      TokenType.TWO_DOTS -> rangeNode(leftOperand, rightOperand, "of")
      TokenType.TWO_DOTS_END_EXCLUSIVE -> rangeNode(leftOperand, rightOperand, "ofToExclusive")
      TokenType.AND -> AndNode(
        truthyCast(leftOperand.accept(this)), truthyCast(
          rightOperand.accept(
            this,
          )
        )
      )

      TokenType.OR -> OrNode(
        truthyCast(leftOperand.accept(this)), truthyCast(
          rightOperand.accept(
            this,
          )
        )
      )

      TokenType.EQUAL, TokenType.NOT_EQUAL -> equalityComparisonOperatorNode(
        leftOperand, rightOperand,
        if (tokenType == TokenType.EQUAL) ::IsEqualNode else ::IsNotEqualNode
      ) { left, right ->
        val equalNode = when {
          left.type.isArray && left.type.isArray -> {
            if (left.type == right.type) {
              if (left.type.asArrayType.elementsType.primitive) fCall(
                node = node,
                name = "equals",
                ownerType = Arrays::class.javaType,
                arguments = listOf(left, right)
              )
              else fCall(
                node = node,
                name = "deepEquals",
                ownerType = Arrays::class.javaType,
                arguments = listOf(left, right)
              )
            }
            // Objects.equals(...) handles any Object whereas Arrays.equals() handles Object[]
            else fCall(
              node = node,
              name = "deepEquals",
              ownerType = Objects::class.javaType,
              arguments = listOf(left, right)
            )
          }

          else -> fCall(
            node = node,
            name = "equals",
            ownerType = Objects::class.javaType,
            arguments = listOf(left, right)
          )
        }
        if (tokenType == TokenType.EQUAL) equalNode else NotNode(equalNode)
      }

      TokenType.GOE -> numberComparisonOperatorNode(
        leftOperand, rightOperand,
        ::GeNode
      )

      TokenType.GT -> numberComparisonOperatorNode(
        leftOperand, rightOperand,
        ::GtNode
      )

      TokenType.LOE -> numberComparisonOperatorNode(
        leftOperand, rightOperand,
        ::LeNode
      )

      TokenType.LT -> numberComparisonOperatorNode(
        leftOperand, rightOperand,
        ::LtNode
      )

      TokenType.IS -> {
        val left = leftOperand.accept(this)
        val right = rightOperand.accept(this)
        if (left.type.primitive || right.type.primitive) return exprError(
          leftOperand,
          "=== operator is reserved for object comparison",
          JavaType.boolean
        )
        IsEqualNode(left, right)
      }
      TokenType.FIND -> {
        val left = leftOperand.accept(this)
        val right = rightOperand.accept(this)

        if (!CharSequence::class.javaType.isAssignableFrom(left.type)) {
          return exprError(node, "FIND operator left operand must be a CharSequence", Matcher::class.javaType)
        }
        if (!Pattern::class.javaType.isAssignableFrom(right.type)) {
          return exprError(node, "FIND operator right operand must be a Pattern", Matcher::class.javaType)
        }
        fCall(
          owner = right, ownerType = Pattern::class.javaType,
          name = "matcher", arguments = listOf(left), node = node
        )
      }

      TokenType.IS_NOT -> {
        val left = leftOperand.accept(this)
        val right = rightOperand.accept(this)
        if (left.type.primitive || right.type.primitive) return exprError(
          leftOperand,
          "=== operator is reserved for object comparison", JavaType.boolean
        )
        IsNotEqualNode(left, right)
      }
      else -> return exprError(node, "Doesn't handle operator $tokenType", smartCastType)
    }
  }

  private fun assignmentOperator(node: BinaryOperatorCstNode, smartCastType: JavaType?): ExpressionNode {
    val scope = currentMethodScope

    if (scope.staticContext || !scope.classType.isScript || node.leftOperand !is ReferenceCstNode) {
      // normal case
      // passing smartCastType to indicate whether we need to check get access or not
      return assignment(node, node.leftOperand.accept(this, smartCastType))
    }
    // handling script bound variables
    val referenceCstNode = node.leftOperand as ReferenceCstNode
    try {
      return assignment(node, referenceOrThrow(referenceCstNode, smartCastType))
    } catch (e: VariableRelatedException) {
      if (e is VariableNotFoundException) {
        // the field wasn't defined, but we're in a script => create a bound field
        val right = node.rightOperand.accept(this)

        // This is important. We always want bound field to be object type as values are obtained from getVariable which returns an Object
        val boundField = BoundField(right.type.objectType, (node.leftOperand as ReferenceCstNode).value, scope.classType)
        symbolResolver.defineField(boundField)

        return assignment(node, left = ReferenceNode(
          owner = ThisReferenceNode(currentScope.classType, node.token),
          variable = boundField,
          token = node.token
        ), right = cast(boundField.type, right))
      } else {
        return exprError(node, e.message, smartCastType)
      }
    }
  }

  private fun assignment(
    node: BinaryOperatorCstNode,
    left: ExpressionNode,
    right: ExpressionNode = node.rightOperand.accept(this, left.type)
  ): ExpressionNode {
    return assignment(left, right, node)
  }

  private fun assignment(left: ExpressionNode, right: ExpressionNode, node: CstNode): ExpressionNode {
    return when (left) {
      is ReferenceNode -> {
        if (left.nullness == Nullness.NOT_NULL && right.nullness == Nullness.NULLABLE) {
          error(node, "Cannot assign nullable value $right to a non null variable")
        }
        val variable = left.variable
        try {
          checkVariableAccess(variable, node, checkSet = true)
        } catch (e: VariableAccessException) {
          error(left, e.message)
        }
        VariableAssignmentNode(
          variable,
          cast(variable.type, right), left.owner, node
        )
      }

      is GetAtFunctionCallNode -> {
        val owner = left.ownerNode
        val arguments = left.arguments + right
        val isSafeAccess = left.javaMethod.name == GET_AT_SAFE_METHOD_NAME
        val putAtMethod = symbolResolver.findMethodOrThrow(
          owner.type,
          if (isSafeAccess) PUT_AT_SAFE_METHOD_NAME else PUT_AT_METHOD_NAME,
          arguments,
          node.token
        )
        fCall(method = putAtMethod, owner = owner, arguments = arguments, node = node)
      }

      is ArrayAccessNode -> {
        val owner = left.owner
        val elementType = owner.type.asArrayType.elementsType
        ArrayIndexAssignmentNode(
          owner,
          cast(JavaType.int, left.indexNode),
          cast(elementType, right),
          node
        )
      }
      else -> return exprError(node, "Invalid assignment operator use", left.type)
    }
  }

  private fun dotOperator(
    node: CstNode,
    // owner is actually the left operand
    owner: ExpressionNode, rightOperand: ExpressionCstNode,
    // useful for ternaryNode which duplicate value to avoid using local variable
    discardOwnerInReturned: Boolean = false,
    smartCastType: JavaType? = null
  ): ExpressionNode {
    return when (rightOperand) {
      is FunctionCallCstNode -> {
        val positionalArguments = rightOperand.positionalArgumentNodes.map { it.accept(this) }
        val namedArguments = rightOperand.namedArgumentNodes.map { Pair(it.first, it.second.accept(this)) }
        val (method, arguments) = methodResolver.resolveMethod(
          node, owner.type, rightOperand.value,
          positionalArguments,
          namedArguments
        )
          ?: return exprError(
            node.token,
            MethodResolver.methodResolveErrorMessage(positionalArguments, namedArguments, owner.type, rightOperand.value),
            smartCastType
          )
        val castType = rightOperand.castType?.let { resolve(it) }
        fCall(
          method = method,
          owner = if (discardOwnerInReturned || method.isMarcelStatic) null else owner,
          castType = castType,
          arguments = arguments,
          // this is important for code highlight
          tokenStart = rightOperand.tokenStart,
          tokenEnd = rightOperand.tokenEnd
        )
      }

      is ReferenceCstNode -> {
        val variable = symbolResolver.findFieldOrThrow(owner.type, rightOperand.value, rightOperand.token)
        try {
          checkVariableAccess(variable, node)
        } catch (e: VariableAccessException) {
          error(rightOperand, e.message)
        }
        ReferenceNode(
          if (discardOwnerInReturned || variable.isMarcelStatic) null else owner,
          variable,
          rightOperand.token
        )
      }

      is DirectFieldReferenceCstNode -> {
        val variable = symbolResolver.getClassField(owner.type, rightOperand.value, rightOperand.token)
        try {
          checkVariableAccess(variable, node)
        } catch (e: VariableAccessException) {
          error(rightOperand, e.message)
        }
        ReferenceNode(
          if (discardOwnerInReturned || variable.isMarcelStatic) null else owner,
          variable,
          rightOperand.token
        )
      }

      is IndexAccessCstNode -> {
        val indexOwner = dotOperator(node, owner, rightOperand.ownerNode, false, smartCastType)
        indexAccess(indexOwner, rightOperand)
      }

      is IncrCstNode -> {
        val variable = symbolResolver.findFieldOrThrow(owner.type, rightOperand.value, rightOperand.token)
        incr(rightOperand, variable, owner)
      }

      else -> exprError(node, "Invalid dot operator use" + rightOperand.javaClass, smartCastType)
    }
  }

  private fun staticDotOperator(
    node: CstNode,
    ownerType: JavaType,
    rightOperand: ExpressionCstNode,
    smartCastType: JavaType? = null
  ): ExpressionNode {
    return when (rightOperand) {
      is FunctionCallCstNode -> {
        val (method, arguments) = methodResolver.resolveMethod(node, ownerType, rightOperand.value,
          rightOperand.positionalArgumentNodes.map { it.accept(this) },
          rightOperand.namedArgumentNodes.map { Pair(it.first, it.second.accept(this)) })
          ?: return exprError(node.token, "Method ${ownerType}.${rightOperand.value} couldn't be resolved", smartCastType)
        val castType = rightOperand.castType?.let { resolve(it) }
        if (!method.isStatic) return exprError(node, "Method $method is not static", method.returnType)
        fCall(
          method = method, owner = null, castType = castType,
          arguments = arguments, node = node
        )
      }

      is ReferenceCstNode -> {
        val variable = symbolResolver.findFieldOrThrow(ownerType, rightOperand.value, rightOperand.token)
        if (!variable.isStatic) return exprError(node, "Variable $variable is not static", variable.type)
        try {
          checkVariableAccess(variable, node)
        } catch (e: VariableAccessException) {
          error(rightOperand, e.message)
        }
        ReferenceNode(null, variable, rightOperand.token)
      }

      is IncrCstNode -> {
        val variable = symbolResolver.findFieldOrThrow(ownerType, rightOperand.value, rightOperand.token)
        if (!variable.isStatic) return exprError(node, "Variable $variable is not static", variable.type)
        try {
          checkVariableAccess(variable, node, checkGet = true, checkSet = true)
        } catch (e: VariableAccessException) {
          error(rightOperand, e.message)
        }
        incr(rightOperand, variable, owner = null)
      }
      else -> return exprError(node, "Invalid dot operator use", smartCastType)
    }

  }
  override fun visit(node: BinaryTypeOperatorCstNode, smartCastType: JavaType?): ExpressionNode {
    val right = resolve(node.rightOperand)
    val left = node.leftOperand.accept(this, right)

    return when (val tokenType = node.tokenType) {
      TokenType.AS -> cast(right, left)
      TokenType.INSTANCEOF, TokenType.NOT_INSTANCEOF -> {
        if (left.type.primitive || right.primitive) return exprError(
          left.token,
          "Primitive aren't instance of anything", JavaType.boolean
        )
        val instanceOfNode = InstanceOfNode(right, left, node)
        if (tokenType == TokenType.NOT_INSTANCEOF) NotNode(instanceOfNode, node) else instanceOfNode
      }

      else -> return exprError(node, "Doesn't handle operator ${node.tokenType}", smartCastType)
    }
  }

  override fun visit(node: ElvisThrowCstNode, smartCastType: JavaType?): ExpressionNode {
    val expr = node.expression.accept(this)
    return fCall(
      node = node, ownerType = BytecodeHelper::class.javaType, name = "elvisThrow",
      arguments = listOf(
        cast(expr.type.objectType, expr),
        cast(Throwable::class.javaType, node.throwableException.accept(this))
      )
    )
  }

  private fun comparisonOperatorNode(
    leftOperand: ExpressionCstNode,
    rightOperand: ExpressionCstNode,
    nodeCreator: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode
  ): ExpressionNode {
    var left = leftOperand.accept(this)
    var right = rightOperand.accept(this)

    if (!left.type.primitive || !right.type.primitive) {
      // compare left.compareTo(right) with 0
      if (!left.type.implements(Comparable::class.javaType)) {
        return exprError(leftOperand, "Cannot compare non comparable type", JavaType.int)
      }
      left =
        fCall(owner = left, ownerType = left.type, name = "compareTo", arguments = listOf(right), node = leftOperand)
      right = IntConstantNode(leftOperand.token, 0)
    }

    val type = if (left.type != JavaType.int) right.type else left.type
    return nodeCreator.invoke(cast(type, left), cast(type, right))
  }

  private fun equalityComparisonOperatorNode(
    leftOperand: ExpressionCstNode,
    rightOperand: ExpressionCstNode,
    nodeCreator: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode,
    objectComparisonNodeCreator: (ExpressionNode, ExpressionNode) -> ExpressionNode
  ): ExpressionNode {
    val left = leftOperand.accept(this)
    val right = rightOperand.accept(this)

    return if (left.type.primitive && right.type.primitive) {
      val commonType = JavaType.commonType(left.type, right.type)
      nodeCreator.invoke(cast(commonType, left), cast(commonType, right))
    } else if (left.type.primitive && !right.type.primitive || !left.type.primitive && right.type.primitive) {
      if (!left.type.isPrimitiveObjectType || !left.type.isPrimitiveObjectType) {
        return exprError(leftOperand.token, "Cannot compare ${left.type} with ${right.type}", JavaType.boolean)
      }
      // getting the object type (not primitive because as one is originally an object, it can be null)
      val leftType = left.type.asPrimitiveType.objectType
      val rightType = right.type.asPrimitiveType.objectType
      val commonType = JavaType.commonType(leftType, rightType)
      nodeCreator.invoke(
        cast(commonType, cast(leftType, left)),
        cast(commonType, cast(rightType, right))
      )
    } else if (left is NullValueNode || right is NullValueNode) {
      nodeCreator.invoke(left, right)
    } else {
      objectComparisonNodeCreator.invoke(cast(JavaType.Object, left), cast(JavaType.Object, right))
    }
  }

  private fun numberComparisonOperatorNode(
    leftOperand: ExpressionCstNode,
    rightOperand: ExpressionCstNode,
    nodeCreator: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode
  ): ExpressionNode {
    val left = leftOperand.accept(this)
    val right = rightOperand.accept(this)

    if (left.type == JavaType.boolean || right.type == JavaType.boolean) {
      return exprError(leftOperand, "Cannot compare non number primitives", JavaType.int)
    }
    return comparisonOperatorNode(leftOperand, rightOperand, nodeCreator)
  }

  private fun rangeNode(
    leftOperand: ExpressionCstNode,
    rightOperand: ExpressionCstNode,
    methodName: String
  ): ExpressionNode {
    val left = leftOperand.accept(this)
    val right = rightOperand.accept(this)

    val rangeElementType =
      if (left.type == JavaType.Long || left.type == JavaType.long || right.type == JavaType.Long || right.type == JavaType.long) JavaType.long
      else if (left.type == JavaType.Integer || left.type == JavaType.int || right.type == JavaType.Integer || right.type == JavaType.int) JavaType.int
      else return exprError(leftOperand, "Ranges can only be of int or long", IntRange::class.javaType)

    val rangeType = if (rangeElementType == JavaType.long) LongRanges::class.javaType else IntRanges::class.javaType

    val method = symbolResolver.findMethodOrThrow(rangeType, methodName, listOf(rangeElementType, rangeElementType))
    return fCall(method = method, arguments = listOf(left, right), node = leftOperand)
  }

  private fun shiftOperator(
    leftOperand: ExpressionCstNode, rightOperand: ExpressionCstNode,
    operatorMethodName: String,
    nodeSupplier: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode,
    smartCastType: JavaType?
  ): ExpressionNode {
    val left = leftOperand.accept(this, if (operatorMethodName == "leftShift") smartCastType else null)
    val right = rightOperand.accept(this)
    val node = arithmeticBinaryOperator(left, right, operatorMethodName, nodeSupplier)
    if (JavaType.commonType(
        left,
        right
      ).isPrimitiveOrObjectPrimitive && node.type.primitive && node.type != JavaType.long && node.type != JavaType.int
    ) {
      return exprError(node.token, "Can only shift ints or longs", JavaType.int)
    }
    return node
  }

  private inline fun arithmeticBinaryOperator(
    leftOperand: ExpressionCstNode, rightOperand: ExpressionCstNode,
    operatorMethodName: String,
    nodeSupplier: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode
  ) = arithmeticBinaryOperator(leftOperand.accept(this), rightOperand.accept(this), operatorMethodName, nodeSupplier)

  private inline fun arithmeticAssignmentBinaryOperator(
    leftOperand: ExpressionCstNode, rightOperand: ExpressionCstNode,
    operatorMethodName: String, nodeSupplier: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode
  ): ExpressionNode = useInnerScope {
    var left = leftOperand.accept(this)

    val right = if (left is OwnableAstNode && left.owner != null) {
      // for owned node, in order to prevent evaluating twice the owner, we have to store it into a local variable
      val owner = left.owner!!
      val lv = it.addLocalVariable(owner.type, owner.nullness)
      // assign the local variable while pushing the owner for the variable store instruction
      left = left.withOwner(VariableAssignmentNode(node = leftOperand, variable = lv, owner = null, expression = owner))
      arithmeticBinaryOperator(
        left = left.withOwner(ReferenceNode(variable = lv, token = left.token)),
        right = rightOperand.accept(this, left.type),
        operatorMethodName, nodeSupplier
      )
    } else {
      arithmeticBinaryOperator(
        left = left,
        right = rightOperand.accept(this, left.type),
        operatorMethodName, nodeSupplier
      )
    }
    return@useInnerScope assignment(left = left, right = right, node = leftOperand)
  }

  private inline fun arithmeticBinaryOperator(
    left: ExpressionNode, right: ExpressionNode,
    operatorMethodName: String,
    nodeSupplier: (ExpressionNode, ExpressionNode) -> BinaryOperatorNode
  ): ExpressionNode {
    val commonType = JavaType.commonType(left, right)
    return if (commonType.isPrimitiveOrObjectPrimitive) {
      val commonPrimitiveType = commonType.asPrimitiveType
      if (!commonPrimitiveType.isNumber) return exprError(
        left.token,
        "Cannot apply operator on non number types",
        commonType
      )
      nodeSupplier.invoke(cast(commonPrimitiveType, left), cast(commonPrimitiveType, right))
    } else if (left.type == JavaType.String) {
      return StringNode(listOf(left, right))
    } else {
      val arguments = listOf(right)
      val method = symbolResolver.findMethodOrThrow(left.type, operatorMethodName, arguments, left.token)
      fCall(
        method = method,
        owner = left,
        castType = null,
        arguments = arguments,
        tokenStart = left.tokenStart,
        tokenEnd = right.tokenEnd
      )
    }
  }

  override fun visit(node: ReferenceCstNode, smartCastType: JavaType?): ExpressionNode {
    try {
      return referenceOrThrow(node, smartCastType)
    } catch (e: VariableRelatedException) {
      return exprError(node, e.message)
    }
  }

  private fun referenceOrThrow(node: ReferenceCstNode, smartCastType: JavaType?): ReferenceNode {
    // findVariableAndOwner and checkVariableAccess take care of the throwing
    val (variable, owner) = findVariableAndOwner(node.value, node)
    checkVariableAccess(
      variable, node,
      // we pass the void type to tell not to check get access (see assignment operator)
      checkGet = smartCastType != JavaType.void
    )

    return ReferenceNode(owner, variable, node.token)
  }

  private fun findVariableAndOwner(name: String, node: CstNode): Pair<Variable, ExpressionNode?> {
    val localVariable = currentScope.findLocalVariable(name)
    if (localVariable != null) {
      return Pair(localVariable, null)
    }
    val field = currentScope.findField(name)
    if (field != null) {
      val classType = currentScope.classType
      val thisNode = ThisReferenceNode(classType, node.token)
      return if (!field.isMarcelStatic) {
        val owner =
          if (field.owner.isAssignableFrom(classType)) thisNode
          else {
            val (outerLevel, _) = outerLevel(node.token, currentScope.classType, field.owner)
              ?: throw RuntimeException("Compiler error. Couldn't get outer level")

            val variable = currentScope.findField("this$$outerLevel")
              ?: throw RuntimeException("Compiler error. Couldn't get outer level field")

            ReferenceNode(owner = thisNode, variable = variable, token = node.token)
          }
        Pair(field, owner)
      } else Pair(field, null)
    }
    if (!currentMethodScope.staticContext && currentScope.classType.implements(Delegable::class.javaType)) {
      val owner = fCall(
        name = "getDelegate",
        owner = ThisReferenceNode(currentScope.classType, node.token),
        arguments = emptyList(),
        node = node
      )
      val delegatedField = symbolResolver.findField(owner.type, name)
      if (delegatedField != null && !delegatedField.isMarcelStatic) {
        return Pair(delegatedField, owner)
      }
    }
    throw VariableNotFoundException(node.token, "Variable $name is not defined")
  }

  final override fun visit(node: FunctionCallCstNode, smartCastType: JavaType?): ExpressionNode {
    val positionalArguments = node.positionalArgumentNodes.map { it.accept(this) }
    val namedArguments = node.namedArgumentNodes.map { Pair(it.first, it.second.accept(this)) }

    val castType = node.castType?.let(this::resolve)

    return resolveMethodCall(node, positionalArguments, namedArguments, castType)
      ?: exprError(node.token, "Method with name ${node.value} couldn't be resolved", smartCastType)
  }

  protected open fun resolveMethodCall(
    node: FunctionCallCstNode, positionalArguments: List<ExpressionNode>,
    namedArguments: List<Pair<String, ExpressionNode>>,
    castType: JavaType?
  ): ExpressionNode? {
    val currentScopeType = currentScope.classType
    var methodResolve =
      methodResolver.resolveMethod(node, currentScopeType, node.value, positionalArguments, namedArguments)
        ?: methodResolver.resolveMethodFromImports(
          node,
          node.value,
          positionalArguments,
          namedArguments,
          currentMethodScope.importResolver
        )

    if (methodResolve != null
      // need this especially for extensions classes to avoid referencing an instance method even though only static methods can be referenced. In this case we want the real static, not isMarcelStatic
      && (!currentMethodScope.staticContext || methodResolve.first.isStatic)
    ) {
      val owner =
        if (methodResolve.first.isMarcelStatic) null else ThisReferenceNode(currentScope.classType, node.token)
      return fCall(
        tokenStart = node.tokenStart,
        tokenEnd = node.tokenEnd,
        methodResolve = methodResolve,
        owner = owner,
        castType = castType
      )
    }

    if (currentScopeType.outerTypeName != null) {
      // searching on outer classes
      var outerLevel = 0
      var outerType = currentScopeType.outerTypeName?.let { symbolResolver.of(it, emptyList(), node.token) }
      while (outerType != null) {
        methodResolve = methodResolver.resolveMethod(node, outerType, node.value, positionalArguments, namedArguments)
        if (methodResolve != null) {
          val owner = if (methodResolve.first.isMarcelStatic) null else
            currentScope.findField("this$$outerLevel")?.let {
              ReferenceNode(
                owner = ThisReferenceNode(currentScopeType, node.token),
                variable = it,
                token = node.token
              )
            }
              ?: throw RuntimeException("Compiler error. Couldn't get outer level field")
          return fCall(
            tokenStart = node.tokenStart,
            tokenEnd = node.tokenEnd,
            methodResolve = methodResolve,
            owner = owner,
            castType = castType
          )

        }
        outerType = outerType.outerTypeName?.let { symbolResolver.of(it, emptyList(), node.token) }
        outerLevel++
      }
    }
    // look for delegate if any
    if (currentScope.classType.isScript) {
      val delegateGetter = symbolResolver.findMethod(currentScope.classType, "getDelegate", emptyList())
      if (delegateGetter != null) {
        methodResolve =
          methodResolver.resolveMethod(node, delegateGetter.returnType, node.value, positionalArguments, namedArguments)
        if (methodResolve != null) {
          val owner = fCall(
            method = delegateGetter,
            arguments = emptyList(),
            owner = ThisReferenceNode(currentScope.classType, node.token),
            tokenStart = node.tokenStart,
            tokenEnd = node.tokenEnd
          )
          return fCall(
            methodResolve = methodResolve, owner = owner, castType = castType,
            tokenStart = node.tokenStart,
            tokenEnd = node.tokenEnd,
          )
        }
      }
    }

    if (!currentMethodScope.staticContext && currentScopeType.implements(Delegable::class.javaType)) {
      val owner = fCall(
        name = "getDelegate",
        owner = ThisReferenceNode(currentScopeType, node.token),
        arguments = emptyList(),
        node = node
      )
      methodResolve = methodResolver.resolveMethod(node, owner.type, node.value, positionalArguments, namedArguments)
      if (methodResolve != null) {
        return fCall(
          methodResolve = methodResolve, owner = owner, castType = castType,
          tokenStart = node.tokenStart, tokenEnd = node.tokenEnd
        )
      }
    }
    // searching on extension class if it is one
    val extensionType = currentScope.forExtensionType
    val selfLocalVariable = this.selfLocalVariable
    if (extensionType != null) {
      methodResolve = methodResolver.resolveMethod(node, extensionType, node.value, positionalArguments, namedArguments)
      if (methodResolve != null) {
        val owner = if (methodResolve.first.isMarcelStatic) null
        else {
          if (selfLocalVariable == null) {
            return exprError(node, "Instance method ${methodResolve.first} cannot be referenced from a static context", castType)
          }
          ReferenceNode(
            variable = selfLocalVariable, token = node.token
          )
        }
        return fCall(
          methodResolve = methodResolve, owner = owner, castType = castType,
          tokenStart = node.tokenStart,
          tokenEnd = node.tokenEnd
        )
      }
    }
    return null
  }

  override fun visit(node: SuperConstructorCallCstNode, smartCastType: JavaType?): ExpressionNode {
    val superType = currentScope.classType.superType!!
    val (superConstructorMethod, arguments) = methodResolver.resolveConstructorCallOrThrow(node, superType,
      node.positionalArgumentNodes.map { it.accept(this) },
      node.namedArgumentNodes.map { it.first to it.second.accept(this) })

    return SuperConstructorCallNode(
      superType,
      superConstructorMethod,
      castedArguments(superConstructorMethod, arguments),
      node.tokenStart,
      node.tokenEnd
    )
  }

  override fun visit(node: ThisConstructorCallCstNode, smartCastType: JavaType?): ExpressionNode {
    val classType = currentScope.classType
    val (constructorMethod, arguments) = methodResolver.resolveConstructorCallOrThrow(node, classType,
      node.positionalArgumentNodes.map { it.accept(this) },
      node.namedArgumentNodes.map { it.first to it.second.accept(this) })

    return ThisConstructorCallNode(
      classType,
      constructorMethod,
      castedArguments(constructorMethod, arguments),
      node.tokenStart,
      node.tokenEnd
    )
  }

  override fun visit(node: ExpressionStatementCstNode) = ExpressionStatementNode(
    node.expressionNode.accept(
      this,
      // void to specify we won't use the returned value
      JavaType.void
    ), node.tokenStart, node.tokenEnd
  )

  override fun visit(node: ReturnCstNode): StatementNode {
    val scope = currentMethodScope
    val expectedReturnType = scope.method.let { if (it.isAsync) it.asyncReturnType!! else it.returnType }
    val expression = node.expressionNode?.accept(this, expectedReturnType)?.let { cast(expectedReturnType, it) }

    if (expression != null && expression.type != JavaType.void && expectedReturnType == JavaType.void) {
      return stmtError(node, "Cannot return expression in void function")
    } else if (expression == null && expectedReturnType != JavaType.void) {
      return ReturnStatementNode(exprError(node, "Must return expression in non void function", expectedReturnType))
    }
    return ReturnStatementNode(expression, node.tokenStart, node.tokenEnd)
  }

  override fun visit(node: VariableDeclarationCstNode): StatementNode {
    val variable = currentMethodScope.addLocalVariable(resolve(node.type), node.value, Nullness.of(node.isNullable), token = node.token)
    try {
      checkVariableAccess(variable, node, checkSet = true)
    } catch (e: VariableAccessException) {
      error(node, e.message)
    }
    return ExpressionStatementNode(
      VariableAssignmentNode(variable,
        node.expressionNode?.accept(this, variable.type)?.let { cast(variable.type, it) }
          ?: variable.type.getDefaultValueExpression(node.token), null, node.tokenStart, node.tokenEnd, node.variableToken)
    )
  }

  override fun visit(node: MultiVarDeclarationCstNode): StatementNode {
    if (node.declarations.isEmpty() || node.declarations.all { it == null }) {
      return stmtError(node, "Need to declare at least one variable")
    }
    // needed for switch/whens, which need a smartCastType to work
    val expression = node.expressionNode.accept(this, List::class.javaType)
    val blockNode = BlockStatementNode(mutableListOf(), node.tokenStart, node.tokenEnd)
    currentMethodScope.useTempLocalVariable(expression.type, expression.nullness) { expressionVariable: LocalVariable ->
      val expressionRef = ReferenceNode(variable = expressionVariable, token = node.token)

      // put the expression in a local variable
      blockNode.statements.add(
        ExpressionStatementNode(
          VariableAssignmentNode(
            localVariable = expressionVariable,
            expression = expression,
            node = node
          )
        )
      )

      // declare all variables
      val variableMap = mutableMapOf<Int, LocalVariable>()
      node.declarations.forEachIndexed { index, triple ->
        if (triple != null) variableMap[index] =
          currentMethodScope.addLocalVariable(resolve(triple.first), triple.second, Nullness.of(triple.third), token = node.token)
      }
      // then assign
      when {
        expressionVariable.type.implements(List::class.javaType) -> {
          val getAtMethod =
            symbolResolver.findMethodOrThrow(expressionVariable.type, GET_AT_METHOD_NAME, listOf(JavaType.int))
          variableMap.forEach { (index, variable) ->
            blockNode.statements.add(
              ExpressionStatementNode(
                VariableAssignmentNode(
                  localVariable = variable,
                  expression = fCall(
                    method = getAtMethod,
                    arguments = listOf(IntConstantNode(node.token, index)),
                    owner = expressionRef,
                    node = node,
                    castType = variable.type
                  ),
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
                  expression = cast(
                    variable.type,
                    ArrayAccessNode(
                      expressionRef,
                      IntConstantNode(node.token, index),
                      node
                    )
                  ),
                  node = node
                )
              )
            )
          }
        }

        expression.type.implements(Map.Entry::class.javaType) -> {
          if (node.declarations.size != 2) {
            return stmtError(node, "multi var declaration of Map.Entry should declare 2 variables")
          }
          for (i in 0..1) {
            val variable = variableMap[i]!!
            blockNode.statements.add(
              ExpressionStatementNode(
                VariableAssignmentNode(
                  localVariable = variable,
                  expression = fCall(
                    method = symbolResolver.findMethodOrThrow(
                      expression.type,
                      if (i == 0) "getKey" else "getValue",
                      emptyList(),
                      token = node.token
                    ),
                    arguments = emptyList(), owner = expressionRef, node = node, castType = variable.type
                  ),
                  node = node
                )
              )
            )
          }
        }

        else -> return stmtError(node, "Multi variable declarations only works on List or arrays")
      }
    }
    return blockNode
  }

  override fun visit(node: IfStatementCstNode): IfStatementNode {
    val (condition, trueStatement) = useInnerScope {
      Pair(truthyCast(node.condition.accept(this)), node.trueStatementNode.accept(this))
    }
    val falseStatement = useInnerScope { node.falseStatementNode?.accept(this) }

    return IfStatementNode(condition, trueStatement, falseStatement, node)
  }

  fun visit(node: IfStatementCstNode, smartCastType: JavaType?): IfStatementNode {
    if (smartCastType == null) return visit(node)
    val (condition, trueStatement) = useInnerScope {
      Pair(truthyCast(node.condition.accept(this)), visitSmartCast(node.trueStatementNode, smartCastType))
    }
    val falseStatement = useInnerScope { node.falseStatementNode?.let { visitSmartCast(it, smartCastType) } }
    return IfStatementNode(condition, trueStatement, falseStatement, node)
  }

  // allow to pass smartCast to statements. Useful for nested switches/whens
  private fun visitSmartCast(stmtNode: StatementCstNode, smartCastType: JavaType): StatementNode {
    return when (stmtNode) {
      is ExpressionStatementCstNode -> ExpressionStatementNode(stmtNode.expressionNode.accept(this, smartCastType))
      // because when/switches are transformed to ifs
      is IfStatementCstNode -> visit(stmtNode, smartCastType)
      else -> stmtNode.accept(this)
    }
  }

  override fun visit(node: SwitchCstNode, smartCastType: JavaType?): ExpressionNode {
    // transforming the switch into a when
    val switchExpression = node.switchExpression.accept(this)
    return switchWhen(node, smartCastType, switchExpression, node.varDeclaration)
  }

  /**
   * Predefine the lambda. This methods just creates the lambda class node and return a constructor call of it.
   * The lambda class will be constructed and sematically checked later, as we first we to continue the analysis
   * to get potential wanted cast types of the lambda
   */
  override fun visit(node: LambdaCstNode, smartCastType: JavaType?): ExpressionNode {
    // search for already generated lambdaNode if not empty
    val lambdaOuterClassNode = currentClassNode

    if (lambdaOuterClassNode != null) {
      val alreadyExistingLambdaNode = lambdaMap[node]
      if (alreadyExistingLambdaNode != null) {
        return alreadyExistingLambdaNode.constructorCallNode
      }
      val lambdaNode = createLambdaNode(node, lambdaOuterClassNode, smartCastType)
      lambdaMap[node] = lambdaNode
      val constructorCallNode = NewLambdaInstanceNode(
        lambdaNode.type, lambdaNode.constructorNode,
        // this part is important, as we will compute the constructorParameters later
        lambdaNode.constructorArguments, lambdaNode, node.token
      )
      lambdaNode.constructorCallNode = constructorCallNode
      return constructorCallNode
    } else {
      // part for top level lambdas, that are lambda defined in annotations, for a Class attribute
      val alreadyExistingLambdaNode = lambdaMap[node]
      if (alreadyExistingLambdaNode != null) {
        return ClassReferenceNode(alreadyExistingLambdaNode.type, node.token)
      }
      // smartCastType = null because we actually expect a Class here, and that is not something the lambda should implement
      val lambdaNode = createLambdaNode(node, lambdaOuterClassNode = null, smartCastType = null)
      defineLambda(lambdaNode)
      lambdaMap[node] = lambdaNode
      return ClassReferenceNode(lambdaNode.type, node.token)
    }
  }

  private fun createLambdaNode(node: LambdaCstNode, lambdaOuterClassNode: ClassNode?, smartCastType: JavaType?): LambdaClassNode {
    val interfaceType =
      if (smartCastType != null && !Lambda::class.javaType.isAssignableFrom(smartCastType)) smartCastType else null

    val lambdaClassName =
      if (lambdaOuterClassNode != null) generateLambdaClassName(lambdaOuterClassNode)
      else generateTopLevelLambdaClassName(node)

    // useful for method type resolver, when matching method parameters.
    val lambdaImplementedInterfaces = listOf(Lambda::class.javaType)
    val lambdaType = symbolResolver.defineType(
      node.token,
      Visibility.INTERNAL,
      if (lambdaOuterClassNode != null) "${lambdaOuterClassNode.type}\$$lambdaClassName"
      else lambdaClassName,
      JavaType.Object,
      isInterface = false,
      lambdaImplementedInterfaces
    )

    val lambdaConstructor = MethodNode(
      name = MarcelMethod.CONSTRUCTOR_NAME,
      visibility = Visibility.INTERNAL,
      returnType = JavaType.void,
      isStatic = false,
      tokenStart = node.tokenStart,
      tokenEnd = node.tokenEnd,
      parameters = mutableListOf(),
      nullness = Nullness.NOT_NULL,
      ownerClass = lambdaType
    )
    val parameters =
      node.parameters.map { param -> LambdaClassNode.MethodParameter(param.type?.let { resolve(it) },
        if (param.type != null) Nullness.of(param.nullable) else Nullness.UNKNOWN,
        param.name) }

    val lambdaNode = LambdaClassNode(
      lambdaType,
      lambdaConstructor,
      isStatic = lambdaOuterClassNode?.isStatic ?: false,
      node,
      fileName = fileName,
      parameters,
      (currentScope as? MethodScope)?.localVariablesSnapshot ?: emptyList()
    ).apply {
      interfaceType?.let { interfaceTypes.add(it) }
    }
    lambdaConstructor.blockStatement.addAll(
      listOf(
        ExpressionStatementNode(superNoArgConstructorCall(lambdaNode, symbolResolver)),
        returnVoid(lambdaNode)
      )
    )
    if (lambdaOuterClassNode != null) {
      classNodeMap[lambdaNode.type] = lambdaNode
      // handling inner class fields if any
      handleLambdaInnerClassFields(lambdaNode, lambdaConstructor, lambdaNode.constructorArguments, node.token)
      lambdaOuterClassNode.innerClasses.add(lambdaNode)
    }
    return lambdaNode
  }

  private fun generateTopLevelLambdaClassName(lambdaCstNode: LambdaCstNode): String {
    return currentScope.classType.simpleName + "_lambda" + lambdaMap.size
  }

  /**
   * Define the lambda. If we wanted a particular (non lambda) interface, it will implement it.
   * Otherwise, it will implement the lambda object
   */
  protected fun defineLambda(lambdaNode: LambdaClassNode): Unit =
    useScope(ClassScope(symbolResolver, lambdaNode.type, null, imports)) { classScope ->
      if (lambdaNode.interfaceTypes.size > 1) {
        throw MarcelSemanticException(
          lambdaNode.token,
          "Expected lambda to be of multiple types: " + lambdaNode.interfaceTypes
        )
      }
      val interfaceType =
        if (lambdaNode.interfaceTypes.size == 1) lambdaNode.interfaceTypes.first()
        else null
      interfaceType?.let {
        lambdaNode.type.directlyImplementedInterfaces.remove(Lambda::class.javaType) // not needed anymore
        lambdaNode.type.addImplementedInterface(it)
      }
      val methodParameters = computeLambdaParameters(lambdaNode, interfaceType)
      val lambdaMethodScope: LambdaMethodScope
      // because we don't want to match on Lambda, Lambda0, Lambda1, ...
      if (interfaceType != null && !interfaceType.className.startsWith("marcel.lang.lambda.Lambda")) {
        val interfaceMethod = symbolResolver.getInterfaceLambdaMethodOrThrow(interfaceType, lambdaNode.token)
        val lambdaParameters = mutableListOf<MethodParameter>()
        interfaceMethod.parameters.forEachIndexed { index, _ ->
          lambdaParameters.add(MethodParameter(methodParameters[index].type, methodParameters[index].nullness, methodParameters[index].name))
        }
        val interfaceMethodNode = MethodNode(
          interfaceMethod.name,
          interfaceMethod.nullness,
          lambdaParameters,
          interfaceMethod.visibility,
          interfaceMethod.returnType,
          interfaceMethod.isStatic,
          lambdaNode.tokenStart,
          lambdaNode.tokenEnd,
          lambdaNode.type
        )

        lambdaMethodScope = LambdaMethodScope(classScope, interfaceMethodNode, lambdaNode.localVariablesSnapshot)
        var interfaceMethodBlockStatement = useScope(lambdaMethodScope) {
          lambdaNode.blockCstNode.accept(this) as BlockStatementNode
        }
        if (interfaceMethodNode.returnType != JavaType.void) {
          interfaceMethodBlockStatement =
            interfaceMethodBlockStatement.accept(ReturningBranchTransformer(lambdaNode.blockCstNode) {
              cast(
                interfaceMethodNode.returnType,
                it
              )
            }) as BlockStatementNode
        } else {
          interfaceMethodBlockStatement.statements.add(
            returnVoid(interfaceMethodBlockStatement)
          )
        }
        interfaceMethodNode.blockStatement.addAll(interfaceMethodBlockStatement.statements)
        lambdaNode.methods.add(interfaceMethodNode)
      } else {
        // defining the lambda "invoke" method
        val lambdaType = getLambdaType(lambdaNode, methodParameters)
        // needed because we don't want to add this twice
        if (interfaceType?.packageName != "marcel.lang.lambda") lambdaNode.type.addImplementedInterface(lambdaType)

        val lambdaMethod = symbolResolver.getInterfaceLambdaMethodOrThrow(lambdaType, lambdaNode.token)
        val lambdaMethodNode = MethodNode(
          lambdaMethod.name,
          lambdaMethod.nullness,
          methodParameters,
          lambdaMethod.visibility,
          lambdaMethod.returnType,
          lambdaMethod.isStatic,
          lambdaNode.tokenStart,
          lambdaNode.tokenEnd,
          lambdaNode.type
        )

        lambdaMethodScope = LambdaMethodScope(classScope, lambdaMethodNode, lambdaNode.localVariablesSnapshot)
        val blockStatement = useScope(lambdaMethodScope) {
          lambdaNode.blockCstNode.accept(this).accept(
            // need to cast to objectType because lambdas always return objects
            ReturningBranchTransformer(lambdaNode.blockCstNode) { cast(JavaType.Object, it) }
          ) as BlockStatementNode
        }
        lambdaMethodNode.blockStatement.addAll(blockStatement.statements)
        lambdaNode.methods.add(lambdaMethodNode)
      }

      /*
       * Examining referenced local variables so that we declare them as fields in the lambda class
       * and then pass them in the constructor call
       */
      val usedLocalVariables =
        lambdaMethodScope.usedOuterScopeLocalVariable.toList() // to list so that order is constant
      if (usedLocalVariables.isNotEmpty()) {
        val lambdaConstructor = lambdaNode.constructors.first()
        val constructorParameters = lambdaConstructor.parameters
        // + 1 because we are not in a static context
        var lvIndex = constructorParameters.sumOf { it.type.nbSlots } + 1
        for (lv in usedLocalVariables) {
          val field = FieldNode(
            lv.type,
            lv.name,
            lambdaNode.type,
            lv.nullness,
            mutableListOf(),
            true,
            Visibility.PRIVATE,
            false,
            lambdaNode.tokenStart,
            lambdaNode.tokenEnd
          )
          lambdaNode.fields.add(field)

          constructorParameters.add(MethodParameter(lv.type, lv.nullness, lv.name))
          lambdaConstructor.blockStatement.statements.add(lambdaConstructor.blockStatement.statements.size - 1, // -1 because last is return call
            ExpressionStatementNode(
              VariableAssignmentNode(
                owner = ThisReferenceNode(lambdaNode.type, lambdaNode.token),
                variable = field,
                expression = ReferenceNode(variable = lv.withIndex(lvIndex),
                  token = lambdaNode.token),
                tokenStart = lambdaNode.tokenStart,
                tokenEnd = lambdaNode.tokenEnd
              )
            )
          )
          lvIndex += lv.type.nbSlots
          lambdaNode.constructorArguments.add(ReferenceNode(variable = lv, token = lambdaNode.token))
        }
      }

      for (innerClass in lambdaNode.innerClasses) {
        if (innerClass is LambdaClassNode) {
          defineLambda(innerClass)
        }
      }
    }

  private fun computeLambdaParameters(
    lambdaNode: LambdaClassNode,
    interfaceType: JavaType?
  ): MutableList<MethodParameter> {
    if (interfaceType == null) {
      return if (lambdaNode.explicit0Parameters) mutableListOf()
      else if (lambdaNode.lambdaMethodParameters.isEmpty()) mutableListOf(MethodParameter(JavaType.Object, Nullness.UNKNOWN, "it"))
      else lambdaNode.lambdaMethodParameters.map { MethodParameter(it.type ?: JavaType.Object, it.nullness, it.name) }
        .toMutableList()
    }
    val method = symbolResolver.getInterfaceLambdaMethodOrThrow(interfaceType, lambdaNode.token)

    if (lambdaNode.explicit0Parameters) {
      if (method.parameters.isNotEmpty()) throw MarcelSemanticException(
        lambdaNode.token,
        "Lambda parameters mismatch. Expected parameters ${method.parameters}"
      )
      return mutableListOf()
    }
    if (lambdaNode.lambdaMethodParameters.isEmpty()) {
      if (method.parameters.size > 1) throw MarcelSemanticException(
        lambdaNode.token,
        "Lambda parameters mismatch. Expected parameters ${method.parameters}"
      )
      return method.parameters.map { MethodParameter(it.type, it.nullness, "it") }.toMutableList()
    }

    if (lambdaNode.lambdaMethodParameters.size != method.parameters.size) {
      throw MarcelSemanticException(
        lambdaNode.token,
        "Lambda parameters mismatch. Expected parameters ${method.parameters}"
      )
    }
    return lambdaNode.lambdaMethodParameters.mapIndexed { index, lambdaMethodParameter ->
      if (lambdaMethodParameter.type != null && !method.parameters[index].type.isAssignableFrom(lambdaMethodParameter.type!!)) {
        throw MarcelSemanticException(
          lambdaNode.token,
          "Type ${method.parameters[index].type} is not assignable to ${lambdaMethodParameter.type}"
        )
      }
      MethodParameter(lambdaMethodParameter.type ?: method.parameters[index].type,
        lambdaMethodParameter.nullness,
        lambdaMethodParameter.name)
    }.toMutableList()
  }

  override fun visit(node: AsyncBlockCstNode, smartCastType: JavaType?): ExpressionNode {
    if (currentMethodScope.isAsync) {
      return exprError(node, "Cannot start async context because current context is already async", smartCastType)
    }
    if (node.block.statements.isEmpty()) {
      return exprError(node, "async block need to have at least one statements", smartCastType)
    }

    /*
     * Looking for all local variables used from cst node as we'll need to pass them to the 'when' method
     */
    val referencedLocalVariables = mutableListOf<LocalVariable>()
    node.block.forEach { cstNode ->
      // not returning because we can still continue semantic analysis
      if (cstNode is ReturnCstNode) exprError(
        node,
        "Cannot have return statement in an async block"
      )
      else if ((cstNode is ReferenceCstNode || cstNode is IncrCstNode) && currentMethodScope.hasLocalVariable(cstNode.value.toString())) {
        referencedLocalVariables.add(currentMethodScope.findLocalVariable(cstNode.value.toString())!!)
      }
    }

    /*
     * generating method that will initialize Threadmill context, and execute the async block
     */
    val asyncMethodParameters = mutableListOf<MethodParameter>()
    val asyncMethodArguments = mutableListOf<ReferenceNode>()
    for (lv in referencedLocalVariables) {
      if (asyncMethodParameters.none { it.name == lv.name }) {
        asyncMethodParameters.add(MethodParameter(lv.type, lv.nullness, lv.name, isFinal = true))
        asyncMethodArguments.add(ReferenceNode(variable = lv, token = node.token))
      }
    }
    val asyncReturnType = smartCastType ?: JavaType.void
    val asyncMethodNode = generateOrGetMethod(
      ASYNC_METHOD_PREFIX, asyncMethodParameters,
      returnType = if (asyncReturnType == JavaType.void) JavaType.void else asyncReturnType.objectType, node, Nullness.UNKNOWN
    )

    compose(
      asyncMethodNode,
      AsyncScope(
        symbolResolver = symbolResolver,
        method = asyncMethodNode,
        originalScope = currentMethodScope,
      )
    ) { asyncScope ->
      // initializing Threadmill context. Needs to be declared BEFORE the block as we will initialize this variable
      // before running the block instructions
      val threadmillResourceVariable = asyncScope.addLocalVariable(Closeable::class.javaType, Nullness.NOT_NULL)

      // store return expression in local variable to be able to execute finally block
      val returnValueVar = if (asyncReturnType != JavaType.void) asyncScope.addLocalVariable(asyncMethodNode.returnType, Nullness.UNKNOWN)
      else null
      val asyncStatementTryBlock = visit(node.block).apply {
        if (asyncReturnType != JavaType.void) {
          val lastStatement = statements.last()
          val expressionNode = (lastStatement as? ExpressionStatementNode)?.expressionNode ?: exprError(
            lastStatement.token,
            "Expected an expression of type $asyncReturnType", asyncReturnType
          )
          statements[statements.lastIndex] = ExpressionStatementNode(
            VariableAssignmentNode(
              localVariable = returnValueVar!!,
              // always return an object
              expression = cast(asyncReturnType.objectType, expressionNode),
              node = node
            )
          )
        } else {
          statements.add(returnVoid(asyncMethodNode))
        }
      }

      // Closeable context = Threadmill.startNewContext()
      varAssignStmt(variable = threadmillResourceVariable, expr = fCall(
        node = node,
        ownerType = Threadmill::class.javaType,
        name = "startNewContext",
        arguments = emptyList()
      ))

      // try { runAsyncBlock() } finally { context.close() }
      stmt(TryNode(
        node = node,
        tryStatementNode = asyncStatementTryBlock,
        catchNodes = emptyList(),
        finallyNode = useInnerScope { finallyScope ->
          TryNode.FinallyNode(
            finallyScope.addLocalVariable(Throwable::class.javaType, Nullness.NOT_NULL), block(ExpressionStatementNode(
              fCall(
                node = node,
                owner = ReferenceNode(variable = threadmillResourceVariable, token = node.token),
                name = "close",
                arguments = emptyList()
              )
            )), returnValueVar
          )
        }
      ))

      if (asyncReturnType == JavaType.void) {
        returnVoidStmt()
      }
    }

    return fCall(
      node = node,
      owner = ThisReferenceNode(currentScope.classType, node.token),
      method = asyncMethodNode,
      arguments = asyncMethodArguments,
      castType = asyncReturnType
    )
  }

  override fun visit(node: WhenCstNode, smartCastType: JavaType?) = switchWhen(node, smartCastType)

  private fun switchWhen(
    node: WhenCstNode,
    smartCastType: JavaType?,
    switchExpression: ExpressionNode? = null,
    varDecl: VariableDeclarationCstNode? = null
  ): ExpressionNode {
    val shouldReturnValue = smartCastType != null && smartCastType != JavaType.void
    val elseStatement = node.elseStatement
    if (shouldReturnValue && elseStatement == null) {
      return exprError(node, "When/switch expression should have an else branch", smartCastType)
    }
    if (node.branches.isEmpty()) {
      if (elseStatement == null || shouldReturnValue) return exprError(
        node.token,
        "Switch/When should have at least 1 non else branch", smartCastType
      )
      node.branches.add(
        Pair(
          BoolCstNode(node.parent, false, node.token),
          BlockCstNode(emptyList(), node.parent, node.tokenStart, node.tokenEnd)
        )
      )
    }

    val whenReturnType = smartCastType ?: computeWhenReturnType(node)

    val switchExpressionRef = ReferenceCstNode(
      node.parent,
      varDecl?.value ?: ("__switch_expression" + node.hashCode().toString().replace('-', '_')),
      node.token
    )
    val switchExpressionLocalVariable =
      currentMethodScope.addLocalVariable(varDecl?.let { resolve(it.type) } ?: switchExpression?.type
      ?: JavaType.Object, switchExpressionRef.value, switchExpression?.nullness ?: Nullness.UNKNOWN, token = node.token)

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

    /*
     * Looking for all local variables used from cst node as we'll need to pass them to the 'when' method
     */
    val referencedLocalVariables =
      findAllReferencedLocalVariables(node, currentMethodScope, switchExpressionLocalVariable)

    val whenMethodParameters = mutableListOf<MethodParameter>()
    val whenMethodArguments = mutableListOf<ExpressionNode>()
    if (switchExpression != null) {
      whenMethodParameters.add(MethodParameter(switchExpressionLocalVariable.type, switchExpressionLocalVariable.nullness, switchExpressionLocalVariable.name))
      whenMethodArguments.add(switchExpression)
    }
    for (lv in referencedLocalVariables) {
      whenMethodParameters.add(MethodParameter(lv.type, lv.nullness, lv.name, isFinal = true))
      whenMethodArguments.add(ReferenceNode(variable = lv, token = node.token))
    }

    /*
     * generating method
     */
    val whenMethod = generateOrGetMethod(WHEN_METHOD_PREFIX, whenMethodParameters, whenReturnType, node, Nullness.UNKNOWN)
    val whenStatement = useScope(newMethodScope(whenMethod)) { visit(rootIfCstNode, smartCastType) }
    if (shouldReturnValue) {
      var tmpIfNode: IfStatementNode? = whenStatement
      val branchTransformer = ReturningWhenIfBranchTransformer(node) { cast(whenReturnType, it) }
      while (tmpIfNode != null) {
        tmpIfNode.trueStatementNode = tmpIfNode.trueStatementNode.accept(branchTransformer)
        if (tmpIfNode.falseStatementNode is IfStatementNode || tmpIfNode.falseStatementNode == null) {
          tmpIfNode = tmpIfNode.falseStatementNode as? IfStatementNode
        } else {
          tmpIfNode.falseStatementNode = tmpIfNode.falseStatementNode!!.accept(branchTransformer)
          break
        }
      }
    }

    whenMethod.blockStatement.apply {
      add(whenStatement)
      // if it is not void, statements already have return nodes because of ReturningWhenIfBranchTransformer
      if (whenReturnType == JavaType.void) add(returnVoid(this))
    }

    // dispose switch expression variable
    currentMethodScope.freeLocalVariable(switchExpressionLocalVariable.name)

    // now calling the method
    return fCall(
      node = node, owner = ThisReferenceNode(currentScope.classType, node.token),
      arguments = whenMethodArguments, method = whenMethod
    )
  }

  private fun findAllReferencedLocalVariables(
    node: WhenCstNode, scope: MethodScope,
    // variable to ignore
    switchLocalVariable: LocalVariable
  ): List<LocalVariable> {
    val localVariables = mutableSetOf<LocalVariable>()
    val consumer: (CstNode) -> Unit = { cstNode ->
      if (cstNode is ReferenceCstNode) {
        val lv = scope.findLocalVariable(cstNode.value)
        if (lv != null && lv.name != switchLocalVariable.name) localVariables.add(lv)
      }
    }
    for (branch in node.branches) {
      branch.first.forEach(consumer)
      branch.second.forEach(consumer)
    }
    node.elseStatement?.forEach(consumer)
    return localVariables.toList() // to list so that order is constant
  }

  private fun computeWhenReturnType(node: WhenCstNode): JavaType = useInnerScope {
    val branchTransformer = ReturningWhenIfBranchTransformer(node)
    node.branches.forEach {
      it.second.accept(this).accept(branchTransformer)
    }
    node.elseStatement?.accept(this)?.accept(branchTransformer)
    return@useInnerScope JavaType.commonType(branchTransformer.collectedTypes)
  }

  // TODO handle nullness in when/switch, asyncFunction, lambda, ... everything that generates a function. To stop passing UNKNOWN to this function
  private fun generateOrGetMethod(
    prefix: String,
    parameters: MutableList<MethodParameter>,
    returnType: JavaType,
    node: CstNode,
    nullness: Nullness,
    asyncReturnType: JavaType? = null
  ): MethodNode {
    val classType = currentScope.classType
    val classNode = classNodeMap.getValue(classType)
    val methodName = currentMethodScope.method.name + prefix + node.hashCode().toString().replace('-', '0')
    val existingMethodNode = classNode.methods.find { it.name == methodName }
    /// we don't want to define the same method twice, so we find it if we already registered it
    if (existingMethodNode != null) return existingMethodNode
    val methodNode = MethodNode(
      methodName, nullness, parameters, Visibility.PRIVATE, returnType,
      currentMethodScope.staticContext, asyncReturnType, node.tokenStart, node.tokenEnd, classType
    )
    symbolResolver.defineMethod(classType, methodNode)
    classNode.methods.add(methodNode)
    return methodNode
  }

  private fun toIf(
    it: Pair<ExpressionCstNode, StatementCstNode>,
    switchExpression: ExpressionNode?,
    switchExpressionRef: ExpressionCstNode,
    node: CstNode
  ): IfStatementCstNode {
    return if (switchExpression != null) IfStatementCstNode(
      BinaryOperatorCstNode(
        TokenType.EQUAL, switchExpressionRef, it.first, node.parent, node.tokenStart, node.tokenEnd
      ), it.second, null, it.first.parent, it.first.tokenStart, it.second.tokenEnd
    )
    else IfStatementCstNode(it.first, it.second, null, it.first.parent, node.tokenStart, node.tokenEnd)
  }

  override fun visit(node: WhileCstNode) = useScope(MethodInnerScope(currentMethodScope, isInLoop = true)) {
    val condition = truthyCast(node.condition.accept(this))
    val statement = node.statement.accept(this)
    WhileNode(node, condition, statement)
  }


  override fun visit(node: DoWhileStatementCstNode) = useScope(MethodInnerScope(currentMethodScope, isInLoop = true)) {
    val condition = truthyCast(node.condition.accept(this))
    val statement = node.statement.accept(this)
    DoWhileNode(node, condition, statement)
  }

  override fun visit(node: ForInCstNode) = useScope(MethodInnerScope(currentMethodScope, isInLoop = true)) {
    val variable = it.addLocalVariable(resolve(node.varType), node.varName, Nullness.of(node.isVarNullable), token = node.token)

    val inNode = node.inNode.accept(this)

    return@useScope if (inNode.type.isArray) forInArrayNode(
      node = node, forScope = it, inNode = inNode,
      forVariable = variable, iVar = it.addLocalVariable(JavaType.int, Nullness.NOT_NULL, token = node.token)
    ) { node.statementNode.accept(this) }
    else forInIteratorNode(node, it, variable, inNode) { node.statementNode.accept(this) }
  }

  override fun visit(node: ForInMultiVarCstNode): StatementNode {
    return useScope(MethodInnerScope(currentMethodScope, isInLoop = true)) {
      val inNode = node.inNode.accept(this)

      val localVars = node.declarations.map { triple ->
        it.addLocalVariable(resolve(triple.first), triple.second, Nullness.of(triple.third), token = node.token)
      }

      if (inNode.type.implements(JavaType.Map)) {
        if (localVars.size != 2) {
          return@useScope stmtError(node, "Needs 2 variables when iterating over maps")
        }
        val mapVar = it.addLocalVariable(Map::class.javaType, Nullness.NOT_NULL)
        val keyVar = localVars.first()
        val valueVar = localVars.last()

        compose(node, currentMethodScope) {
          // need to init map variable before going in the loop
          // valueVar = map[keyVar]. keyVar is initialized before, in MethodInstructionWriter
          varAssignStmt(variable = mapVar, expr = inNode)

          forInIteratorNodeStmt(forVariable = keyVar, inNode = fCall(
            node = node,
            owner = ReferenceNode(variable = mapVar, token = node.token),
            name = "keySet",
            arguments = emptyList()
          )) { forVar, scope ->
            // valueVar = map[keyVar]. keyVar is initialized before, in MethodInstructionWriter
            varAssignStmt(variable = valueVar,
              expr = fCall(
                castType = valueVar.type, owner = ReferenceNode(variable = mapVar, token = node.token),
                method = symbolResolver.findMethod(JavaType.Map, "get", listOf(JavaType.Object))!!,
                arguments = listOf(ReferenceNode(variable = keyVar, token = node.token)), node = node
              ))
            stmt(node.statementNode.accept(this@SemanticCstNodeVisitor))
          }
        }
      } else {
        return@useScope stmtError(
          node,
          "Cannot iterate multiple variables on an expression of type ${inNode.type}"
        )
      }
    }
  }

  override fun visit(node: TruthyVariableDeclarationCstNode, smartCastType: JavaType?): ExpressionNode {
    val variable = currentMethodScope.addLocalVariable(resolve(node.type), node.value, Nullness.UNKNOWN, token = node.token)
    var expression = node.expression.accept(this)
    /*
     * handle Optional unboxing
     */
    if (variable.type != Optional::class.javaType && !variable.type.primitive
      // comparing class name because we want to ignore generic types
      && expression.type.className == Optional::class.javaType.className
    ) {
      val nullNode = NullValueNode(node.token, variable.type)
      expression = fCall(
        node = node,
        method = symbolResolver.findMethod(Optional::class.javaType, "orElse", listOf(nullNode))!!,
        arguments = listOf(nullNode),
        owner = expression
      )
    } else if (variable.type == JavaType.Integer && expression.type == OptionalInt::class.javaType
      || variable.type == JavaType.Long && expression.type == OptionalLong::class.javaType
      || variable.type == JavaType.Double && expression.type == OptionalDouble::class.javaType
    ) {
      // no owner because method is static
      expression = fCall(
        node = node,
        method = symbolResolver.findMethod(BytecodeHelper::class.javaType, "orElseNull", listOf(expression))!!,
        arguments = listOf(expression)
      )
    }
    return VariableAssignmentNode(
      localVariable = variable,
      expression = cast(variable.type, expression),
      node = node,
      identifierToken = node.identifierToken
    )
  }

  override fun visit(node: ForVarCstNode): StatementNode =
    useScope(MethodInnerScope(currentMethodScope, isInLoop = true)) {
      val initStatement = node.varDecl.accept(this)
      val condition = truthyCast(node.condition.accept(this))
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
      return stmtError(node, "Cannot break outside of a loop")
    }
    return BreakNode(node)
  }

  override fun visit(node: ContinueCstNode): StatementNode {
    if (!currentInnerMethodScope.isInLoop) {
      return stmtError(node, "Cannot continue outside of a loop")
    }
    return ContinueNode(node)
  }

  override fun visit(node: ThrowCstNode): StatementNode {
    val expression = cast(Throwable::class.javaType, node.expression.accept(this, Throwable::class.javaType))
    return ThrowNode(node, expression)
  }

  override fun visit(node: TryCatchCstNode): StatementNode = useInnerScope { resourcesScope ->
    if (node.finallyNode == null && node.catchNodes.isEmpty() && node.resources.isEmpty()) {
      return@useInnerScope stmtError(node, "Try statement must have a finally, catch and/or resources")
    }

    // handle resources first, as they need to be declared
    val resourceVarDecls = node.resources.map {
      val resourceType = resolve(it.type)
      if (!resourceType.implements(Closeable::class.javaType)) {
        return@useInnerScope stmtError(node, "Try resources need to implement Closeable")
      }
      val resourceVar = resourcesScope.addLocalVariable(resourceType, it.value, Nullness.NOT_NULL, token = it.token)

      if (it.expressionNode == null) return@useInnerScope stmtError(it, "Resource declarations need to be initialised")
      VariableAssignmentNode(
        resourceVar,
        cast(resourceType.type, it.expressionNode!!.accept(this, resourceType.type)),
        it.tokenStart, it.tokenEnd
      )
    }
    val resourceVarNames = resourceVarDecls.asSequence()
      .map { it.variable.name }
      .toSet()

    // will be needed for later, to run the finally statements and then return the value stored in this variable
    val hasReturnStatements = node.any { it is ReturnCstNode }
    if (hasReturnStatements && node.finallyNode != null && node.any { it is ReturnCstNode && it.expressionNode == null }) {
      // yup. throw error in this case because I don't know how to properly handle 'finally' block otherwise.
      return@useInnerScope stmtError(node, "Cannot have void return statement in a try with a finally block")
    }
    val returnValueVar = if (hasReturnStatements) resourcesScope.addLocalVariable(resourcesScope.method.returnType, resourcesScope.method.nullness)
    else null

    // handle try block
    val tryBlock = useInnerScope { node.tryNode.accept(this) }

    // handle catch blocks
    val catchNodes = node.catchNodes.map { triple ->
      val throwableTypes = triple.first.map(this::resolve)
      if (throwableTypes.any { !Throwable::class.javaType.isAssignableFrom(it) }) {
        return@useInnerScope stmtError(node, "Can only catch throwable types")
      } else if (throwableTypes.isEmpty()) {
        return@useInnerScope stmtError(node, "Need to catch at least one exception")
      }

      val (throwableVar, catchStatement) = useScope(CatchBlockScope(resourcesScope, resourceVarNames)) { catchScope ->
        val v = catchScope.addLocalVariable(JavaType.commonType(throwableTypes), triple.second, Nullness.NOT_NULL)
        Pair(v, triple.third.accept(this))
      }
      TryNode.CatchNode(throwableTypes, throwableVar, catchStatement)
    }

    // handle finally block
    val finallyNode = if (node.finallyNode == null && node.resources.isEmpty()) null
    else useScope(CatchBlockScope(resourcesScope, resourceVarNames)) { finallyScope ->
      finallyScope.addLocalVariable(Throwable::class.javaType, Nullness.NOT_NULL)
      val throwableVar = finallyScope.addLocalVariable(Throwable::class.javaType, Nullness.NOT_NULL)
      val finallyBlock = BlockStatementNode(
        mutableListOf(), node.finallyNode?.tokenStart ?: node.tokenStart,
        node.finallyNode?.tokenEnd ?: node.tokenEnd
      )
      // dispose the resources first, if any
      resourceVarDecls.forEach { resourceVarDecl ->
        val resourceRef = ReferenceNode(variable = resourceVarDecl.variable, token = node.token)
        finallyBlock.statements.add(
          IfStatementNode(
            IsNotEqualNode(resourceRef, NullValueNode(node.token)),
            ExpressionStatementNode(
              fCall(
                owner = resourceRef,
                arguments = emptyList(),
                method = symbolResolver.findMethodOrThrow(resourceVarDecl.variable.type, "close", emptyList()),
                node = node
              )
            ), null, node
          )
        )
      }

      // then do the finally-block
      node.finallyNode?.let { finallyBlock.statements.add(it.accept(this)) }
      TryNode.FinallyNode(throwableVar, finallyBlock, returnValueVar)
    }

    val tryCatchNode = TryNode(node, tryBlock, catchNodes, finallyNode)
    if (tryBlock.isEmpty) {
      return@useInnerScope stmtError(tryCatchNode, "Try block must have at least one statement")
    }

    if (hasReturnStatements && node.finallyNode != null && !AllPathsReturnVisitor.test(tryCatchNode)) {
      // yup. throw error in this case because I don't know how to properly handle 'finally' block otherwise.
      return@useInnerScope stmtError(
        tryBlock,
        "All paths of the try/catch block need to return if there is at least one return in this statement block"
      )
    }

    if (resourceVarDecls.isEmpty()) tryCatchNode
    else {
      // initialize the resources first
      val statements = mutableListOf<StatementNode>()
      resourceVarDecls.forEach { resourceVarDecl ->
        statements.add(ExpressionStatementNode(resourceVarDecl))
      }
      // then process the actual try/catch block
      statements.add(tryCatchNode)
      BlockStatementNode(statements, node.tokenStart, node.tokenEnd)
    }
  }

  override fun visit(node: BlockCstNode) = useInnerScope {
    val statements = blockStatements(node.statements)
    BlockStatementNode(statements, node.tokenStart, node.tokenEnd)
  }

  protected fun blockStatements(cstStatements: List<StatementCstNode>): MutableList<StatementNode> {
    val statements = mutableListOf<StatementNode>()
    for (i in cstStatements.indices) {
      val statement = cstStatements[i].accept(this)
      if (statement is ReturnStatementNode && i < cstStatements.lastIndex) {
        // can still continue semantic analysis by ignoring this stmt
        stmtError(statement, "Cannot have statements after a return statement")
      } else {
        statements.add(statement)
      }
    }
    return statements
  }

  protected fun fCall(
    methodResolve: Pair<MarcelMethod, List<ExpressionNode>>, owner: ExpressionNode?, castType: JavaType? = null,
    tokenStart: LexToken, tokenEnd: LexToken
  ) = fCall(
    tokenStart = tokenStart,
    tokenEnd = tokenEnd,
    method = methodResolve.first,
    arguments = methodResolve.second,
    owner = owner,
    castType = castType
  )

  private fun checkVariableAccess(
    variable: Variable,
    node: CstNode,
    checkGet: Boolean = false,
    checkSet: Boolean = false
  ) {
    if (!variable.isVisibleFrom(currentScope.classType) // not visible globally, from either get or set
      // these 2 below check are in fact just for MethodField, which can have different visibilities between the getter and the setter
      || checkGet && !variable.isVisibleFrom(currentScope.classType, Variable.Access.GET)
      || checkSet && !variable.isVisibleFrom(currentScope.classType, Variable.Access.SET)
    ) {
      throw VariableAccessException(node.token, "Variable ${variable.name} is not visible from ${currentScope.classType}")
    }
    if (checkGet && !variable.isGettable) {
      throw VariableAccessException(node.token, "Cannot get value of variable ${variable.name}")
    }
    if (checkSet && !variable.isSettable) {
      val scope = currentScope
      print(scope)
      val methodScope = currentScope as? MethodScope
      val message = when {
        // prevented because it would be useless, as async/switch/when generate and are executed on a new method
        methodScope?.method?.name?.contains(WHEN_METHOD_PREFIX) == true -> "Cannot set value of local variables from outer scope in a switch/when. Use if/else instead"
        methodScope?.method?.name?.contains(ASYNC_METHOD_PREFIX) == true -> "Cannot set value of local variables from outer scope in an async block"
        methodScope?.isInLambda == true -> "Cannot set value of local variables from outer scope in a lambda"
        else -> "Cannot set value for variable ${variable.name}"
      }
      throw VariableAccessException(node.token, message)
    }
  }

  override fun resolve(node: TypeCstNode): JavaType = currentScope.resolveTypeOrThrow(node)

  override fun toMethodParameter(
    ownerType: JavaType, forExtensionType: JavaType?, visibility: Visibility,
    nullness: Nullness, isStatic: Boolean,
    parameterIndex: Int, methodName: String, node: MethodParameterCstNode
  ): MethodParameter {
    val parameterType =
      if (node.thisParameter) symbolResolver.getClassField(ownerType, node.name, node.token).type
      else resolve(node.type)
    val defaultValue = if (node.defaultValue != null) {
      val defaultValueMethod =
        generateDefaultParameterMethod(node, ownerType, visibility, isStatic, methodName, parameterType, Nullness.NULLABLE, parameterIndex)
      useScope(newMethodScope(ownerType, forExtensionType, defaultValueMethod)) {
        val defaultValue = node.defaultValue!!.accept(this)
        defaultValueMethod.nullness = defaultValue.nullness
        cast(
          parameterType,
          defaultValue
        )
      }
    } else null
    return MethodParameter(
      parameterType,
      nullness,
      node.name,
      node.annotations.map { visit(it, ElementType.PARAMETER) },
      defaultValue,
      isFinal = node.thisParameter
    )
  }

  private fun generateDefaultParameterMethod(
    node: CstNode,
    ownerClass: JavaType,
    visibility: Visibility,
    isStatic: Boolean,
    methodName: String,
    type: JavaType,
    nullness: Nullness,
    parameterIndex: Int
  ): MethodNode {
    return MethodNode(
      "${methodName}_defaultParam${parameterIndex}", nullness, mutableListOf(), visibility, type,
      // always static because we don't it is painful to push the owner
      isStatic = true, node.tokenStart, node.tokenEnd, ownerClass
    )
  }

  // add annotation if necessary to the node, e.g. for default parameters. To use on to be compiled methods
  protected fun toMethodParameterNode(
    classNode: ClassNode, visibility: Visibility,
    isStatic: Boolean, parameterIndex: Int,
    methodName: String, node: MethodParameterCstNode
  ): MethodParameter {
    val ownerType = classNode.type
    val annotations = node.annotations.map { visit(it, ElementType.PARAMETER) }.toMutableList()
    val parameterType =
      if (node.thisParameter) symbolResolver.getClassField(ownerType, node.name, node.token).type
      else resolve(node.type)
    val parameterName = node.name
    // may be needed
    val defaultValueMethod =
      generateDefaultParameterMethod(node, ownerType, visibility, isStatic, methodName, parameterType, Nullness.NULLABLE, parameterIndex)
    val defaultValue =
      if (node.defaultValue != null) {
        useScope(newMethodScope(ownerType, classNode.forExtensionType, defaultValueMethod)) {
          val defaultValue = node.defaultValue!!.accept(this)
          defaultValueMethod.nullness = defaultValue.nullness
          cast(
            parameterType,
            defaultValue
          )
        }
      } else null
    if (defaultValue != null) {
      when {
        defaultValue is NullValueNode
            // because of casting
            || defaultValue is JavaCastNode && defaultValue.expressionNode is NullValueNode -> {
          if (parameterType.primitive) {
            throw MarcelSemanticException(node.token, "Primitive types cannot have null default value")
          }
          annotations.add(
            AnnotationNode(
              NullDefaultValue::class.javaAnnotationType,
              emptyList(),
              node.tokenStart,
              node.tokenEnd
            )
          )
        }

        (parameterType == JavaType.int || parameterType == JavaType.Integer)
            && defaultValue is IntConstantNode -> annotations.add(
          AnnotationNode(
            IntDefaultValue::class.javaAnnotationType,
            listOf(JavaAnnotation.Attribute("value", JavaType.int, defaultValue.value)),
            node.tokenStart,
            node.tokenEnd
          )
        )

        (parameterType == JavaType.long || parameterType == JavaType.Long)
            && defaultValue is LongConstantNode -> annotations.add(
          AnnotationNode(
            LongDefaultValue::class.javaAnnotationType,
            listOf(JavaAnnotation.Attribute("value", JavaType.long, defaultValue.value)),
            node.tokenStart,
            node.tokenEnd
          )
        )

        (parameterType == JavaType.float || parameterType == JavaType.Float)
            && defaultValue is FloatConstantNode -> annotations.add(
          AnnotationNode(
            FloatDefaultValue::class.javaAnnotationType,
            listOf(JavaAnnotation.Attribute("value", JavaType.float, defaultValue.value)),
            node.tokenStart,
            node.tokenEnd
          )
        )

        (parameterType == JavaType.double || parameterType == JavaType.Double)
            && defaultValue is DoubleConstantNode -> annotations.add(
          AnnotationNode(
            DoubleDefaultValue::class.javaAnnotationType,
            listOf(JavaAnnotation.Attribute("value", JavaType.double, defaultValue.value)),
            node.tokenStart,
            node.tokenEnd
          )
        )

        (parameterType == JavaType.char || parameterType == JavaType.Character)
            && defaultValue is CharConstantNode -> annotations.add(
          AnnotationNode(
            CharDefaultValue::class.javaAnnotationType,
            listOf(JavaAnnotation.Attribute("value", JavaType.char, defaultValue.value)),
            node.tokenStart,
            node.tokenEnd
          )
        )

        (parameterType == JavaType.boolean || parameterType == JavaType.Boolean)
            && defaultValue is BoolConstantNode -> annotations.add(
          AnnotationNode(
            BooleanDefaultValue::class.javaAnnotationType,
            listOf(JavaAnnotation.Attribute("value", JavaType.boolean, defaultValue.value)),
            node.tokenStart,
            node.tokenEnd
          )
        )

        parameterType == JavaType.String
            && defaultValue is StringConstantNode -> annotations.add(
          AnnotationNode(
            StringDefaultValue::class.javaAnnotationType,
            listOf(JavaAnnotation.Attribute("value", JavaType.String, defaultValue.value)),
            node.tokenStart,
            node.tokenEnd
          )
        )

        else -> {
          // defining method
          defaultValueMethod.blockStatement.add(ReturnStatementNode(cast(parameterType, defaultValue)))
          classNode.methods.add(defaultValueMethod)
          symbolResolver.defineMethod(ownerType, defaultValueMethod)

          // now adding annotation
          annotations.add(
            AnnotationNode(
              MethodCallDefaultValue::class.javaAnnotationType,
              listOf(JavaAnnotation.Attribute("methodName", JavaType.String, defaultValueMethod.name)),
              node.tokenStart, node.tokenEnd
            )
          )
        }
      }
    }
    return MethodParameter(parameterType, Nullness.of(node.isNullable), parameterName, annotations, defaultValue)
  }


  private fun newMethodScope(method: MarcelMethod) =
    MethodScope(ClassScope(symbolResolver, currentScope.classType, null, imports), method)

  private fun newMethodScope(classType: JavaType, forExtensionType: JavaType?, method: MarcelMethod) =
    MethodScope(ClassScope(symbolResolver, classType, forExtensionType, imports), method)

  fun visit(cstAnnotation: AnnotationCstNode, elementType: ElementType): AnnotationNode {
    val annotationType = resolve(cstAnnotation.typeNode)
    if (!annotationType.isAnnotation) throw MarcelSemanticException(
      cstAnnotation.token,
      "$annotationType is not an annotation"
    )
    val javaAnnotationType = JavaAnnotationType.of(annotationType)
    if (!javaAnnotationType.targets.contains(elementType)) {
      error(
        cstAnnotation,
        "Annotation $javaAnnotationType is not expected on elements of type $elementType"
      )
    }

    val annotation = AnnotationNode(
      type = annotationType.asAnnotationType,
      tokenStart = cstAnnotation.tokenStart,
      attributes = cstAnnotation.attributes.map { annotationAttribute(cstAnnotation, javaAnnotationType, it) },
      tokenEnd = cstAnnotation.tokenEnd,
      identifierToken = cstAnnotation.identifierToken
    )

    // check attributes without default values that weren't specified
    for (attr in javaAnnotationType.attributes) {
      if (attr.defaultValue == null && annotation.attributes.none { it.name == attr.name }) {
        error(
          cstAnnotation,
          "Attribute ${attr.name} has no default value and was not specified for annotation $javaAnnotationType"
        )
      }
    }

    return annotation
  }

  private fun annotationAttribute(
    node: AnnotationCstNode,
    javaAnnotationType: JavaAnnotationType,
    specifiedAttr: Pair<String, ExpressionCstNode>
  ): JavaAnnotation.Attribute {
    val attribute = javaAnnotationType.attributes.find { it.name == specifiedAttr.first }
      ?: throw MarcelSemanticException(
        node.token,
        "Unknown member ${specifiedAttr.first} for annotation $javaAnnotationType"
      )

    return if (attribute.type.isEnum) {
      val specifiedName = (specifiedAttr.second as? ReferenceCstNode)?.value
        ?: throw MarcelSemanticException(node, "Need the enum name for an enum attribute")
      val enumValues = attribute.type.realClazz.enumConstants
      if (enumValues?.any { (it as Enum<*>).name == specifiedName } != true) {
        throw MarcelSemanticException(node, "Unknown enum $specifiedName")
      }
      JavaAnnotation.Attribute(attribute.name, attribute.type, specifiedName)
    } else {
      val specifiedValueNode = specifiedAttr.second.accept(this, attribute.type)
      if (specifiedValueNode !is JavaConstantExpression) {
        throw MarcelSemanticException(
          node,
          "Specified a non constant value for attribute ${attribute.name}"
        )
      }
      val attrValue = (specifiedValueNode.value ?: attribute.defaultValue)
        ?: throw MarcelSemanticException(node, "Attribute value cannot be null${attribute.name}")

      // check type
      when (attribute.type) {
        JavaType.String -> if (attrValue !is String) annotationErrorAttributeTypeError(
          node,
          javaAnnotationType,
          attribute,
          attrValue
        )

        JavaType.int -> if (attrValue !is Int) annotationErrorAttributeTypeError(
          node,
          javaAnnotationType,
          attribute,
          attrValue
        )

        JavaType.long -> if (attrValue !is Long) annotationErrorAttributeTypeError(
          node,
          javaAnnotationType,
          attribute,
          attrValue
        )

        JavaType.float -> if (attrValue !is Float) annotationErrorAttributeTypeError(
          node,
          javaAnnotationType,
          attribute,
          attrValue
        )

        JavaType.double -> if (attrValue !is Double) annotationErrorAttributeTypeError(
          node,
          javaAnnotationType,
          attribute,
          attrValue
        )

        JavaType.char -> if (attrValue !is Char) annotationErrorAttributeTypeError(
          node,
          javaAnnotationType,
          attribute,
          attrValue
        )

        JavaType.boolean -> if (attrValue !is Boolean) annotationErrorAttributeTypeError(
          node,
          javaAnnotationType,
          attribute,
          attrValue
        )

        JavaType.byte -> if (attrValue !is Byte) annotationErrorAttributeTypeError(
          node,
          javaAnnotationType,
          attribute,
          attrValue
        )

        JavaType.short -> if (attrValue !is Short) annotationErrorAttributeTypeError(
          node,
          javaAnnotationType,
          attribute,
          attrValue
        )

        JavaType.Clazz -> if (attrValue !is JavaType) annotationErrorAttributeTypeError(
          node,
          javaAnnotationType,
          attribute,
          attrValue
        )

        else -> annotationErrorAttributeTypeError(node, javaAnnotationType, attribute, attrValue)
      }

      JavaAnnotation.Attribute(attribute.name, attribute.type, attrValue)
    }
  }

  private fun annotationErrorAttributeTypeError(
    node: AnnotationCstNode,
    annotation: JavaAnnotationType,
    attribute: JavaAnnotationType.Attribute,
    attrValue: Any
  ): Nothing = throw MarcelSemanticException(
    node,
    "Incompatible type for annotation member ${attribute.name} of annotation ${annotation}. Wanted ${attribute.type} but got ${attrValue.javaClass}"
  )

  override fun cast(expectedType: JavaType, node: ExpressionNode): ExpressionNode {
    return try {
       caster.cast(expectedType, node)
    } catch (e: TypeCastException) {
      exprError(node, e.message, expectedType)
    }
  }

  override fun castNumberConstantOrNull(value: Int, type: JavaPrimitiveType) = caster.castNumberConstantOrNull(value, type)

  override fun javaCast(expectedType: JavaType, node: ExpressionNode): ExpressionNode {
    return try {
      caster.javaCast(expectedType, node)
    } catch (e: TypeCastException) {
      exprError(node, e.message, expectedType)
    }
  }

  override fun truthyCast(node: ExpressionNode): ExpressionNode {
    return try {
      caster.truthyCast(node)
    } catch (e: TypeCastException) {
      exprError(node, e.message, JavaType.boolean)
    }

  }
  protected fun stmtError(node: AstNode, message: String) = ExpressionStatementNode(exprError(node.token, message, JavaType.void))
  protected fun stmtError(node: CstNode, message: String) = ExpressionStatementNode(exprError(node.token, message, JavaType.void))

  protected fun exprError(node: AstNode, message: String, smartCastType: JavaType? = null) = exprError(node.token, message, smartCastType)
  protected fun exprError(node: CstNode, message: String, smartCastType: JavaType? = null) = exprError(node.token, message, smartCastType)

  protected fun exprError(token: LexToken, message: String, smartCastType: JavaType? = null): ExpressionNode {
    errors.add(MarcelSemanticException.Error(message, token))
    return ExprErrorNode(token, smartCastType)
  }

  protected fun error(node: AstNode, message: String) = error(node.token, message)
  protected fun error(node: CstNode, message: String) = error(node.token, message)

  protected fun error(token: LexToken, message: String) {
    errors.add(MarcelSemanticException.Error(message, token))
  }

  fun throwIfHasErrors(moduleNode: ModuleNode? = null) {
    if (errors.isNotEmpty()) {
      // need a copy because we will clear this list
      throw MarcelSemanticException(errors.toList(), moduleNode)
    }
  }
}