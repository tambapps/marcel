package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.asm.MarcelClassCompiler
import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.compiler.file.SourceFile
import com.tambapps.marcel.compiler.transform.SyntaxTreeTransformer
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.semantic.processor.MarcelSemantic
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import marcel.lang.MarcelClassLoader
import java.io.File
import java.io.IOException
import java.util.function.Consumer

class MarcelCompiler(configuration: CompilerConfiguration): AbstractMarcelCompiler(configuration) {

  constructor(): this(CompilerConfiguration())

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileToJar(scriptLoader: MarcelClassLoader? = null, files: Collection<SourceFile>, outputJar: File) {
    val classes = mutableListOf<CompiledClass>()
    compileSourceFiles(files, scriptLoader, classes::add)

    MarcelJarOutputStream(outputJar).use {
      classes.forEach { compiledClass ->
        it.writeClass(compiledClass)
      }
    }
  }

  fun compile(file: File, scriptLoader: MarcelClassLoader? = null): List<CompiledClass> {
    return compileSourceFiles(listOf(SourceFile.fromFile(file)), scriptLoader)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(fileName: String, text: String, marcelClassLoader: MarcelClassLoader? = null): List<CompiledClass> {
    return compileSourceFiles(listOf(SourceFile.from(fileName, text)), marcelClassLoader)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileSourceFiles(sourceFiles: Collection<SourceFile>, marcelClassLoader: MarcelClassLoader? = null): List<CompiledClass> {
    val compiledClasses = mutableListOf<CompiledClass>()
    compileSourceFiles(sourceFiles, marcelClassLoader, compiledClasses::add)
    return compiledClasses
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileFiles(file: Collection<File>, marcelClassLoader: MarcelClassLoader? = null, classConsumer: Consumer<CompiledClass>) {
    compileSourceFiles(file.map(SourceFile::fromFile), marcelClassLoader, classConsumer)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileSourceFiles(sourceFiles: Collection<SourceFile>, marcelClassLoader: MarcelClassLoader? = null, classConsumer: Consumer<CompiledClass>) {
    val symbolResolver = MarcelSymbolResolver(marcelClassLoader)

    val semantics = sourceFiles.map { sourceFile ->
      val tokens = MarcelLexer().lex(sourceFile.text)
      val cst = MarcelParser(classSimpleName = sourceFile.className, tokens = tokens).parse()

      handleDumbbells(marcelClassLoader, cst)

      MarcelSemantic(symbolResolver, configuration.scriptClass.javaType, cst, sourceFile.fileName)
    }

    // defining types
    defineSymbols(symbolResolver, semantics)

    // load transformations if any
    val syntaxTreeTransformer = SyntaxTreeTransformer(configuration, symbolResolver)
    semantics.forEach { syntaxTreeTransformer.applyCstTransformations(it) }

    // apply semantic analysis
    val asts = semantics.map { it.apply() }

    // apply transformations if any
    asts.forEach { syntaxTreeTransformer.applyAstTransformations(it) }

    // checks
    asts.forEach { check(it, symbolResolver) }

    val classWriter = MarcelClassCompiler(configuration, symbolResolver)

    // then compile them
    asts.forEach { ast ->
      val compiledClasses = classWriter.compileDefinedClasses(ast.classes)
      compiledClasses.forEach(classConsumer)
    }
  }

}