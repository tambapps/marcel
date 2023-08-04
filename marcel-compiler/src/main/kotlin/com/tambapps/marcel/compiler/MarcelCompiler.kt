package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.exception.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import marcel.lang.MarcelClassLoader
import java.io.File
import java.io.IOException
import java.io.Reader
import java.util.function.Consumer

class MarcelCompiler(compilerConfiguration: CompilerConfiguration): AbstractMarcelCompiler(compilerConfiguration) {

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
  fun compile(scriptLoader: MarcelClassLoader? = null, file: File): List<CompiledClass> {
    val classes = mutableListOf<CompiledClass>()
    compileSourceFiles(scriptLoader, listOf(SourceFile.fromFile(file)), classes::add)
    return classes
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, text: String, className: String? = null): List<CompiledClass> {
    val classes = mutableListOf<CompiledClass>()
    compileSourceFiles(scriptLoader, listOf(SourceFile("$className.mcl") { text }), classes::add)
    return classes
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, sourceFile: SourceFile): List<CompiledClass> {
    return compile(scriptLoader, listOf(sourceFile))
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, sourceFiles: Collection<SourceFile>): List<CompiledClass> {
    val classes = mutableListOf<CompiledClass>()
    compileSourceFiles(scriptLoader, sourceFiles, classes::add)
    return classes
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(scriptLoader: MarcelClassLoader? = null, sourceFile: SourceFile, classConsumer: Consumer<CompiledClass>) {
    return compileSourceFiles(scriptLoader, listOf(sourceFile), classConsumer)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileToJar(scriptLoader: MarcelClassLoader? = null, files: Collection<SourceFile>, outputJar: File) {
    val classes = mutableListOf<CompiledClass>()
    compileSourceFiles(scriptLoader, files, classes::add)

    JarWriter(outputJar).use {
      classes.forEach { compiledClass ->
        it.writeClass(compiledClass)
      }
    }
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileSourceFiles(marcelClassLoader: MarcelClassLoader? = null, sourceFiles: Collection<SourceFile>, classConsumer: Consumer<CompiledClass>) {
    val typeResolver = JavaTypeResolver(marcelClassLoader)

    // first load all classes in typeResolver
    val asts = sourceFiles.map { sourceFile ->
      val tokens = MarcelLexer().lex(sourceFile.text)
      val parser = MarcelParser(typeResolver, sourceFile.className, tokens) //if (className != null) MarcelParser(typeResolver, className, tokens) else MarcelParser(typeResolver, tokens)
      val ast = parser.parse()
      visitAst(ast, typeResolver)

      if (ast.dumbbells.isNotEmpty() && !compilerConfiguration.dumbbellEnabled) {
        throw MarcelCompilerException("Cannot use dumbbells because dumbbell feature is not enabled")
      }
      if (ast.dumbbells.isNotEmpty() && marcelClassLoader != null) {
        for (dumbbell in ast.dumbbells) {
          val artifacts = Dumbbell.pull(dumbbell)
          artifacts.forEach {
            if (it.jarFile != null) {
              marcelClassLoader.addLibraryJar(it.jarFile)
            }
          }
        }
      }
      ast.extensionTypes.forEach(typeResolver::loadExtension)
      ast.classes.forEach { typeResolver.registerClass(it) }
      ast
    }

    val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)

    // then compile them
    asts.forEach { ast ->
      ast.extensionTypes.forEach(typeResolver::loadExtension)
      val compiledClasses = classCompiler.compileDefinedClasses(ast.classes)
      ast.extensionTypes.forEach(typeResolver::unloadExtension)
      compiledClasses.forEach(classConsumer)
    }
  }

}