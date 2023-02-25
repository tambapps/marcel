package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.JarWriter
import com.tambapps.marcel.compiler.MarcelCompiler
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import marcel.lang.Binding
import marcel.lang.Script
import java.io.File
import java.net.URLClassLoader

class MarcelEvaluator constructor(
  private val binding: Binding,
  private val replCompiler: MarcelReplCompiler,
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

    // load the jar into the classpath
    val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), MarcelCompiler::class.java.classLoader)
    // and then run it with the new classLoader
    val clazz = classLoader.loadClass(className)
    if (Script::class.java.isAssignableFrom(clazz)) {
      val script = clazz.getDeclaredConstructor(Binding::class.java).newInstance(binding) as Script
      return script.run()
    } else {
      throw RuntimeException("This shouldn't happen")
    }
  }
}