package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.parser.node.ScriptNode
import jdk.internal.org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

class BytecodeGenerator {


  fun generate(node: ScriptNode): ByteArray {
    val classWriter = ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS or org.objectweb.asm.ClassWriter.COMPUTE_FRAMES)

    // TODO make java class version configurable https://www.baeldung.com/java-find-class-version
    classWriter.visit(52,  Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, node.className, null, "java/lang/Object", null)
    // TODO generate main function
    //https://github.com/JakubDziworski/Enkel-JVM-language/blob/master/compiler/src/main/java/com/kubadziworski/bytecodegeneration/MethodGenerator.java

    classWriter.visitEnd()
    return classWriter.toByteArray()
  }
}