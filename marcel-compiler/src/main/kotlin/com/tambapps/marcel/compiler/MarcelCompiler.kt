package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ModuleNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import marcel.lang.MarcelClassLoader
import java.io.File
import java.io.IOException
import java.io.Reader

class MarcelCompiler(private val compilerConfiguration: CompilerConfiguration) {

  constructor(): this(CompilerConfiguration.DEFAULT_CONFIGURATION)

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, files: Collection<File>): CompilationResult {
    return compileSourceFiles(scriptLoader, files.map { SourceFile.fromFile(it) })
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, reader: Reader, className: String? = null): CompilationResult {
    return compile(scriptLoader, reader.readText(), className)
  }


  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, file: File): CompilationResult {
    return compile(scriptLoader, SourceFile.fromFile(file))
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, text: String, className: String? = null): CompilationResult {
    return compileSourceFiles(scriptLoader, listOf(SourceFile("$className.mcl") { text }))
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, sourceFile: SourceFile): CompilationResult {
    return compileSourceFiles(scriptLoader, listOf(sourceFile))
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun compileSourceFiles(scriptLoader: MarcelClassLoader? = null, sourceFiles: Collection<SourceFile>): CompilationResult {
    val typeResolver = JavaTypeResolver(scriptLoader)
    // adding extensions
    typeResolver.loadDefaultExtensions()
    val dumbbells = mutableSetOf<String>()
    val classes = mutableListOf<ClassNode>()
    val module = ModuleNode(dumbbells, classes)

    val compiledClasses = mutableListOf<CompiledClass>()
    for (sourceFile in sourceFiles) {
      val tokens = MarcelLexer().lex(sourceFile.text)
      val parser = MarcelParser(typeResolver, sourceFile.className, tokens) //if (className != null) MarcelParser(typeResolver, className, tokens) else MarcelParser(typeResolver, tokens)
      val ast = parser.parse()

      if (ast.dumbbells.isNotEmpty() && scriptLoader != null) {
        for (dumbbell in ast.dumbbells) {
          val artifacts = Dumbbell.pull(dumbbell)
          artifacts.forEach {
            scriptLoader.addLibraryJar(it.jarFile)
          }
        }
      }

      val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)
      ast.classes.forEach {
        compiledClasses.addAll(classCompiler.compileClass(it))
      }
      dumbbells.addAll(ast.dumbbells)
      classes.addAll(ast.classes)
    }
    return CompilationResult(module, compiledClasses)
  }

}