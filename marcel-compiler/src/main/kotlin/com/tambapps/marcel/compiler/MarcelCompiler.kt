package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import marcel.lang.MarcelClassLoader
import java.io.IOException
import java.io.Reader

class MarcelCompiler(private val compilerConfiguration: CompilerConfiguration) {

  constructor(): this(CompilerConfiguration.DEFAULT_CONFIGURATION)

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, reader: Reader, className: String? = null): CompilationResult {
    return compile(scriptLoader, reader.readText(), className)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, text: String, className: String? = null): CompilationResult {
    val tokens = MarcelLexer().lex(text)
    val typeResolver = JavaTypeResolver(scriptLoader)

    val parser = if (className != null) MarcelParser(typeResolver, className, tokens) else MarcelParser(typeResolver, tokens)
    val ast = parser.parse()

    if (ast.dumbbells.isNotEmpty() && scriptLoader != null) {
      for (dumbbell in ast.dumbbells) {
        val artifacts = Dumbbell.pull(dumbbell)
        artifacts.forEach {
          scriptLoader.addLibraryJar(it.jarFile)
        }
      }
    }

    // adding extensions
    typeResolver.loadDefaultExtensions()

    val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)
    val compiledClasses = ast.classes.flatMap {
      classCompiler.compileClass(it)
    }
    return CompilationResult(ast, compiledClasses)
  }

}