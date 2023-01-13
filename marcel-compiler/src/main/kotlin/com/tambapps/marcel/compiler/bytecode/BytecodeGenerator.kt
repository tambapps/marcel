package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.parser.ast.TokenNode
import com.tambapps.marcel.parser.ast.TokenNodeType
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class BytecodeGenerator {

  // TODO make java class version configurable https://www.baeldung.com/java-find-class-version

  fun generate(node: TokenNode): ByteArray {
    if (node.type != TokenNodeType.SCRIPT) {
      throw UnsupportedOperationException("sdfmldsfls")
    }
    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)

    // creating class
    classWriter.visit(52,  Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, node.value, null, "java/lang/Object", null)
    //https://github.com/JakubDziworski/Enkel-JVM-language/blob/master/compiler/src/main/java/com/kubadziworski/bytecodegeneration/MethodGenerator.java

    // creating main (psvm) function
    val mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
    val maxStack = 100; //TODO - do that properly
    val localVariablesCount = 0 // TODO

    // writing statements
    for (statement in node.children) {
      writeStatement(mv, statement)
    }
    mv.visitInsn(Opcodes.RETURN)
    mv.visitMaxs(maxStack, localVariablesCount) //set max stack and max local variables
    mv.visitEnd()

    classWriter.visitEnd()
    return classWriter.toByteArray()
  }

  private fun writeStatement(visitor: MethodVisitor, statement: TokenNode) {
    when(statement.type) {
      TokenNodeType.FUNCTION_CALL -> {
        if (statement.value == "println") {
          visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        } else {
          throw UnsupportedOperationException("Cannot handle function call yet")
        }
        for (argumentNode in statement.children) {
          when (argumentNode.type) {
            TokenNodeType.INTEGER -> {
              // TODO don't work for large integers. might need to push many times
              val value = argumentNode.value.toInt()
              visitor.visitIntInsn(Opcodes.BIPUSH, value)
            }
             else -> throw UnsupportedOperationException("Cannot handle arguments of type ${statement.type} yet")
          }
        }
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false)
      }
      else -> throw UnsupportedOperationException("Cannot handle ${statement.type} yet")
    }
  }
}