package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.visitor.ExpressionVisitor
import com.tambapps.marcel.parser.visitor.StatementVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class StatementGenerator(private val mv: MethodVisitor): StatementVisitor {

  private val expressionGenerator = ExpressionGenerator(mv)


  override fun visit(dropNode: DropNode) {
    dropNode.child.accept(expressionGenerator)
    // TODO drop what's on the stack if child is expression
    // TODO find a way to optimize statements: avoid pushing on the stack if it is just to drop right after

  }
}