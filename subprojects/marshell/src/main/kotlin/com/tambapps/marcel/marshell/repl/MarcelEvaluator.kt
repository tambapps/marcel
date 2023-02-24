package com.tambapps.marcel.marshell.repl

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JarWriter
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.MarcelCompiler
import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.MethodNode

import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.Scope
import marcel.lang.Binding
import marcel.lang.Script
import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.atomic.AtomicReference

class MarcelEvaluator constructor(
  compilerConfiguration: CompilerConfiguration,
  private val typeResolver: JavaTypeResolver,
  private val tempDir: File) {

  private val lexer = MarcelLexer(false)
  private val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)
  private val definedFunctions = mutableSetOf<MethodNode>()
  private val binding = Binding()
  private val resultReference = AtomicReference<Result>()

  data class Result(val tokens: List<LexToken>, val scriptNode: ClassNode, val textHashCode: Int)

  init {
    typeResolver.loadDefaultExtensions()
  }

  fun tryParse(text: String): Result? {
    return resultReference.updateAndGet {
      try {
        updateAndGet(it, text)
      }  catch (e: Exception) {
        when (e) {
          is MarcelLexerException, is MarcelParserException, is MarcelSemanticException -> null
          else -> throw e
        }
      }
    }
  }

  fun parse(text: String): Result {
    return resultReference.updateAndGet { updateAndGet(it, text) }
  }

  private fun updateAndGet(result: Result?, text: String): Result {
    if (result != null && result.hashCode() == text.hashCode()) return result

    val tokens = lexer.lex(text)
    val parser = MarcelParser(typeResolver, tokens)

    val scriptNode = parser.script(Scope.DEFAULT_IMPORTS.toMutableList(), null)

    for (method in definedFunctions) {
      if (scriptNode.methods.any { it.matches(method) }) {
        throw MarcelSemanticException("Method $method is already defined")
      }
      method.ownerClass = scriptNode.type
      method.scope.classType = scriptNode.type
      scriptNode.methods.add(method)
    }

    typeResolver.defineClassMembers(scriptNode)
    return Result(tokens, scriptNode, text.hashCode())
  }

  @Throws(MarcelLexerException::class, MarcelParserException::class)
  fun eval(code: String): Any? {

    val result = parse(code)
    val scriptNode = result.scriptNode
    // writing script. class members were defined when parsing
    val compiledClasses = classCompiler.compileDefinedClass(scriptNode)

    // keeping function for next runs. Needs to be AFTER compilation because this step may add some methods (e.g. switch, properties...)
    definedFunctions.addAll(
      scriptNode.methods.filter {
        !it.isConstructor && it.name != "run" && it.name != "main"
      }
    )

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