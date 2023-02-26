package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.JarWriter
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import marcel.lang.Binding
import java.io.File

class MarcelEvaluator constructor(
  private val binding: Binding,
  private val replCompiler: MarcelReplCompiler,
  private val scriptLoader: MarcelScriptLoader,
  private val tempDir: File
) {

  @Throws(MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class,
    ClassNotFoundException::class)
  fun eval(code: String): Any? {
    val result = replCompiler.compile(code)
    val scriptNode = result.parserResult.scriptNode

    val className = scriptNode.type.simpleName
    val jarFile = File(tempDir.parentFile, "$className.jar")
    JarWriter(jarFile).use {
      it.writeScriptJar(result.compiledScript + result.otherClasses)
    }
    if (scriptNode.fields.isNotEmpty()) {
      throw MarcelSemanticException("Cannot define field variables in Marshell. Use global or local variables only")
    }
    val eval = scriptLoader.loadScript(className, jarFile, binding).run()
    if (result.otherClasses.isNotEmpty()) {
      // TODO need separate jar for library
      // some other classes were defined? need to keep jar for future evals.
      scriptLoader.addLibraryJar(jarFile)
    }
    return eval
  }
}