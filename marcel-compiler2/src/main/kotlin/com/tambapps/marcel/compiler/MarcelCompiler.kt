package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.compiler.file.SourceFile
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser2
import com.tambapps.marcel.parser.MarcelParser2Exception
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import marcel.lang.MarcelClassLoader
import java.io.File
import java.io.IOException
import java.util.function.Consumer

class MarcelCompiler(private val configuration: CompilerConfiguration) {

  @Throws(IOException::class, MarcelLexerException::class, MarcelParser2Exception::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileToJar(scriptLoader: MarcelClassLoader? = null, files: Collection<SourceFile>, outputJar: File) {
    val classes = mutableListOf<CompiledClass>()
    compileSourceFiles(scriptLoader, files, classes::add)

    JarWriter(outputJar).use {
      classes.forEach { compiledClass ->
        it.writeClass(compiledClass)
      }
    }
    for (file in files) {
      val tokens = MarcelLexer().lex(file.text)
      val cst = MarcelParser2(classSimpleName = file.className, tokens = tokens).parse()
      val rootClassNode = MarcelSemantic(cst).apply()
      rootClassNode
    }
  }

  @Throws(IOException::class, MarcelLexerException::class, MarcelParser2Exception::class, MarcelSemanticException::class, MarcelCompilerException::class)
  fun compileSourceFiles(marcelClassLoader: MarcelClassLoader? = null, sourceFiles: Collection<SourceFile>, classConsumer: Consumer<CompiledClass>) {
   // val typeResolver = JavaTypeResolver(marcelClassLoader)

    // first load all classes in typeResolver
    val asts = sourceFiles.map { sourceFile ->
      val tokens = MarcelLexer().lex(sourceFile.text)
      val cst = MarcelParser2(classSimpleName = sourceFile.className, tokens = tokens).parse()
      val ast = MarcelSemantic(cst).apply()
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

    val classCompiler = ClassCompiler(configuration)

    // then compile them
    asts.forEach { ast ->
      //ast.extensionTypes.forEach(typeResolver::loadExtension)
      val compiledClasses = classCompiler.compileDefinedClasses(ast.classes)
      //ast.extensionTypes.forEach(typeResolver::unloadExtension)
      compiledClasses.forEach(classConsumer)
    }
  }
}