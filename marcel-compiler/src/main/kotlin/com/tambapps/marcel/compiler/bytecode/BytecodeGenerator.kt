package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.ast.ModuleNode
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

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
    val instructionGenerator = InstructionGenerator(mv, methodNode.scope)
    val maxStack = 100; //TODO - do that properly

    // writing statements
    for (statement in methodNode.statements) {
      statement.accept(instructionGenerator)
    }
    mv.visitInsn(Opcodes.RETURN)
    // TODO may need one day to treat inner scopes
    mv.visitMaxs(maxStack, instructionGenerator.scope.localVariablesCount) //set max stack and max local variables
    mv.visitEnd()

    classWriter.visitEnd()
    return classWriter.toByteArray()
  }

}