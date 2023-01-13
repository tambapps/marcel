package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.parser.ast.ModuleNode
import com.tambapps.marcel.parser.ast.TokenNode
import com.tambapps.marcel.parser.ast.TokenNodeType
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

// https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
class BytecodeGenerator {

  // TODO make java class version configurable https://www.baeldung.com/java-find-class-version

  fun generate(moduleNode: ModuleNode): ByteArray {
    // handling only one class for now
    val classNode = moduleNode.classes.first()
    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)

    // creating class
    classWriter.visit(52,  classNode.access, classNode.name, null, classNode.parentType.internalName, null)
    //https://github.com/JakubDziworski/Enkel-JVM-language/blob/master/compiler/src/main/java/com/kubadziworski/bytecodegeneration/MethodGenerator.java

    // handling only one class for now
    val methodNode = classNode.methods.first()

    // creating main (psvm) function
    val mv = classWriter.visitMethod(methodNode.access, methodNode.name, methodNode.methodDescriptor, null, null)
    val maxStack = 100; //TODO - do that properly
    val localVariablesCount = 0 // TODO

    // writing statements
    for (statement in methodNode.statements) {
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
              val value = argumentNode.value.toInt()
              visitor.visitLdcInsn(value) // write primitive value, from an Object class e.g. Integer -> int
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