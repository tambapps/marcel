package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParsingException
import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ConstructorNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Binding
import marcel.lang.methods.DefaultMarcelMethods
import marcel.lang.methods.IoMarcelMethods
import org.objectweb.asm.Opcodes
import java.io.IOException
import java.io.Reader

class MarcelCompiler(private val compilerConfiguration: CompilerConfiguration) {

  constructor(): this(CompilerConfiguration.DEFAULT_CONFIGURATION)

  @Throws(IOException::class, MarcelLexerException::class, MarcelParsingException::class, SemanticException::class)
  fun compile(reader: Reader, className: String? = null): CompilationResult {
    val tokens = MarcelLexer().lex(reader)
    val typeResolver = JavaTypeResolver()
    val extensionClassLoader = ExtensionClassLoader(typeResolver)
    extensionClassLoader.loadExtensionMethods(DefaultMarcelMethods::class.java, IoMarcelMethods::class.java)
    val parser = if (className != null) MarcelParser(typeResolver, className, tokens) else MarcelParser(typeResolver, tokens)
    val ast = parser.parse()
    val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)
    val compiledClasses = ast.classes.flatMap {
      if (it.isScript) addScriptMethods(it)
      classCompiler.compileClass(it)
    }
    return CompilationResult(compiledClasses)
  }

  private fun addScriptMethods(classNode: ClassNode) {
    classNode.addMethod(scriptEmptyConstructor(classNode))
    classNode.addMethod(scriptBindingConstructor(classNode))
  }

  private fun scriptEmptyConstructor(classNode: ClassNode): ConstructorNode {
    val emptyConstructorScope = MethodScope(classNode.scope, JavaMethod.CONSTRUCTOR_NAME, emptyList(), JavaType.void)
    return ConstructorNode(classNode.superType, Opcodes.ACC_PUBLIC, FunctionBlockNode(emptyConstructorScope, mutableListOf()), mutableListOf(), emptyConstructorScope)
  }
  private fun scriptBindingConstructor(classNode: ClassNode): ConstructorNode {
    val bindingType = JavaType.of(Binding::class.java)
    val bindingParameterName = "binding"
    val bindingConstructorParameters = mutableListOf(MethodParameter(bindingType, bindingParameterName))
    val bindingConstructorScope = MethodScope(classNode.scope, JavaMethod.CONSTRUCTOR_NAME, bindingConstructorParameters, JavaType.void)
    return ConstructorNode(classNode.superType, Opcodes.ACC_PUBLIC, FunctionBlockNode(bindingConstructorScope, mutableListOf(
      ExpressionStatementNode(
        SuperConstructorCallNode(classNode.scope, mutableListOf(
          ReferenceExpression(
        bindingConstructorScope, bindingParameterName)
        ))
      )
    )), bindingConstructorParameters, bindingConstructorScope)

  }
}