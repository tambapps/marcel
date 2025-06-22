package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.returnCode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.TryNode
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.util.*

class TryFinallyMethodInstructionWriter(mv: MethodVisitor, classScopeType: JavaType) :
  MethodInstructionWriter(mv, classScopeType) {
  private val contextQueue = LinkedList<TryContext>()
  private val currentContext: TryContext get() = contextQueue.peek()

  data class TryContext(
    /**
     * The label to use when encountering a return instruction
     */
    val hasFinally: Boolean,
    var currentFinallyLabel: Label,
    val returnVariable: LocalVariable?,
  )

  /**
   * Writes TryCatchNode. Java ASM does not have explicit support for finnally block, that is why we do some tricks
   * (that even the Java compiler does) to implement finally.
   * 'Finally' instructions are duplicated in the code, at the end of the try block, at the end of each catch block, and in a
   * special catch block that catches everything, run the 'finally' statement and rethrow the exception
   *
   * @param node the node
   */
  override fun visit(node: TryNode) {
    label(node)
    val tryStart = Label()
    val tryEnd = Label()
    val endLabel = Label()
    val catchNodes = node.catchNodes
    val finallyCatchWithLabel = node.finallyNode?.let { it to Label() }
    val context = TryContext(
      hasFinally = node.finallyNode != null,
      currentFinallyLabel = tryEnd,
      returnVariable = node.finallyNode?.returnVariable,
    )
    contextQueue.push(context)
    val catchLabelMap = generateCatchLabel(catchNodes, tryStart, tryEnd, finallyCatchWithLabel)

    tryBranch(node, tryStart, tryEnd, endLabel, node.finallyNode)

    catchNodes.forEach { catchNode ->
      val catchFinallyLabel = Label()
      context.currentFinallyLabel = catchFinallyLabel
      catchBlock(catchNode.throwableVariable, catchLabelMap.getValue(catchNode))
      catchNode.statement.accept(this)
      node.finallyNode?.let { finallyNode ->
        mv.visitLabel(catchFinallyLabel)
        finallyNode.statement.accept(this)
        finallyNode.returnVariable?.let { returnVariable ->
          returnVariable.accept(loadVariableVisitor)
          mv.visitInsn(returnVariable.type.returnCode)
        }
      }
      mv.visitJumpInsn(Opcodes.GOTO, endLabel)
    }

    contextQueue.pop() // no need for the context anymore

    // catch everything, run finally and rethrow
    finallyCatchWithLabel?.let {
      catchBlock(it.first.throwableVariable, it.second)
      it.first.statement.accept(this)
      it.first.throwableVariable.accept(loadVariableVisitor)
      mv.visitInsn(Opcodes.ATHROW)
    }
    mv.visitLabel(endLabel)
  }

  override fun visit(node: ReturnStatementNode) {
    if (contextQueue.isEmpty() || !currentContext.hasFinally) {
      super.visit(node)
      return
    }
    val context = currentContext
    if (node.expressionNode.type != JavaType.void && context.returnVariable != null) {
      visit(VariableAssignmentNode(
        localVariable = context.returnVariable,
        expression = node.expressionNode,
        tokenStart = node.tokenStart,
        tokenEnd = node.tokenEnd
      ))
    }
    mv.visitJumpInsn(Opcodes.GOTO, context.currentFinallyLabel)
  }

  private fun tryBranch(node: TryNode, tryStart: Label, tryEnd: Label, endLabel: Label, finallyNode : TryNode.FinallyNode?) {
    mv.visitLabel(tryStart)
    node.tryStatementNode.accept(this)
    mv.visitLabel(tryEnd)
    if (finallyNode != null) {
      finallyNode.statement.accept(this)
      finallyNode.returnVariable?.let { returnVariable ->
        returnVariable.accept(loadVariableVisitor)
        mv.visitInsn(returnVariable.type.returnCode)
      }
    }
    mv.visitJumpInsn(Opcodes.GOTO, endLabel)
  }

  private fun catchBlock(throwableVariable: LocalVariable, label: Label) {
    mv.visitLabel(label)
    mv.visitVarInsn(Opcodes.ASTORE, throwableVariable.index)
  }

  private fun generateCatchLabel(
    catchNodes: List<TryNode.CatchNode>,
    tryStart: Label,
    tryEnd: Label,
    finallyWithLabel: Pair<TryNode.FinallyNode, Label>? = null
  ): Map<TryNode.CatchNode, Label> {
    val map: Map<TryNode.CatchNode, Label> = catchNodes.associateBy(keySelector = { it }, valueTransform = { Label() })
    map.forEach { (node, label) ->
      node.throwableTypes.forEach { throwableType ->
        mv.visitTryCatchBlock(tryStart, tryEnd, label, throwableType.internalName)
      }
    }
    finallyWithLabel?.let {
      mv.visitTryCatchBlock(tryStart, tryEnd, it.second, null)
    }
    return map
  }
}