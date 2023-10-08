package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.asm.MarcelClassWriter
import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.compiler.file.SourceFile
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser2
import com.tambapps.marcel.parser.MarcelParser2Exception
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import marcel.lang.MarcelClassLoader
import java.io.File
import java.io.IOException
import java.util.function.Consumer

class MarcelCompiler(private val configuration: CompilerConfiguration) {

  @Throws(IOException::class, MarcelLexerException::class, MarcelParser2Exception::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileToJar(scriptLoader: MarcelClassLoader? = null, files: Collection<SourceFile>, outputJar: File) {
    val classes = mutableListOf<CompiledClass>()
    compileSourceFiles(files, scriptLoader, classes::add)

    JarWriter(outputJar).use {
      classes.forEach { compiledClass ->
        it.writeClass(compiledClass)
      }
    }
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParser2Exception::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compile(fileName: String, text: String, marcelClassLoader: MarcelClassLoader? = null): List<CompiledClass> {
    return compileSourceFiles(listOf(SourceFile.from(fileName, text)), marcelClassLoader)
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParser2Exception::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileSourceFiles(sourceFiles: Collection<SourceFile>, marcelClassLoader: MarcelClassLoader? = null): List<CompiledClass> {
    val compiledClasses = mutableListOf<CompiledClass>()
    compileSourceFiles(sourceFiles, marcelClassLoader, compiledClasses::add)
    return compiledClasses
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParser2Exception::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileSourceFiles(sourceFiles: Collection<SourceFile>, marcelClassLoader: MarcelClassLoader? = null, classConsumer: Consumer<CompiledClass>) {
    val typeResolver = JavaTypeResolver(marcelClassLoader)

    // first load all classes in typeResolver
    val asts = sourceFiles.map { sourceFile ->
      val tokens = MarcelLexer().lex(sourceFile.text)
      val cst = MarcelParser2(classSimpleName = sourceFile.className, tokens = tokens).parse()
      val ast = MarcelSemantic(typeResolver, cst).apply()
      //visitAst(ast, typeResolver)

      /*
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

       */
      ast
    }

    val classWriter = MarcelClassWriter(configuration)

    // then compile them
    asts.forEach { ast ->
      //ast.extensionTypes.forEach(typeResolver::loadExtension)
      val compiledClasses = classWriter.compileDefinedClasses(ast.classes)
      //ast.extensionTypes.forEach(typeResolver::unloadExtension)
      compiledClasses.forEach(classConsumer)
    }
  }
}