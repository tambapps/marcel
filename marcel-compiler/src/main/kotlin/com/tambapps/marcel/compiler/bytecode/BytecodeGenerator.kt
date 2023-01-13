package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.parser.ast.ScriptNode
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

class BytecodeGenerator {

  // TODO make java class version configurable https://www.baeldung.com/java-find-class-version

  fun generate(node: ScriptNode): ByteArray {
    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)

    classWriter.visit(52,  Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, node.className, null, "java/lang/Object", null)
    // TODO generate main function
    //https://github.com/JakubDziworski/Enkel-JVM-language/blob/master/compiler/src/main/java/com/kubadziworski/bytecodegeneration/MethodGenerator.java

    val mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
    val maxStack = 100; //TODO - do that properly
    val localVariablesCount = 0 // TODO
    for (statement in node.children) {
      statement.write(mv)
    }
    // TODO write instruction
    mv.visitInsn(Opcodes.RETURN)
    mv.visitMaxs(maxStack, localVariablesCount) //set max stack and max local variables
    mv.visitEnd()

    classWriter.visitEnd()
    return classWriter.toByteArray()
  }
}