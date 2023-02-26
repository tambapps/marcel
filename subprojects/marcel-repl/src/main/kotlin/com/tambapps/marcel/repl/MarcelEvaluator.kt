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

  @Throws(MarcelLexerException::class, MarcelParserException::class)
  fun eval(code: String): Any? {
    val (result, compiledClasses) = replCompiler.compile(code)
    val scriptNode = result.scriptNode

    val className = scriptNode.type.simpleName
    val jarFile = File(tempDir.parentFile, "$className.jar")
    JarWriter().writeScriptJar(compiledClasses, jarFile)
    if (scriptNode.fields.isNotEmpty()) {
      throw MarcelSemanticException("Cannot define field variables in Marshell. Use global or local variables only")
    }
    return scriptLoader.loadScript(className, jarFile, binding).run()
  }
}