package com.tambapps.marcel.marshell.repl

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
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
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.Throws

class MarcelReplCompiler(
  compilerConfiguration: CompilerConfiguration,
  private val typeResolver: JavaTypeResolver,
) {

  data class ParserResult(val tokens: List<LexToken>, val scriptNode: ClassNode, val textHashCode: Int)

  private val lexer = MarcelLexer(false)
  private val definedFunctions = mutableSetOf<MethodNode>()
  private val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)
  private val parserResultReference = AtomicReference<ParserResult>()

  fun compile(text: String): Pair<ParserResult, List<CompiledClass>> {
    val result = parse(text)
    val scriptNode = result.scriptNode
    // writing script. class members were defined when parsing
    val classes = classCompiler.compileDefinedClass(scriptNode)

    // keeping function for next runs. Needs to be AFTER compilation because this step may add some methods (e.g. switch, properties...)
    definedFunctions.addAll(
      scriptNode.methods.filter {
        !it.isConstructor && it.name != "run" && it.name != "main"
      }
    )
    return Pair(result, classes)
  }

  fun tryParseWithoutUpdate(text: String): ParserResult? {
    return try {
      updateAndGet(null, text)
    }  catch (e: Exception) {
      when (e) {
        is MarcelLexerException, is MarcelParserException, is MarcelSemanticException -> null
        else -> throw e
      }
    }
  }

  fun tryParse(text: String): ParserResult? {
    return parserResultReference.updateAndGet {
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

  @Throws(MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun parse(text: String): ParserResult {
    return parserResultReference.updateAndGet { updateAndGet(it, text) }
  }

  private fun updateAndGet(parserResult: ParserResult?, text: String): ParserResult {
    if (parserResult != null) {
      typeResolver.disposeClass(parserResult.scriptNode) // some cleaning
      if (parserResult.hashCode() == text.hashCode()) return parserResult
    }

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
    return ParserResult(tokens, scriptNode, text.hashCode())
  }
}