package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.signature
import com.tambapps.marcel.compiler.util.ReflectUtils
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
import java.lang.annotation.ElementType

class MarcelClassWriter(
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
   // TODO these checks need to be done on MarcelSemantic checks.forEach { it.visit(classNode, typeResolver) }

    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
    // creating class
    classWriter.visit(compilerConfiguration.classVersion,  ReflectUtils.computeAccess(
      classNode.visibility, isStatic = false
    ), classNode.type.internalName,

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

    /*
    / TODO in semantic
    for (constructor in classNode.constructors) {
        if (!constructor.startsWithOwnConstructorCall) {
          constructor.block.statements.add(0, ExpressionStatementNode(classNode.token, SuperConstructorCallNode(
            classNode.token, constructor.scope, mutableListOf()
          )))
        }
      }


    if (classNode.staticInitializationNode != null) {
      writeMethod(typeResolver, classWriter, classNode, classNode.staticInitializationNode!!)
    }
    */

    var i = 0 // using plain old for i loop because while writing method we might add some other to write (e.g. for switch)
    while (i < classNode.methods.size) {
      val methodNode = classNode.methods[i++]
      if (methodNode.isInline) continue // inline method are not to be written (?)
      writeMethod(classWriter, classNode, methodNode)
    }

    /*
    for (innerClass in classNode.innerClasses) {
      // define inner class
      compileRec(classes, innerClass)
      // Add the inner class to the outer class
      classWriter.visitInnerClass(innerClass.type.internalName, classNode.type.internalName, innerClass.type.innerName, innerClass.access)
    }
     */

    classWriter.visitEnd()
    classes.add(CompiledClass(classNode.type.className, Script::class.javaType.isAssignableFrom(classNode.type), classWriter.toByteArray()))

  }

  private fun writeField(classWriter: ClassWriter, field: FieldNode) {
    val fieldVisitor = classWriter.visitField(
      ReflectUtils.computeAccess(
        field.visibility, isStatic = field.isStatic, isFinal = field.isFinal
      )
      , field.name, field.type.descriptor,
      if (field.type.superType?.hasGenericTypes == true || field.type.directlyImplementedInterfaces.any { it.hasGenericTypes }) field.type.signature else null,
      null
    )

    // writing annotations
    for (annotation in field.annotations) {
      writeAnnotation(fieldVisitor.visitAnnotation(annotation.type.descriptor, true), annotation)
    }
  }

  private fun writeMethod(classWriter: ClassWriter, classNode: ClassNode, methodNode: MethodNode) {
    val mv = classWriter.visitMethod(ReflectUtils.computeAccess(
      classNode.visibility, isStatic = methodNode.isStatic
    ), methodNode.name, methodNode.descriptor, methodNode.signature, null)

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
    defineMethodParameters() // yes. This is to be done at the end
  }

  private fun defineMethodParameters() {
    // TODO
  }

  private fun writeAnnotation(annotationVisitor: AnnotationVisitor, annotationNode: AnnotationNode) {
    for (attr in annotationNode.attributeNodes) {
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