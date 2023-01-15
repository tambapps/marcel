package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.compiler.CompilationResult
import com.tambapps.marcel.parser.ast.ModuleNode
import org.objectweb.asm.ClassWriter

// https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
class BytecodeWriter {

  // TODO make java class version configurable https://www.baeldung.com/java-find-class-version

  fun generate(moduleNode: ModuleNode): CompilationResult {
    // handling only one class for now
    val classNode = moduleNode.classes.first()
    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)

    // creating class
    classWriter.visit(52,  classNode.access, classNode.internalName, null, classNode.parentType.internalName, null)
    //https://github.com/JakubDziworski/Enkel-JVM-language/blob/master/compiler/src/main/java/com/kubadziworski/bytecodegeneration/MethodGenerator.java

    for (methodNode in classNode.methods) {
      val mv = classWriter.visitMethod(methodNode.access, methodNode.name, methodNode.methodDescriptor, null, null)
      mv.visitCode()

      val instructionGenerator = InstructionGenerator(mv, methodNode.scope)
      val maxStack = 100; //TODO - do that properly

      // writing method
      instructionGenerator.visit(methodNode.block)
      // TODO may need one day to treat inner scopes
      mv.visitMaxs(maxStack, instructionGenerator.scope.localVariablesCount) //set max stack and max local variables
      mv.visitEnd()
    }

    classWriter.visitEnd()
    return CompilationResult(classWriter.toByteArray(), classNode.name)
  }

}