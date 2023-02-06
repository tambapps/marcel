package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ConstructorNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

class ClassCompiler(private val compilerConfiguration: CompilerConfiguration,
                    private val typeResolver: JavaTypeResolver) {

  fun compileClass(classNode: ClassNode): List<CompiledClass> {
    val classes = mutableListOf<CompiledClass>()
    compileRec(classes, classNode)
    return classes
  }

  private fun compileRec(classes: MutableList<CompiledClass>, classNode: ClassNode) {
    // TODO first go through EACH class and subclasses to define methods, so that the instruction writer knows about them and their methods
    classNode.methods.forEach { typeResolver.defineMethod(classNode.type, it) }
    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
    // creating class
    classWriter.visit(compilerConfiguration.classVersion,  classNode.access, classNode.internalName, null, classNode.superType.internalName, null)

    for (innerClass in classNode.innerClasses) {
      // define inner class
      compileRec(classes, innerClass)
      // Add the inner class to the outer class
      classWriter.visitInnerClass(innerClass.type.internalName, classNode.type.internalName, innerClass.type.innerName, innerClass.access)
    }

    if (classNode.constructorsCount == 0) {
      // if no constructor is defined, we'll define one for you
      val emptyConstructorScope = MethodScope(classNode.scope, JavaMethod.CONSTRUCTOR_NAME, emptyList(), JavaType.void)
      classNode.methods.add(
        ConstructorNode(classNode.superType, Opcodes.ACC_PUBLIC, FunctionBlockNode(emptyConstructorScope, emptyList()), mutableListOf(), emptyConstructorScope),
      )
    }
    for (methodNode in classNode.methods) {
      if (methodNode.isInline) continue // inline method are not to be written (?)
      writeMethod(typeResolver, classWriter, classNode, methodNode)
    }
    classWriter.visitEnd()
    classes.add(CompiledClass(classNode.type.className, classWriter.toByteArray()))
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