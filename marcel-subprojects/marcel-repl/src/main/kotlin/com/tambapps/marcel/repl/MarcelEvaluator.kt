package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.repl.jar.JarWriterFactory
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import java.io.File
import java.util.concurrent.ThreadLocalRandom

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
    for (importNode in result.semanticResult.imports) {
      replCompiler.addImport(importNode)
    }
    val scriptNode = result.semanticResult.scriptNode

    if (result.otherClasses.isNotEmpty()) {
      addLibraryJar(scriptNode?.type?.className, result.otherClasses)
    }

    if (scriptNode == null) return null

    if (scriptNode.fields.isNotEmpty()) {
      throw MarcelSemanticException(scriptNode.fields.first().token, "Cannot define field variables in Marshell. Use global or local variables only")
    }

    val className = scriptNode.type.simpleName
    val jarFile = File(tempDir.parentFile, "$className.jar")

    jarWriterFactory.newJarWriter(jarFile).use {
      it.writeClasses(result.compiledScript)
    }
    return scriptLoader.loadScript(className, jarFile, binding).run()
  }

  fun evalJarFile(jarFile: File, className: String?): Any? {
    if (className == null) {
      scriptLoader.addLibraryJar(jarFile)
      return null
    } else {
      return scriptLoader.loadScript(className, jarFile, binding).run()
    }
  }

  private fun addLibraryJar(prefix: String?,
                            compiledClasses: List<CompiledClass>) {
    val actualPrefix = prefix ?: ThreadLocalRandom.current().nextInt(0, Int.MAX_VALUE - 1).toString()
    val libraryJar = File(tempDir.parentFile, "${actualPrefix}_library.jar")
    jarWriterFactory.newJarWriter(libraryJar).use {
      it.writeClasses(compiledClasses)
    }
    scriptLoader.addLibraryJar(libraryJar)
  }
}