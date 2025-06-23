package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.AbstractMarcelCompiler
import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.asm.MarcelClassCompiler
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.repl.semantic.MarcelReplSemantic
import com.tambapps.marcel.semantic.transform.SemanticPurpose
import com.tambapps.marcel.semantic.analysis.MarcelSemanticAnalysis
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.processor.imprt.ImportResolverGenerator
import com.tambapps.marcel.semantic.processor.imprt.MutableImportResolver
import com.tambapps.marcel.semantic.symbol.type.JavaType
import marcel.lang.MarcelClassLoader
import kotlin.jvm.Throws

class MarcelReplCompiler constructor(
  compilerConfiguration: CompilerConfiguration,
  internal val marcelClassLoader: MarcelClassLoader,
  internal val symbolResolver: ReplMarcelSymbolResolver,
): AbstractMarcelCompiler(
  if (compilerConfiguration.purpose == SemanticPurpose.REPL) compilerConfiguration
  else compilerConfiguration.withPurpose(purpose = SemanticPurpose.REPL)
) {

  val imports = MutableImportResolver.empty()
  private val lexer = MarcelLexer()
  private val _definedFunctions = mutableSetOf<MethodCstNode>()
  val definedFunctions: Set<MethodCstNode> get() = _definedFunctions
  private val _definedTypes = mutableSetOf<JavaType>()
  val definedTypes: Set<JavaType> get() = _definedTypes
  private val classCompiler = MarcelClassCompiler(compilerConfiguration, symbolResolver)
  @Volatile
  var semanticResult: SemanticResult? = null
    private set
  private val dumbbells = mutableSetOf<String>()
  val collectedDumbbells: Set<String> get() = dumbbells

  fun addRawImport(importString: String) {
    addImports(ImportResolverGenerator.generateImports(
      symbolResolver,
      listOf(MarcelParser(lexer.lex(importString)).import())
    ))
  }

  fun addImports(imports: ImportResolver) {
    this.imports.add(imports)
  }

  fun removeMethod(method: MethodCstNode) {
    _definedFunctions.remove(method)
  }

  @Throws(MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun applyAndLoadSemantic(text: String): SemanticResult {
    val result = applySemantic(text)

    // keeping function for next runs.
    result.cst.script?.methods?.forEach {
      if (it.name != "run" && it.name != "main") {
        _definedFunctions.add(it)
      }
    }

    for (artifactString in result.dumbbells) {
      val pulledArtifacts = Dumbbell.pull(artifactString)
      pulledArtifacts.forEach {
        if (it.jarFile != null) {
          marcelClassLoader.addJar(it.jarFile)
        }
      }
    }
    // compiling other classes first so that the script can find them
    for (clazz in result.classes) {
      if (!clazz.type.isScript) {
        _definedTypes.add(clazz.type)
      }
    }
    imports.add(result.imports)
    return result
  }

  fun compile(text: String): ReplCompilerResult {
    val result = applyAndLoadSemantic(text)
    var compiledScriptClass = emptyList<CompiledClass>()
    // compiling other classes first so that the script can find them
    val otherClasses = result.classes
      .filter { !it.isScript }
      .flatMap { classCompiler.compileDefinedClass(it) }

    val scriptNode = result.scriptNode
    if (scriptNode != null) {
      // writing script. class members were defined when parsing
      compiledScriptClass = classCompiler.compileDefinedClass(scriptNode)
    }
    return ReplCompilerResult(result, compiledScriptClass, otherClasses)
  }

  fun tryParseWithoutUpdateAsResult(text: String): Result<SemanticResult> {
    return try {
      Result.success(semanticallyCheck(text))
    }  catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun tryParseWithoutUpdate(text: String): SemanticResult? {
    return try {
      semanticallyCheck(text)
    }  catch (e: Exception) {
      when (e) {
        is MarcelLexerException, is MarcelParserException, is MarcelSemanticException -> null
        else -> throw e
      }
    }
  }

  fun tryApplySemantic(text: String, fetchDumbbells: Boolean = false): SemanticResult? {
    return try {
      updateAndGet(text, fetchDumbbells)
    }  catch (e: Exception) {
      when (e) {
        is MarcelLexerException, is MarcelParserException, is MarcelSemanticException -> null
        else -> throw e
      }
    }
  }

  @Throws(MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class)
  fun applySemantic(text: String): SemanticResult {
    return updateAndGet(text)
  }

  private fun semanticallyCheck(text: String, fetchDumbbells: Boolean = true): SemanticResult {
    semanticResult?.let { previousResult ->
      // don't need previous definitions, as we probably redefine some symbols that were present in the previous result
      previousResult.scriptNode?.let(symbolResolver::undefineClass)
      previousResult.classes.forEach(symbolResolver::undefineClass)
      if (semanticResult.hashCode() == text.hashCode()) return previousResult
    }
    val tokens = lexer.lex(text)
    val parser = MarcelParser(tokens)

    val cst = parser.parse()
    val cstScriptNode = cst.script
    if (cstScriptNode != null) {
      for (method in _definedFunctions) {
        if (cstScriptNode.methods.any { it == method }) {
          throw MarcelSemanticException(method.token, "Method $method is already defined")
        }
        cstScriptNode.methods.add(method)
      }
    }

    // handle dumbbells
    if (fetchDumbbells) {
      for (dumbbell in cst.dumbbells) {
        if (dumbbells.add(dumbbell)) handleDumbbell(marcelClassLoader, dumbbell)
      }
    }

    val semantic = MarcelReplSemantic(symbolResolver, cst, "prompt.mcl", imports)
    val ast = MarcelSemanticAnalysis.apply(configuration, symbolResolver, semantic)

    val r = SemanticResult(tokens, cst, ast.classes, semantic.imports, text.hashCode())
    return r
  }

  @Synchronized
  private fun updateAndGet(text: String, fetchDumbbells: Boolean = true): SemanticResult {
    val result = semanticallyCheck(text, fetchDumbbells)
    this.semanticResult = result
    return result
  }
}