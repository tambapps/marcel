package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import marcel.lang.MarcelClassLoader
import java.io.File
import java.io.IOException
import java.io.Reader
import java.util.function.Consumer

class MarcelCompiler(private val compilerConfiguration: CompilerConfiguration) {

  constructor(): this(CompilerConfiguration())

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, files: Collection<File>, classConsumer: Consumer<CompiledClass>) {
    return compileSourceFiles(scriptLoader, files.map { SourceFile.fromFile(it) }, classConsumer)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, reader: Reader, className: String? = null, classConsumer: Consumer<CompiledClass>) {
    return compile(scriptLoader, reader.readText(), className, classConsumer)
  }


  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, file: File, classConsumer: Consumer<CompiledClass>) {
    return compile(scriptLoader, SourceFile.fromFile(file), classConsumer)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, text: String, className: String? = null, classConsumer: Consumer<CompiledClass>) {
    return compileSourceFiles(scriptLoader, listOf(SourceFile("$className.mcl") { text }), classConsumer)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, text: String, className: String? = null): List<CompiledClass> {
    val classes = mutableListOf<CompiledClass>()
    compileSourceFiles(scriptLoader, listOf(SourceFile("$className.mcl") { text }), classes::add)
    return classes
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, sourceFile: SourceFile): List<CompiledClass> {
    val classes = mutableListOf<CompiledClass>()
    compileSourceFiles(scriptLoader, listOf(sourceFile), classes::add)
    return classes
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, sourceFile: SourceFile, classConsumer: Consumer<CompiledClass>) {
    return compileSourceFiles(scriptLoader, listOf(sourceFile), classConsumer)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileSourceFiles(scriptLoader: MarcelClassLoader? = null, sourceFiles: Collection<SourceFile>, classConsumer: Consumer<CompiledClass>) {
    val typeResolver = JavaTypeResolver(scriptLoader)
    // adding extensions
    typeResolver.loadDefaultExtensions()

    for (sourceFile in sourceFiles) {
      val tokens = MarcelLexer().lex(sourceFile.text)
      val parser = MarcelParser(typeResolver, sourceFile.className, tokens) //if (className != null) MarcelParser(typeResolver, className, tokens) else MarcelParser(typeResolver, tokens)
      val ast = parser.parse()

      if (ast.dumbbells.isNotEmpty() && !compilerConfiguration.dumbbellEnabled) {
        throw MarcelCompilerException("Cannot use dumbbells because dumbbell is not enabled")
      }
      if (ast.dumbbells.isNotEmpty() && scriptLoader != null) {
        for (dumbbell in ast.dumbbells) {
          val artifacts = Dumbbell.pull(dumbbell)
          artifacts.forEach {
            if (it.jarFile != null) {
              scriptLoader.addLibraryJar(it.jarFile)
            }
          }
        }
      }

      val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)
      ast.classes.forEach {
        val compiledClasses = classCompiler.compileClass(it)
        compiledClasses.forEach(classConsumer)
      }
    }
  }

}