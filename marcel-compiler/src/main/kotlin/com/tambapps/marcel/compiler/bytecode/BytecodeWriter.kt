package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.compiler.CompilationResult
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.ModuleNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaType
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
      writeMethod(classWriter, methodNode)
    }

    classWriter.visitEnd()
    return CompilationResult(classWriter.toByteArray(), classNode.type.className)
  }

  private fun writeMethod(classWriter: ClassWriter, methodNode: MethodNode) {
    val mv = classWriter.visitMethod(methodNode.access, methodNode.name, methodNode.descriptor, null, null)
    mv.visitCode()

    for (param in methodNode.scope.parameters) {
      methodNode.scope.addLocalVariable(param.type, param.name)
    }
    val instructionGenerator = InstructionGenerator(mv, methodNode.scope)
    val maxStack = 100; //TODO - do that properly

    if (methodNode.returnType != JavaType.void && methodNode.block.type != methodNode.returnType) {
      throw SemanticException("Return type of block doesn't match method's return type")
    }
    // writing method
    instructionGenerator.visit(methodNode.block)
    // TODO may need one day to treat inner scopes
    mv.visitMaxs(maxStack, instructionGenerator.scope.localVariablesCount) //set max stack and max local variables
    mv.visitEnd()
  }
}