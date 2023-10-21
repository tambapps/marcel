package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.arrayStoreCode
import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.takes2Slots
import com.tambapps.marcel.compiler.extensions.typeCode
import com.tambapps.marcel.compiler.extensions.visitMethodInsn
import com.tambapps.marcel.semantic.ast.expression.DupNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.PopNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.TernaryNode
import com.tambapps.marcel.semantic.ast.expression.ThisConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.lang.StringBuilder

sealed class MethodExpressionWriter(
  protected val mv: MethodVisitor,
  protected val typeResolver: JavaTypeResolver,
  classScopeType: JavaType
): ExpressionNodeVisitor<Unit> {

  protected val loadVariableVisitor = LoadVariableVisitor(mv, classScopeType)
  private val storeVariableVisitor = StoreVariableVisitor(mv, classScopeType)

  internal abstract fun pushExpression(node: ExpressionNode)

  override fun visit(node: VariableAssignmentNode) {
    node.owner?.let { pushExpression(it) }
    pushExpression(node.expression)
    node.variable.accept(storeVariableVisitor)
  }

  override fun visit(node: FunctionCallNode) {
    node.owner?.let { pushExpression(it) }
    node.arguments.forEach { pushExpression(it) }
    mv.visitMethodInsn(node.javaMethod)
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


  override fun visit(node: ReferenceNode) {
    node.owner?.let { pushExpression(it) }
    node.variable.accept(loadVariableVisitor)
  }

  // this node should ALWAYS be pushed so don't override it
  final override fun visit(node: DupNode) {
    pushExpression(node.expression)
    dup(node.type)
  }

  override fun visit(node: PopNode) {
    popStackIfNotVoid(node.popType)
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

  override fun visit(node: MapNode) {
    val mapType = HashMap::class.javaType
    val method = typeResolver.findMethodOrThrow(mapType, JavaMethod.CONSTRUCTOR_NAME, emptyList())
    visit(NewInstanceNode(mapType, method, emptyList(), node.token))

    for (entry in node.entries) {
      mv.visitInsn(Opcodes.DUP)
      entry.first.accept(this)
      entry.second.accept(this)
      invokeMethodAsStatement(typeResolver.findMethodOrThrow(mapType, "put", listOf(JavaType.Object, JavaType.Object)))
    }
  }

  override fun visit(node: StringNode) {
    val stringBuilderType = StringBuilder::class.javaType
    // using StringBuilder to build the whole string
    val constructorMethod = typeResolver.findMethod(StringBuilder::class.javaType, JavaMethod.CONSTRUCTOR_NAME, emptyList())!!
    // new StringBuilder()
    visit(NewInstanceNode(stringBuilderType, constructorMethod, emptyList(), node.token))
    for (part in node.parts) {
      // chained .append(...) calls
      part.accept(this)
      val method = typeResolver.findMethod(stringBuilderType, "append", listOf(
        if (part.type.primitive) part else JavaType.Object
      ))!!
      mv.visitMethodInsn(method)
    }
    mv.visitMethodInsn(typeResolver.findMethod(stringBuilderType, "toString", emptyList())!!)
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
      Opcodes.INVOKESPECIAL, classInternalName, JavaMethod.CONSTRUCTOR_NAME, node.javaMethod.descriptor, false)
  }

  override fun visit(node: ThisConstructorCallNode) {
    visitOwnConstructorCall(node.classType, node.javaMethod, node.arguments)
  }

  override fun visit(node: SuperConstructorCallNode) {
    visitOwnConstructorCall(node.classType, node.javaMethod, node.arguments)
  }

  private fun visitOwnConstructorCall(type: JavaType, method: JavaMethod, arguments: List<ExpressionNode>) {
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    arguments.forEach { pushExpression(it) }
    mv.visitMethodInsn(
      Opcodes.INVOKESPECIAL, type.internalName, JavaMethod.CONSTRUCTOR_NAME,
      // void return type for constructors
      method.descriptor, false)
  }

  // visit and pop the value if necessary
  private fun invokeMethodAsStatement(method: JavaMethod) {
    mv.visitMethodInsn(method)
    popStackIfNotVoid(method.returnType)
  }

  // visit and pop the value if necessary
  protected fun visitAsStatement(expressionNode: ExpressionNode) {
    expressionNode.accept(this)
    popStackIfNotVoid(expressionNode.type)
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