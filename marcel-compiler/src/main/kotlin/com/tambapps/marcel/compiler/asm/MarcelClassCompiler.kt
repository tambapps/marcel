package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.extensions.access
import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.signature
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import marcel.lang.Script
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MarcelClassCompiler(
  private val compilerConfiguration: CompilerConfiguration,
  private val typeResolver: JavaTypeResolver
) {

  fun compileDefinedClasses(classNodes: Collection<ClassNode>): List<CompiledClass> {
    val classes = mutableListOf<CompiledClass>()
    classNodes.forEach { compileRec(classes, it) }
    return classes
  }

  fun compileDefinedClass(classNode: ClassNode): List<CompiledClass> {
    val classes = mutableListOf<CompiledClass>()
    compileRec(classes, classNode)
    return classes
  }

  private fun compileRec(classes: MutableList<CompiledClass>, classNode: ClassNode) {
    val classWriter = MarcelAsmClassWriter(typeResolver)
    // creating class
    classWriter.visit(compilerConfiguration.classVersion, classNode.access, classNode.type.internalName,

      if (classNode.type.superType?.hasGenericTypes == true || classNode.type.directlyImplementedInterfaces.any { it.hasGenericTypes }) classNode.type.signature else null,
      classNode.type.superType!!.internalName, // we know it has a super type as it is not the Object class
      classNode.type.directlyImplementedInterfaces.map { it.internalName }.toTypedArray())

    // writing annotations
    for (annotation in classNode.annotations) {
      writeAnnotation(classWriter.visitAnnotation(annotation.type.descriptor, true), annotation)
    }

    for (field in classNode.fields) {
      writeField(classWriter, field)
    }

    var i = 0 // using plain old for i loop because while writing method we might add some other to write (e.g. for switch)
    while (i < classNode.methods.size) {
      val methodNode = classNode.methods[i++]
      if (methodNode.isInline) continue // inline method are not to be written (?)
      writeMethod(classWriter, classNode, methodNode)
    }

    for (innerClass in classNode.innerClasses) {
      // define inner class
      compileRec(classes, innerClass)
      // Add the inner class to the outer class
      classWriter.visitInnerClass(innerClass.type.internalName, classNode.type.internalName, innerClass.type.innerName, innerClass.access)
    }

    classWriter.visitEnd()
    classes.add(CompiledClass(classNode.type.className, Script::class.javaType.isAssignableFrom(classNode.type), classWriter.toByteArray()))
  }

  private fun writeField(classWriter: ClassWriter, field: FieldNode) {
    val fieldVisitor = classWriter.visitField(field.access, field.name, field.type.descriptor,
      if (field.type.superType?.hasGenericTypes == true || field.type.directlyImplementedInterfaces.any { it.hasGenericTypes }) field.type.signature else null,
      null
    )

    // writing annotations
    for (annotation in field.annotations) {
      writeAnnotation(fieldVisitor.visitAnnotation(annotation.type.descriptor, true), annotation)
    }
  }

  private fun writeMethod(classWriter: ClassWriter, classNode: ClassNode, methodNode: MethodNode) {
    val mv = classWriter.visitMethod(methodNode.access, methodNode.name, methodNode.descriptor, methodNode.signature, null)

    // writing annotations
    for (annotation in methodNode.annotations) {
      writeAnnotation(mv.visitAnnotation(annotation.type.descriptor, true), annotation)
    }

    mv.visitCode()
    val methodStartLabel = Label()
    mv.visitLabel(methodStartLabel)

    val instructionGenerator = MethodInstructionWriter(mv, typeResolver, classNode.type)
    instructionGenerator.visit(methodNode.blockStatement)

    val methodEndLabel = Label()
    mv.visitLabel(methodEndLabel)
    mv.visitMaxs(0, 0) // args ignored since we used the flags COMPUTE_MAXS and COMPUTE_FRAMES
    mv.visitEnd()
    defineMethodParameters(mv, methodNode, methodStartLabel, methodEndLabel)  // yes. This is to be done at the end
  }

  private fun defineMethodParameters(mv: MethodVisitor, methodNode: MethodNode,
                                     methodStartLabel: Label,
                                     methodEndLabel: Label) {
    for (i in methodNode.parameters.indices) {
      val parameter = methodNode.parameters[i]
      // this is important, to be able to resolve marcel method parameter names
      mv.visitParameter(parameter.name, if (parameter.isFinal) Opcodes.ACC_FINAL else 0)
      parameter.annotations.forEach {
        writeAnnotation(mv.visitParameterAnnotation(i, it.type.descriptor, true), it)
      }
      val methodVarIndex = if (methodNode.isStatic) i else 1 + i
      mv.visitLocalVariable(parameter.name,  parameter.type.descriptor, parameter.type.signature,
        methodStartLabel, methodEndLabel, methodVarIndex)
    }
  }

  private fun writeAnnotation(annotationVisitor: AnnotationVisitor, annotationNode: AnnotationNode) {
    for (attr in annotationNode.attributes) {
     val attrValue = attr.value
      if (attr.type.isEnum) {
        annotationVisitor.visitEnum(attr.name, attr.type.descriptor, attrValue.toString())
      } else {
        annotationVisitor.visit(attr.name, attrValue)
      }
    }
    annotationVisitor.visitEnd()
  }
}