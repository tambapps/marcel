package com.tambapps.marcel.repl

import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.exception.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.repl.jar.JarWriterFactory
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import java.io.File

class MarcelEvaluator constructor(
  private val binding: Binding,
  private val replCompiler: MarcelReplCompiler,
  private val scriptLoader: MarcelClassLoader,
  private val jarWriterFactory: JarWriterFactory,
  private val tempDir: File
) {

  @Throws(
    MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class,
    ClassNotFoundException::class
  )
  fun eval(code: String): Any? {
    val result = replCompiler.compile(code)
    for (importNode in result.parserResult.imports) {
      replCompiler.addImport(importNode)
    }
    val scriptNode = result.parserResult.scriptNode ?: return null

    val className = scriptNode.type.simpleName
    val jarFile = File(tempDir.parentFile, "$className.jar")

    if (result.otherClasses.isNotEmpty()) {
      val libraryJar = File(tempDir.parentFile, "${className}_library.jar")
      jarWriterFactory.newJarWriter(libraryJar).use {
        it.writeClasses(result.otherClasses)
      }
      scriptLoader.addLibraryJar(libraryJar)
    }
    jarWriterFactory.newJarWriter(jarFile).use {
      it.writeClasses(result.compiledScript)
    }
    if (scriptNode.fields.isNotEmpty()) {
      throw MarcelSemanticException("Cannot define field variables in Marshell. Use global or local variables only")
    }
    return scriptLoader.loadScript(className, jarFile, binding).run()
  }
}