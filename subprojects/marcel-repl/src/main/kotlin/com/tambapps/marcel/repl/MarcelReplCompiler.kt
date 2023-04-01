package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.asm.ClassCompiler
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.exception.MarcelParserException
import com.tambapps.marcel.parser.ParserConfiguration
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.DelegatedObject
import marcel.lang.MarcelClassLoader
import kotlin.jvm.Throws

class MarcelReplCompiler constructor(
  compilerConfiguration: CompilerConfiguration,
  private val marcelClassLoader: MarcelClassLoader,
  private val typeResolver: ReplJavaTypeResolver,
) {

  val imports = LinkedHashSet<ImportNode>()
  private val lexer = MarcelLexer(false)
  // TODO save this in ReplJavaTypeResolver. This is for android marshell, to be able to save them outside the shell which
  //  needs a reference to an Android context because of the lineReader which reads from a EditText. With this, we would be able
  private val _definedFunctions = mutableSetOf<MethodNode>()
  val definedFunctions: Set<MethodNode> get() = _definedFunctions
  private val classCompiler = ClassCompiler(compilerConfiguration, typeResolver)
  @Volatile
  var parserResult: ParserResult? = null
    private set

  fun addImport(importString: String) {
    addImport(MarcelParser(typeResolver, lexer.lex(importString), ParserConfiguration()).import())
  }
  fun addImport(importNode: ImportNode) {
    imports.add(importNode)
  }

  fun compile(text: String): ReplCompilerResult {
    val result = parse(text)

    for (artifactString in result.dumbbells) {
      val pulledArtifacts = Dumbbell.pull(artifactString)
      pulledArtifacts.forEach {
        if (it.jarFile != null) {
          marcelClassLoader.addLibraryJar(it.jarFile)
        }
      }
    }
    var compiledScriptClass = emptyList<CompiledClass>()
    val scriptNode = result.scriptNode
    if (scriptNode != null) {
      scriptNode.scope.imports.addAll(imports)
      // writing script. class members were defined when parsing
      compiledScriptClass = classCompiler.compileDefinedClass(scriptNode)

      // keeping function for next runs. Needs to be AFTER compilation because this step may add some methods (e.g. switch, properties...)
      _definedFunctions.addAll(
              scriptNode.methods.filter {
                      !it.isConstructor && it.name != "run" && it.name != "main"
              }
      )
    }
    val otherClasses = result.classes
            .filter { !it.isScript }
            .flatMap {
              typeResolver.registerLibraryClass(it)
              classCompiler.compileDefinedClass(it)
            }

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
    if (parserResult != null && parserResult!!.scriptNode != null) {
      typeResolver.disposeClass(parserResult!!.scriptNode!!) // some cleaning
      if (parserResult.hashCode() == text.hashCode()) return parserResult!!
    }
    val tokens = lexer.lex(text)
    val parser = MarcelParser(typeResolver, tokens, ParserConfiguration(
      independentScriptInnerClasses= true,
      scriptInterfaces = if (_definedFunctions.any { it.name == "getDelegate" && it.parameters.isEmpty() }) listOf(DelegatedObject::class.java)
      else emptyList()
    ))

    val module = parser.parse()

    val scriptNode = module.classes.find { it.isScript }
    if (scriptNode != null) {
      for (method in _definedFunctions) {
        if (scriptNode.methods.any { it.matches(method) }) {
          throw MarcelSemanticException("Method $method is already defined")
        }
        method.ownerClass = scriptNode.type
        method.scope.classType = scriptNode.type
        scriptNode.methods.add(method)
      }
    }
    val r = ParserResult(tokens, module.classes, module.imports, module.dumbbells, text.hashCode())
    if (!skipUpdate) {
      this.parserResult = r
    }
    return r
  }
}