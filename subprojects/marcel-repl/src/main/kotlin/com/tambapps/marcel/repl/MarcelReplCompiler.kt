package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.ParserConfiguration
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import kotlin.jvm.Throws

class MarcelReplCompiler(
  compilerConfiguration: CompilerConfiguration,
  private val typeResolver: JavaTypeResolver,
) {

  private val lexer = MarcelLexer(false)
  private val definedFunctions = mutableSetOf<MethodNode>()
  private val _definedClasses = mutableListOf<JavaType>()
  val definedClasses: List<JavaType> get() = _definedClasses
  private val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)
  private val parserConfiguration = ParserConfiguration(true)
  @Volatile
  var parserResult: ParserResult? = null
    private set

  fun compile(text: String): ReplCompilerResult {
    val result = parse(text)
    val scriptNode = result.scriptNode
    // writing script. class members were defined when parsing
    val compiledScriptClass = classCompiler.compileDefinedClass(scriptNode)
    val otherClasses = mutableListOf<CompiledClass>()

    for (clazz in result.classes) {
      if (!clazz.isScript) {
        if (_definedClasses.any { it.className == clazz.type.className }) {
          throw MarcelSemanticException("Class ${clazz.type.simpleName} is already defined")
        }
        otherClasses.addAll(classCompiler.compileClass(clazz))
      }
    }

    // keeping function for next runs. Needs to be AFTER compilation because this step may add some methods (e.g. switch, properties...)
    definedFunctions.addAll(
      scriptNode.methods.filter {
        !it.isConstructor && it.name != "run" && it.name != "main"
      }
    )
    _definedClasses.addAll(result.classes.filter { !it.isScript }.map { it.type })
    return ReplCompilerResult(result, compiledScriptClass, otherClasses)
  }

  fun tryParseWithoutUpdate(text: String): ParserResult? {
    return try {
      updateAndGet(text, true)
    }  catch (e: Exception) {
      when (e) {
        is MarcelLexerException, is MarcelParserException, is MarcelSemanticException -> null
        else -> throw e
      }
    }
  }

  fun tryParse(text: String): ParserResult? {
    return try {
      updateAndGet(text)
    }  catch (e: Exception) {
      when (e) {
        is MarcelLexerException, is MarcelParserException, is MarcelSemanticException -> null
        else -> throw e
      }
    }
  }

  @Throws(MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun parse(text: String): ParserResult {
    return updateAndGet(text)
  }

  @Synchronized
  private fun updateAndGet(text: String, skipUpdate: Boolean = false): ParserResult {
    if (parserResult != null) {
      typeResolver.disposeClass(parserResult!!.scriptNode) // some cleaning
      if (parserResult.hashCode() == text.hashCode()) return parserResult!!
    }
    val tokens = lexer.lex(text)
    val parser = MarcelParser(typeResolver, tokens, parserConfiguration)

    val classes = parser.script(Scope.DEFAULT_IMPORTS.toMutableList(), null)
    val scriptNode = classes.first()

    for (method in definedFunctions) {
      if (scriptNode.methods.any { it.matches(method) }) {
        throw MarcelSemanticException("Method $method is already defined")
      }
      method.ownerClass = scriptNode.type
      method.scope.classType = scriptNode.type
      scriptNode.methods.add(method)
    }

    typeResolver.defineClassMembers(scriptNode)
    val r = ParserResult(tokens, classes, text.hashCode())
    if (!skipUpdate) {
      this.parserResult = r
    }
    return r
  }
}