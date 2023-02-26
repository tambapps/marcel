package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.ast.ModuleNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import java.io.IOException
import java.io.Reader

class MarcelCompiler(private val compilerConfiguration: CompilerConfiguration) {

  constructor(): this(CompilerConfiguration.DEFAULT_CONFIGURATION)

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compile(reader: Reader, className: String? = null): CompilationResult {
    return compile(reader.readText(), className)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compile(text: String, className: String? = null): CompilationResult {
    val tokens = MarcelLexer().lex(text)
    val typeResolver = JavaTypeResolver()

    val parser = if (className != null) MarcelParser(typeResolver, className, tokens) else MarcelParser(typeResolver, tokens)
    return compile(parser.parse(), typeResolver)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compile(ast: ModuleNode, typeResolver: JavaTypeResolver): CompilationResult {

    // adding extensions
    typeResolver.loadDefaultExtensions()

    val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)
    val compiledClasses = ast.classes.flatMap {
      classCompiler.compileClass(it)
    }
    return CompilationResult(ast, compiledClasses)
  }

}