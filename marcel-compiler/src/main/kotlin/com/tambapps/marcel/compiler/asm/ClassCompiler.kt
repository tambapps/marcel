package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ConstructorNode
import com.tambapps.marcel.parser.ast.FieldNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.DefaultValue
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ClassCompiler(private val compilerConfiguration: CompilerConfiguration,
                    private val typeResolver: JavaTypeResolver) {

  fun compileClass(classNode: ClassNode): List<CompiledClass> {
    typeResolver.registerClass(classNode)
   return compileDefinedClass(classNode)
  }

  fun compileDefinedClasses(classNodes: List<ClassNode>): List<CompiledClass> {
    val classes = mutableListOf<CompiledClass>()
    classNodes.forEach { classes.addAll(compileDefinedClass(it)) }
    return classes
  }
  fun compileDefinedClass(classNode: ClassNode): List<CompiledClass> {
    val classes = mutableListOf<CompiledClass>()
    compileRec(classes, classNode)
    return classes
  }

  private fun compileRec(classes: MutableList<CompiledClass>, classNode: ClassNode) {
    // TODO check for final fields not initialized
    // check that all implemented interfaces methods are defined.
    for (interfaze in classNode.type.directlyImplementedInterfaces) {
      for (interfaceMethod in typeResolver.getDeclaredMethods(interfaze).filter { it.isAbstract }) {
        val implementationMethod = typeResolver.findMethod(classNode.type, interfaceMethod.name, interfaceMethod.parameters, true)
        if (implementationMethod == null || implementationMethod.isAbstract) {
          // maybe there is a generic implementation, in which case we have to generate the method with raw types
          throw MarcelSemanticException(classNode.token, "Class ${classNode.type} doesn't define method $interfaceMethod of interface $interfaze")
        }
        val rawInterfaceMethod = typeResolver.findMethod(interfaze.raw(), interfaceMethod.name, interfaceMethod.parameters, true)!!
        // we only need the match on parameters (== ignoring return type) because returning a more specific type is still a valid definition that doesn't need another implementation
        if (!rawInterfaceMethod.parameterMatches(implementationMethod)) {
          // need to write implementation method with raw type
          val rawMethodNode = MethodNode.fromJavaMethod(classNode.scope, rawInterfaceMethod)
          val rawParameterExpressions = mutableListOf<ExpressionNode>()
          for (i in rawMethodNode.parameters.indices) {
            rawParameterExpressions.add(AsNode(LexToken.dummy(),
                    rawMethodNode.scope,
                interfaceMethod.parameters[i].type,
                ReferenceExpression(LexToken.dummy(), rawMethodNode.scope, rawMethodNode.parameters[i].name)
            ))
          }
          rawMethodNode.block.addStatement(
            SimpleFunctionCallNode(LexToken.dummy(), rawMethodNode.scope, implementationMethod.name, rawParameterExpressions,
                  ReferenceExpression.thisRef(rawMethodNode.scope)))
          classNode.methods.add(rawMethodNode)
        }
      }
    }

    // now check for conflicting methods
    for (methodNode in classNode.methods) {
      // also define method parameters. Couldn't do it in parser because it would mean parse the type
      methodNode.scope.defineParametersInScope()
      val conflictMethod = classNode.methods.find { methodNode !== it && it.matches(methodNode) }
      if (conflictMethod != null) throw MarcelSemanticException(
        conflictMethod.token,
        "Method $methodNode conflicts with $conflictMethod"
      )
    }

    if (classNode.type.superType != null && classNode.type.superType!!.isInterface) {
      // TODO need deeper checks e.g. class is not a final, and is accessible from this package
      throw MarcelSemanticException(classNode.token, "Cannot extend an interface")
    }
    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
    // creating class
    classWriter.visit(compilerConfiguration.classVersion,  classNode.access, classNode.internalName,

      if (classNode.type.superType?.hasGenericTypes == true || classNode.type.directlyImplementedInterfaces.any { it.hasGenericTypes }) classNode.type.signature else null,
      classNode.superType.internalName,
      classNode.type.directlyImplementedInterfaces.map { it.internalName }.toTypedArray())

    if (classNode.constructorsCount == 0) {
      // if no constructor is defined, we'll define one for you
      classNode.methods.add(ConstructorNode.emptyConstructor(classNode))
    } else {
      for (constructor in classNode.constructors) {
        if (!constructor.startsWithSuperCall) {
          constructor.block.statements.add(0, ExpressionStatementNode(LexToken.dummy(), SuperConstructorCallNode(
            LexToken.dummy(), constructor.scope, mutableListOf()
          )))
        }
      }
    }
    for (field in classNode.fields) {
      writeField(classWriter, classNode, field)
    }

    if (classNode.staticInitializationNode != null) {
      writeMethod(typeResolver, classWriter, classNode, classNode.staticInitializationNode!!)
    }

    var i = 0 // using plain old fori loop because while writting method we might add some other to write (e.g. for switch)
    while (i < classNode.methods.size) {
      val methodNode = classNode.methods[i++]
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
    classes.add(CompiledClass(classNode.type.className, classNode.isScript, classWriter.toByteArray()))
  }

  private fun writeField(classWriter: ClassWriter, classNode: ClassNode, marcelField: FieldNode) {
    classWriter.visitField(marcelField.access, marcelField.name, marcelField.type.descriptor,
      if (marcelField.type.superType?.hasGenericTypes == true || marcelField.type.directlyImplementedInterfaces.any { it.hasGenericTypes }) marcelField.type.signature else null,
      null
      )

    if (marcelField.initialValue == null || marcelField.initialValue == marcelField.type.defaultValueExpression) return
    if (!marcelField.isStatic) {
      // non-static fields should be initialized in constructors
      for (constructor in classNode.constructors) {
        // FIXME we might assign values twice, if a constructor calls another constructor
        val initialValue = marcelField.initialValue!!
        if (initialValue is LiteralArrayNode && initialValue.elements.isEmpty() &&
          (marcelField.type.isArray || Collection::class.javaType.isAssignableFrom(marcelField.type))) {
          initialValue.type = JavaType.arrayTypeFrom(marcelField.type)
        }
        constructor.block.addStatement(
          FieldAssignmentNode(LexToken.dummy(),
            constructor.scope, GetFieldAccessOperator(LexToken.dummy(), ReferenceExpression.thisRef(constructor.scope),
              ReferenceExpression(LexToken.dummy(), constructor.scope, marcelField.name), false), initialValue
          )
        )
      }
    } else {
      // static fields should be initialized in static initialization block
      val staticInitializationNode = classNode.getOrInitStaticInitializationNode()
      staticInitializationNode.block.addStatement(
        VariableAssignmentNode(LexToken.dummy(),
          staticInitializationNode.scope,
          marcelField.name, marcelField.initialValue!!))
    }
  }
  private fun writeMethod(typeResolver: JavaTypeResolver, classWriter: ClassWriter, classNode: ClassNode, methodNode: MethodNode) {
    val mv = classWriter.visitMethod(methodNode.access, methodNode.name, methodNode.descriptor, methodNode.signature, null)
    mv.visitCode()
    val methodStartLabel = Label()
    mv.visitLabel(methodStartLabel)

    val instructionGenerator = InstructionGenerator(classNode, methodNode, typeResolver, mv)

    if (methodNode.isConstructor) {
      classNode.fields.forEach { it.alreadySet = false }
    }
    // writing method
    instructionGenerator.visit(methodNode.block)

    // checking return type AFTER having generated code because we want variable types to have been resolved
    val methodReturnType = methodNode.returnType
    val blockReturnType = methodNode.block.getType(typeResolver)
    if (methodReturnType != JavaType.void && !methodReturnType.isAssignableFrom(blockReturnType) &&
      !blockReturnType.isAssignableFrom(methodReturnType)
      && methodReturnType.primitive && !blockReturnType.primitive) {
      throw MarcelSemanticException(methodNode.token, "Return type of method $methodNode doesn't match method return type. "
          + "Expected $methodReturnType but got $blockReturnType")
    }

    val methodEndLabel = Label()
    mv.visitLabel(methodEndLabel)
    mv.visitMaxs(0, 0) // args ignored since we used the flags COMPUTE_MAXS and COMPUTE_FRAMES
    mv.visitEnd()

    defineMethodParameters(mv, methodNode, methodStartLabel, methodEndLabel)
  }

  private fun defineMethodParameters(mv: MethodVisitor, methodNode: MethodNode, methodStartLabel: Label, methodEndLabel: Label) {
    for (i in methodNode.parameters.indices) {
      val parameter = methodNode.parameters[i]
      // this is important, to be able to resolve marcel method parameter names
      mv.visitParameter(parameter.name, if (parameter.isFinal) Opcodes.ACC_FINAL else 0)

      if (parameter.defaultValue != null) {
        val annotationVisitor = mv.visitParameterAnnotation(i, DefaultValue::class.javaType.descriptor, true)
        when (parameter.type) {
          JavaType.int, JavaType.Integer -> annotationVisitor.visit("defaultIntValue",
            (parameter.defaultValue as? IntConstantNode)?.value ?: throw MarcelSemanticException("Must specify int constant for an int method default parameter"))
          JavaType.long, JavaType.Long -> annotationVisitor.visit("defaultLongValue",
            (parameter.defaultValue as? LongConstantNode)?.value ?: throw MarcelSemanticException("Must specify long constant for an int method default parameter"))
          JavaType.float, JavaType.Float -> annotationVisitor.visit("defaultFloatValue",
            (parameter.defaultValue as? FloatConstantNode)?.value ?: throw MarcelSemanticException("Must specify float constant for an int method default parameter"))
          JavaType.double, JavaType.Double -> annotationVisitor.visit("defaultDoubleValue",
            (parameter.defaultValue as? DoubleConstantNode)?.value ?: throw MarcelSemanticException("Must specify double constant for an int method default parameter"))
          JavaType.char, JavaType.Character -> annotationVisitor.visit("defaultCharValue",
            (parameter.defaultValue as? CharConstantNode)?.value?.get(0) ?: throw MarcelSemanticException("Must specify char constant for an int method default parameter"))
          JavaType.String -> annotationVisitor.visit("defaultStringValue",
            (parameter.defaultValue as? StringConstantNode)?.value ?: throw MarcelSemanticException("Must specify string constant for an int method default parameter"))
          else -> if (parameter.defaultValue !is NullValueNode) throw MarcelSemanticException(parameter.token, "Object parameters can only have null as default value")
        }
      }
      mv.visitLocalVariable(parameter.name,  parameter.type.descriptor, parameter.type.signature,
        methodStartLabel, methodEndLabel, methodNode.scope.findLocalVariable(parameter.name)!!.index)
    }
  }
}