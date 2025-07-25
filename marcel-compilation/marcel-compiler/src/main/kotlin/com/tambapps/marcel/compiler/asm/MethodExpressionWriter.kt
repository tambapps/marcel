package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.compiler.extensions.arrayStoreCode
import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.typeCode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.expression.ConditionalExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExprErrorNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.TernaryNode
import com.tambapps.marcel.semantic.ast.expression.ThisConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.YieldExpression
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.NewArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.expression.operator.ArrayIndexAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.method.ReflectJavaConstructor
import com.tambapps.marcel.semantic.symbol.method.ReflectJavaMethod
import com.tambapps.marcel.semantic.symbol.type.JavaType
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.lang.StringBuilder

sealed class MethodExpressionWriter(
  protected val mv: MethodVisitor,
  protected val classScopeType: JavaType
): ExpressionNodeVisitor<Unit> {

  private companion object {
    val HASH_MAP_CONSTRUCTOR = ReflectJavaConstructor(HashMap::class.java.getConstructor())
    val MAP_PUT_METHOD = ReflectJavaMethod(HashMap::class.java.getMethod("put", Object::class.java, Object::class.java))
    val STRING_BUILDER_CONSTRUCTOR = ReflectJavaConstructor(StringBuilder::class.java.getConstructor())
    val STRING_BUILDER_TO_STRING_METHOD = ReflectJavaMethod(StringBuilder::class.java.getMethod("toString"))
  }
  protected val methodCallWriter = MethodCallWriter(mv)
  protected val loadVariableVisitor = LoadVariableVisitor(mv, classScopeType, methodCallWriter)
  protected val storeVariableVisitor = StoreVariableVisitor(mv, classScopeType, methodCallWriter)

  internal abstract fun pushExpression(node: com.tambapps.marcel.semantic.ast.expression.ExpressionNode)

  // should be an Int, Byte, Long, Float, Double, String
  internal abstract fun pushConstant(value: Any)

  internal abstract fun visitStatement(statementNode: StatementNode)

  /**
   * Adds line number to bytecode so that it can be displayed when an exception occured.
   * Only useful for statements
   */
  protected fun label(node: AstNode) = Label().apply {
    mv.visitLabel(this)
    mv.visitLineNumber(node.token.line + 1, this)
  }

  override fun visit(node: YieldExpression) {
    node.statement?.let(this::visitStatement)
    node.expression.accept(this)
  }

  override fun visit(node: VariableAssignmentNode) {
    node.owner?.let { pushExpression(it) }
    pushExpression(node.expression)
    node.variable.accept(storeVariableVisitor)
  }

  override fun visit(node: ArrayIndexAssignmentNode) {
    pushExpression(node.owner)
    pushExpression(node.indexExpr)
    pushExpression(node.expression)
    mv.visitInsn(node.arrayType.arrayStoreCode)
  }

  override fun visit(node: com.tambapps.marcel.semantic.ast.expression.FunctionCallNode) {
    node.owner?.let { pushExpression(it) }
    node.arguments.forEach { pushExpression(it) }
    if (node.owner !is SuperReferenceNode) node.javaMethod.accept(methodCallWriter)
    else mv.visitMethodInsn(Opcodes.INVOKESPECIAL, node.javaMethod.ownerClass.internalName, node.javaMethod.name, node.javaMethod.descriptor, node.javaMethod.ownerClass.isInterface)
  }

  override fun visit(node: TernaryNode) {
    pushExpression(node.testExpressionNode)
    val endLabel = Label()
    val falseLabel = Label()
    mv.visitJumpInsn(Opcodes.IFEQ, falseLabel)
    node.trueExpressionNode.accept(this)
    mv.visitJumpInsn(Opcodes.GOTO, endLabel)
    mv.visitLabel(falseLabel)
    node.falseExpressionNode.accept(this)
    mv.visitLabel(endLabel)
  }

  // inspired from MethodInstructionWriter.visit(IfStatementNode)
  override fun visit(node: ConditionalExpressionNode) {
    label(node.condition)
    pushExpression(node.condition)
    val endLabel = Label()
    val falseExpression = node.falseExpression
    if (falseExpression == null) {
      mv.visitJumpInsn(Opcodes.IFEQ, endLabel)
      node.trueExpression.accept(this)
      mv.visitLabel(endLabel)
    } else {
      val falseLabel = Label()
      mv.visitJumpInsn(Opcodes.IFEQ, falseLabel)
      node.trueExpression.accept(this)
      mv.visitJumpInsn(Opcodes.GOTO, endLabel)
      mv.visitLabel(falseLabel)
      falseExpression.accept(this)
      mv.visitLabel(endLabel)
    }
  }


  override fun visit(node: ReferenceNode) {
    node.owner?.let { pushExpression(it) }
    node.variable.accept(loadVariableVisitor)
  }

  // this node should ALWAYS be pushed so don't override it
  final override fun visit(node: com.tambapps.marcel.semantic.ast.expression.DupNode) {
    pushExpression(node.expression)
    dup(node.type)
  }

  override fun visit(node: ArrayNode) {
    val type = node.type
    val elements = node.elements
    // Push the size of the array to the stack
    mv.visitLdcInsn(node.elements.size)
    // Create an int array of size n
    if (type.elementsType.primitive) {
      mv.visitIntInsn(Opcodes.NEWARRAY, type.typeCode)
    } else {
      mv.visitTypeInsn(Opcodes.ANEWARRAY, type.elementsType.internalName)
    }
    for (i in elements.indices) {
      // Push the array reference on the stack
      mv.visitInsn(Opcodes.DUP)
      // push the index
      mv.visitLdcInsn(i)
      // push the value
      elements[i].accept(this)
      // store value at index
      mv.visitInsn(type.arrayStoreCode)
    }
  }

  override fun visit(node: NewArrayNode) {
    val type = node.type
    pushExpression(node.sizeExpr)
    if (type.elementsType.primitive) {
      mv.visitIntInsn(Opcodes.NEWARRAY, type.typeCode)
    } else {
      mv.visitTypeInsn(Opcodes.ANEWARRAY, type.elementsType.internalName)
    }
  }

  override fun visit(node: MapNode) {
    val mapType = HASH_MAP_CONSTRUCTOR.ownerClass
    visit(NewInstanceNode(mapType, HASH_MAP_CONSTRUCTOR, emptyList(), node.token))

    for (entry in node.entries) {
      mv.visitInsn(Opcodes.DUP)
      entry.first.accept(this)
      entry.second.accept(this)
      invokeMethodAsStatement(MAP_PUT_METHOD)
    }
  }

  override fun visit(node: StringNode) {
    val stringBuilderType = STRING_BUILDER_CONSTRUCTOR.ownerClass
    // using StringBuilder to build the whole string
    // new StringBuilder()
    visit(NewInstanceNode(stringBuilderType, STRING_BUILDER_CONSTRUCTOR, emptyList(), node.token))
    for (part in node.parts) {
      // chained .append(...) calls
      part.accept(this)
      val method = ReflectJavaMethod(stringBuilderType.realClazz.getMethod("append",
        if (part.type.primitive) part.type.realClazz else Object::class.java
      ))
      method.accept(methodCallWriter)
    }
    STRING_BUILDER_TO_STRING_METHOD.accept(methodCallWriter)
  }

  override fun visit(node: VoidExpressionNode) {
    // push nothing
  }

  override fun visit(node: NewInstanceNode) {
    val classInternalName = node.type.internalName
    mv.visitTypeInsn(Opcodes.NEW, classInternalName)
    mv.visitInsn(Opcodes.DUP)
    node.arguments.forEach { pushExpression(it) }
    mv.visitMethodInsn(
      Opcodes.INVOKESPECIAL, classInternalName, MarcelMethod.CONSTRUCTOR_NAME, node.javaMethod.descriptor, false)
  }

  override fun visit(node: ThisConstructorCallNode) {
    visitOwnConstructorCall(node.classType, node.javaMethod, node.arguments)
  }

  override fun visit(node: SuperConstructorCallNode) {
    visitOwnConstructorCall(node.classType, node.javaMethod, node.arguments)
  }

  override fun visit(node: ExprErrorNode) {
    throw MarcelCompilerException("Unexpected node of type ExprErrorNode. Your tree is not valid and cannot be compiled")
  }

  private fun visitOwnConstructorCall(type: JavaType, method: MarcelMethod, arguments: List<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>) {
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    arguments.forEach { pushExpression(it) }
    mv.visitMethodInsn(
      Opcodes.INVOKESPECIAL, type.internalName, MarcelMethod.CONSTRUCTOR_NAME,
      // void return type for constructors
      method.descriptor, false)
  }

  // visit and pop the value if necessary
  private fun invokeMethodAsStatement(method: MarcelMethod) {
    method.accept(methodCallWriter)
    popStackIfNotVoid(method.returnType)
  }

  protected fun popStackIfNotVoid(type: JavaType) {
    if (type != JavaType.void) {
      mv.visitInsn(if (type.takes2Slots) Opcodes.POP2 else Opcodes.POP)
    }
  }

  protected fun dup(type: JavaType) {
    mv.visitInsn(if (type.takes2Slots) Opcodes.DUP2 else Opcodes.DUP)
  }
}