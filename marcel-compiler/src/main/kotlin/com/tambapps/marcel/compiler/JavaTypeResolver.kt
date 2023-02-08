package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.util.getElementsType
import com.tambapps.marcel.compiler.util.getKeysType
import com.tambapps.marcel.compiler.util.getMethod
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.compiler.util.getValuesType
import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstTypedObject
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
import com.tambapps.marcel.parser.scope.ClassField
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.scope.MethodField
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.NotLoadedJavaType
import com.tambapps.marcel.parser.type.ReflectJavaConstructor
import com.tambapps.marcel.parser.type.ReflectJavaMethod
import marcel.lang.IntRange

class JavaTypeResolver: AstNodeTypeResolver() {

  private val classMethods = mutableMapOf<String, MutableList<JavaMethod>>()

  override fun defineMethod(javaType: JavaType, method: JavaMethod) {
    val methods = getTypeMethods(javaType)
    if (methods.any { it.matches(this, method.name, method.parameters) }) {
      throw SemanticException("Method with $method is already defined")
    }
    methods.add(method)
  }

  override fun getDeclaredMethods(javaType: JavaType): List<JavaMethod> {
    return if (javaType.isLoaded) javaType.realClazz.declaredMethods.map { ReflectJavaMethod(it, javaType) }
    else classMethods[javaType.className] ?: emptyList()
  }

  override fun findMethod(javaType: JavaType, name: String, argumentTypes: List<AstTypedObject>, excludeInterfaces: Boolean): JavaMethod? {
    val methods = getTypeMethods(javaType)
    var m = methods.find { it.matches(this, name, argumentTypes) }
    if (m != null) return m

    if (javaType.isLoaded) {
      val clazz = javaType.type.realClazz
      val candidates = if (name == JavaMethod.CONSTRUCTOR_NAME) {
        clazz.declaredConstructors
          .map { ReflectJavaConstructor(it) }
          .filter { it.matches(this, argumentTypes) }
      } else {
        clazz.declaredMethods
          .filter { it.name == name }
          .map { ReflectJavaMethod(it, javaType) }
          .filter { it.matches(this, argumentTypes) }
      }
      m = getMoreSpecificMethod(candidates)
      if (m != null) return m
    }

    // search in super types
    var type = javaType.superType
    while (type != null) {
      m = findMethod(type, name, argumentTypes, true)
      if (m != null) return m
      type = type.superType
    }

    if (excludeInterfaces) return null
    // now search on all implemented interfaces
    for (interfaze in javaType.allImplementedInterfaces) {
      m = findMethod(interfaze, name, argumentTypes)
      if (m != null) return m
    }
    return null
  }

  private fun getMoreSpecificMethod(candidates: List<JavaMethod>): JavaMethod? {
    // inspired from Class.searchMethods()
    var m: JavaMethod? = null
    for (candidate in candidates) {
      if (m == null
        || (m.returnType != candidate.returnType
            && m.returnType.isAssignableFrom(candidate.returnType))) m = candidate
    }
    return m
  }

  private fun getTypeMethods(javaType: JavaType): MutableList<JavaMethod> {
    return classMethods.computeIfAbsent(javaType.className) { mutableListOf() }
  }

  override fun findField(javaType: JavaType, name: String, declared: Boolean): MarcelField? {
    if (javaType.isLoaded) {
      val clazz = javaType.realClazz
      val field = try {
        clazz.getDeclaredField(name)
      } catch (e: NoSuchFieldException) {
        null
      }
      if (field != null) {
        return ClassField(JavaType.of(field.type), field.name, javaType, field.modifiers)
      }
      // try to find getter
      val methodFieldName = name.replaceFirstChar { it.uppercase() }
      val getterMethod  = findMethod(javaType, "get$methodFieldName", emptyList())
      val setterMethod = findMethod(javaType, "set$methodFieldName", listOf(javaType))
      if (getterMethod != null || setterMethod != null) {
        return MethodField.from(javaType, name, getterMethod, setterMethod)
      }
      return null
    } else {
      // TODO doesn't search on defined fields of notloaded type. Only search on super classes that are Loaded
      // searching on super types
      var type: JavaType? = javaType.superType!!
      while (type != null) {
        val f = findField(type, name, declared)
        if (f != null) return f
        type = type.superType
      }
      return null
    }
  }

  // ast node type resolver methods
  override fun visit(getFieldAccessOperator: GetFieldAccessOperator) =
    findFieldOrThrow(getFieldAccessOperator.leftOperand.accept(this), getFieldAccessOperator.rightOperand.name).type

  override fun visit(literalListNode: LiteralArrayNode) = literalListNode.type ?: JavaType.arrayType(literalListNode.getElementsType(this))

  override fun visit(literalMapNode: LiteralMapNode) =
    JavaType.mapType(literalMapNode.getKeysType(this), literalMapNode.getValuesType(this))

  override fun visit(fCall: FunctionCallNode) = fCall.getMethod(this).returnType

}