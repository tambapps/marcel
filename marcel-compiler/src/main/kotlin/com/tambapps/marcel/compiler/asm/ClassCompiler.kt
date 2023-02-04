package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.ClassWriter

class ClassCompiler(private val compilerConfiguration: CompilerConfiguration,
                    private val typeResolver: JavaTypeResolver) {

  fun compileClass(classNode: ClassNode): CompiledClass {
    classNode.methods.forEach { typeResolver.defineMethod(classNode.type, it) }
    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)

    // creating class
    classWriter.visit(compilerConfiguration.classVersion,  classNode.access, classNode.internalName, null, classNode.parentType.internalName, null)
    //https://github.com/JakubDziworski/Enkel-JVM-language/blob/master/compiler/src/main/java/com/kubadziworski/bytecodegeneration/MethodGenerator.java

    for (methodNode in classNode.methods) {
      if (methodNode.isInline) continue // inline method are not to be written (?)
      writeMethod(typeResolver, classWriter, classNode, methodNode)
    }

    classWriter.visitEnd()
    return CompiledClass(classNode.type.className, classWriter.toByteArray())
  }

  private fun writeMethod(typeResolver: JavaTypeResolver, classWriter: ClassWriter, classNode: ClassNode, methodNode: MethodNode) {
    val mv = classWriter.visitMethod(methodNode.access, methodNode.name, methodNode.descriptor, null, null)
    mv.visitCode()

    if (!methodNode.isStatic && !methodNode.isConstructor) {
      methodNode.scope.addLocalVariable(classNode.type, "this")
    }
    for (param in methodNode.scope.parameters) {
      methodNode.scope.addLocalVariable(param.type, param.name)
    }
    val instructionGenerator = InstructionGenerator(MethodBytecodeWriter(mv, typeResolver), typeResolver)

    // writing method
    instructionGenerator.visit(methodNode.block)

    // checking return type AFTER having generated code because we want variable types to have been resolved
    val methodReturnType = methodNode.returnType
    val blockReturnType = methodNode.block.getType(typeResolver)
    if (methodReturnType != JavaType.void && !methodReturnType.isAssignableFrom(blockReturnType)
      && methodReturnType.primitive && !blockReturnType.primitive) {
      throw SemanticException("Return type of block doesn't match method return type. " +
          "Expected $methodReturnType but got $blockReturnType")
    }

    mv.visitMaxs(0, 0) // args ignored since we used the flags COMPUTE_MAXS and COMPUTE_FRAMES
    mv.visitEnd()
  }

}