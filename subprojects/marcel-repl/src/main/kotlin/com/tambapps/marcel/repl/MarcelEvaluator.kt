package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.JarWriter
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import java.io.File

class MarcelEvaluator constructor(
  private val binding: Binding,
  private val replCompiler: MarcelReplCompiler,
  private val scriptLoader: MarcelClassLoader,
  private val tempDir: File
) {

  @Throws(
    MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class,
    ClassNotFoundException::class
  )
  fun eval(code: String): Any? {
    val result = replCompiler.compile(code)
    val scriptNode = result.parserResult.scriptNode

    val className = scriptNode.type.simpleName
    val jarFile = File(tempDir.parentFile, "$className.jar")

    if (result.otherClasses.isNotEmpty()) {
      val libraryJar = File(tempDir.parentFile, "${className}_library.jar")
      JarWriter(libraryJar).use {
        it.writeScriptJar(result.otherClasses)
      }
      scriptLoader.addLibraryJar(libraryJar)
    }
    JarWriter(jarFile).use {
      it.writeScriptJar(result.compiledScript)
    }
    if (scriptNode.fields.isNotEmpty()) {
      throw MarcelSemanticException("Cannot define field variables in Marshell. Use global or local variables only")
    }
    return scriptLoader.loadScript(className, jarFile, binding).run()
  }
}