package com.tambapps.marcel.compiler

import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParsingException
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.ModuleNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.SimpleJavaMethod
import marcel.lang.methods.MarcelDefaultMethods
import org.objectweb.asm.ClassWriter
import java.io.IOException
import java.io.Reader

class MarcelCompiler(private val compilerConfiguration: CompilerConfiguration) {

  constructor(): this(CompilerConfiguration.DEFAULT_CONFIGURATION)

  @Throws(IOException::class, MarcelLexerException::class, MarcelParsingException::class, SemanticException::class)
  fun compile(reader: Reader, className: String? = null): CompilationResult {
    val tokens = MarcelLexer().lex(reader)
    val parser = if (className != null) MarcelParser(className, tokens) else MarcelParser(tokens)
    val ast = parser.parse()
    ast.classes.forEach {
      addExtenstions(it)
    }
    return compile(ast)
  }

  private fun compile(moduleNode: ModuleNode): CompilationResult {
    // handling only one class for now
    val classNode = moduleNode.classes.first()
    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)

    // creating class
    classWriter.visit(compilerConfiguration.classVersion,  classNode.access, classNode.internalName, null, classNode.parentType.internalName, null)
    //https://github.com/JakubDziworski/Enkel-JVM-language/blob/master/compiler/src/main/java/com/kubadziworski/bytecodegeneration/MethodGenerator.java

    for (methodNode in classNode.methods) {
      writeMethod(classWriter, classNode, methodNode)
    }

    classWriter.visitEnd()
    return CompilationResult(classWriter.toByteArray(), classNode.type.className)
  }

  private fun writeMethod(classWriter: ClassWriter, classNode: ClassNode, methodNode: MethodNode) {
    val mv = classWriter.visitMethod(methodNode.access, methodNode.name, methodNode.descriptor, null, null)
    mv.visitCode()

    if (!methodNode.isStatic && !methodNode.isConstructor) {
      methodNode.scope.addLocalVariable(classNode.type, "this")
    }
    for (param in methodNode.scope.parameters) {
      methodNode.scope.addLocalVariable(param.type, param.name)
    }
    val instructionGenerator = InstructionGenerator(MethodBytecodeVisitor(mv))

    // writing method
    instructionGenerator.visit(methodNode.block)

    // checking return type AFTER having generated code because we want variable types to have been resolved
    val methodReturnType = methodNode.returnType
    val blockReturnType = methodNode.block.type
    if (methodReturnType != JavaType.void && !methodReturnType.isAssignableFrom(blockReturnType)
      && methodReturnType.primitive && !blockReturnType.primitive) {
      throw SemanticException("Return type of block doesn't match method return type. " +
          "Expected $methodReturnType but got $blockReturnType")
    }

    mv.visitMaxs(0, 0) // args ignored since we used the flags COMPUTE_MAXS and COMPUTE_FRAMES
    mv.visitEnd()
  }

  private fun addExtenstions(classNode: ClassNode) {
    for (m in MarcelDefaultMethods::class.java.declaredMethods) {
      classNode.scope.classMethods.add(SimpleJavaMethod(MarcelDefaultMethods::class.java, m.modifiers, m.name, m.parameters.map { it.type }, m.returnType))
    }
  }
}