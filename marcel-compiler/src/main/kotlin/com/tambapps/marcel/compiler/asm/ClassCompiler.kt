package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ConstructorNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label

class ClassCompiler(private val compilerConfiguration: CompilerConfiguration,
                    private val typeResolver: JavaTypeResolver) {

  fun compileClass(classNode: ClassNode): List<CompiledClass> {
    defineClassMembers(classNode)
    val classes = mutableListOf<CompiledClass>()
    compileRec(classes, classNode)
    return classes
  }

  private fun compileRec(classes: MutableList<CompiledClass>, classNode: ClassNode) {
    // check that all implemented interfaces methods are defined.
    for (interfaze in classNode.type.directlyImplementedInterfaces) {
      for (interfaceMethod in typeResolver.getDeclaredMethods(interfaze).filter { it.isAbstract }) {
        val implementationMethod = typeResolver.findMethod(classNode.type, interfaceMethod.name, interfaceMethod.parameters, true)
        if (implementationMethod == null || implementationMethod.isAbstract) {
          // maybe there is a generic implementation, in which case we have to generate the method with raw types
          throw SemanticException("Class ${classNode.type} doesn't define method $interfaceMethod of interface $interfaze")
        }
        val rawInterfaceMethod = typeResolver.findMethod(interfaze.raw(), interfaceMethod.name, interfaceMethod.parameters, true)!!
        if (rawInterfaceMethod != implementationMethod) {
          // need to write implementation method with raw type
          val rawMethodNode = MethodNode.fromJavaMethod(classNode.scope, rawInterfaceMethod)
          val rawParameterExpressions = mutableListOf<ExpressionNode>()
          for (i in rawMethodNode.parameters.indices) {
            rawParameterExpressions.add(AsNode(
                interfaceMethod.parameters[i].type,
                ReferenceExpression(rawMethodNode.scope, rawMethodNode.parameters[i].name)
            ))
          }
          rawMethodNode.block.addStatement(
              FunctionCallNode(rawMethodNode.scope, implementationMethod.name, rawParameterExpressions,
                  ReferenceExpression.thisRef(rawMethodNode.scope)))
          classNode.methods.add(rawMethodNode)
        }
      }
    }
    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
    // creating class
    classWriter.visit(compilerConfiguration.classVersion,  classNode.access, classNode.internalName, classNode.type.signature, classNode.superType.internalName,
      classNode.type.directlyImplementedInterfaces.map { it.internalName }.toTypedArray())

    if (classNode.constructorsCount == 0) {
      // if no constructor is defined, we'll define one for you
      classNode.methods.add(ConstructorNode.emptyConstructor(classNode))
    }
    for (methodNode in classNode.methods) {
      if (methodNode.isInline) continue // inline method are not to be written (?)
      writeMethod(typeResolver, classWriter, classNode, methodNode)
    }

    for (innerClass in classNode.innerClasses) {
      // define inner class
      compileRec(classes, innerClass)
      // Add the inner class to the outer class
      classWriter.visitInnerClass(innerClass.type.internalName, classNode.type.internalName, innerClass.type.innerName, innerClass.access)
    }

    classWriter.visitEnd()
    classes.add(CompiledClass(classNode.type.className, classWriter.toByteArray()))
  }

  private fun writeMethod(typeResolver: JavaTypeResolver, classWriter: ClassWriter, classNode: ClassNode, methodNode: MethodNode) {
    val mv = classWriter.visitMethod(methodNode.access, methodNode.name, methodNode.descriptor, methodNode.signature, null)
    mv.visitCode()
    val methodStartLabel = Label()
    mv.visitLabel(methodStartLabel)

    if (!methodNode.isStatic && !methodNode.isConstructor) {
      methodNode.scope.addLocalVariable(classNode.type, "this")
    }

    for (param in methodNode.parameters) {
      methodNode.scope.addLocalVariable(param.type, param.name)
    }
    val instructionGenerator = InstructionGenerator(classNode, methodNode, typeResolver, mv)

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

    val methodEndLabel = Label()
    mv.visitLabel(methodEndLabel)
    mv.visitMaxs(0, 0) // args ignored since we used the flags COMPUTE_MAXS and COMPUTE_FRAMES
    mv.visitEnd()

    for (parameter in methodNode.parameters) {
      mv.visitLocalVariable(parameter.name,  parameter.type.descriptor, parameter.type.signature,
          methodStartLabel, methodEndLabel,
          methodNode.scope.findLocalVariable(parameter.name)!!.index)
    }
  }

  private fun defineClassMembers(classNode: ClassNode) {
    classNode.methods.forEach { typeResolver.defineMethod(classNode.type, it) }
    classNode.innerClasses.forEach { defineClassMembers(it) }
  }
}