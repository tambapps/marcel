package com.tambapps.marcel.marshell.repl

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JarWriter
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.MarcelCompiler
import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.ast.ModuleNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.Scope
import marcel.lang.Script
import java.io.File
import java.net.URLClassLoader

class MarcelEvaluator constructor(
  compilerConfiguration: CompilerConfiguration,
  private val typeResolver: JavaTypeResolver,
  private val tempDir: File) {

  private val lexer = MarcelLexer()
  private val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)

  init {
    typeResolver.loadDefaultExtensions()
  }

  @Throws(MarcelLexerException::class, MarcelParserException::class)
  fun eval(code: String): Any? {
    val tokens = lexer.lex(code)
    val parser = MarcelParser(typeResolver, tokens)


    val scriptNode = parser.script(Scope.DEFAULT_IMPORTS.toMutableList(), null)
    val moduleNode = ModuleNode()
    // TODO will need to add constructor with binding
    moduleNode.classes.add(scriptNode)


    // writting script
    val result = classCompiler.compileClass(scriptNode).first()
    val className = scriptNode.type.simpleName
    val jarFile = File(tempDir.parentFile, "$className.jar")
    JarWriter().writeScriptJar(result, jarFile)
    if (scriptNode.fields.isNotEmpty()) {
      throw MarcelSemanticException("Cannot define field variables in Marshell. Use global or local variables only")
    }
    // TODO save methods and include them in each compilation

    // load the jar into the classpath
    val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), MarcelCompiler::class.java.classLoader)
    // and then run it with the new classLoader
    val clazz = classLoader.loadClass(className)
    if (Script::class.java.isAssignableFrom(clazz)) {
      val script = clazz.getDeclaredConstructor().newInstance() as Script
      return script.run()
    } else {
      throw RuntimeException("This shouldn't happen")
    }
  }
}