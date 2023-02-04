package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParsingException
import com.tambapps.marcel.parser.exception.SemanticException
import marcel.lang.methods.DefaultMarcelMethods
import java.io.IOException
import java.io.Reader

class MarcelCompiler(private val compilerConfiguration: CompilerConfiguration) {

  constructor(): this(CompilerConfiguration.DEFAULT_CONFIGURATION)

  @Throws(IOException::class, MarcelLexerException::class, MarcelParsingException::class, SemanticException::class)
  fun compile(reader: Reader, className: String? = null): CompilationResult {
    val tokens = MarcelLexer().lex(reader)
    val typeResolver = JavaTypeResolver()
    val extensionClassLoader = ExtensionClassLoader(typeResolver)
    extensionClassLoader.loadExtensionMethods(DefaultMarcelMethods::class.java)
    val parser = if (className != null) MarcelParser(typeResolver, className, tokens) else MarcelParser(typeResolver, tokens)
    val ast = parser.parse()
    val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)
    val compiledClasses = ast.classes.map { classCompiler.compileClass(it) }
    return CompilationResult(compiledClasses)
  }

}