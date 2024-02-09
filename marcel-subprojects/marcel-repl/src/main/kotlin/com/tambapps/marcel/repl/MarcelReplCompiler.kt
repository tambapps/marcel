package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.AbstractMarcelCompiler
import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.asm.MarcelClassCompiler
import com.tambapps.marcel.compiler.transform.SyntaxTreeTransformer
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.repl.semantic.MarcelReplSemantic
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.imprt.ImportResolver
import com.tambapps.marcel.semantic.imprt.ImportResolverGenerator
import marcel.lang.MarcelClassLoader
import kotlin.jvm.Throws

class MarcelReplCompiler constructor(
  compilerConfiguration: CompilerConfiguration,
  private val marcelClassLoader: MarcelClassLoader,
  private val symbolResolver: ReplMarcelSymbolResolver,
): AbstractMarcelCompiler(compilerConfiguration) {

  val imports = ImportResolver.Imports.empty()
  private val lexer = MarcelLexer(false)
  private val _definedFunctions = mutableSetOf<com.tambapps.marcel.parser.cst.MethodCstNode>()
  val definedFunctions: Set<com.tambapps.marcel.parser.cst.MethodCstNode> get() = _definedFunctions
  private val classCompiler = MarcelClassCompiler(compilerConfiguration, symbolResolver)
  @Volatile
  var semanticResult: SemanticResult? = null
    private set
  private val dumbbells = mutableSetOf<String>()

  fun addRawImport(importString: String) {
    addImports(ImportResolverGenerator.generateImports(symbolResolver, listOf(MarcelParser(lexer.lex(importString)).import())))
  }

  fun addImports(imports: ImportResolver.Imports) {
    this.imports.add(imports)
  }

  fun compile(text: String): ReplCompilerResult {
    val result = parse(text)

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
          marcelClassLoader.addLibraryJar(it.jarFile)
        }
      }
    }
    var compiledScriptClass = emptyList<CompiledClass>()
    // compiling other classes first so that the script can find them
    val otherClasses = result.classes
      .filter { !it.isScript }
      .flatMap {
        symbolResolver.defineLibraryClass(it)
        classCompiler.compileDefinedClass(it)
      }

    val scriptNode = result.scriptNode
    if (scriptNode != null) {
      // writing script. class members were defined when parsing
      compiledScriptClass = classCompiler.compileDefinedClass(scriptNode)
    }

    return ReplCompilerResult(result, compiledScriptClass, otherClasses)
  }

  fun tryParseWithoutUpdate(text: String): SemanticResult? {
    return try {
      updateAndGet(text, true)
    }  catch (e: Exception) {
      when (e) {
        is MarcelLexerException, is MarcelParserException, is MarcelSemanticException -> null
        else -> throw e
      }
    }
  }

  fun tryParse(text: String): SemanticResult? {
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
  fun parse(text: String): SemanticResult {
    return updateAndGet(text)
  }

  @Synchronized
  private fun updateAndGet(text: String, skipUpdate: Boolean = false): SemanticResult {
    if (semanticResult != null && semanticResult!!.scriptNode != null) {
      symbolResolver.undefineClass(semanticResult!!.scriptNode!!) // some cleaning
      if (semanticResult.hashCode() == text.hashCode()) return semanticResult!!
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
    for (dumbbell in cst.dumbbells) {
      if (dumbbells.add(dumbbell)) handleDumbbell(marcelClassLoader, dumbbell)
    }

    val semantic = MarcelReplSemantic(symbolResolver, cst, "prompt.mcl", imports)


    // defining types
    defineSymbols(symbolResolver, semantic)

    // load transformations if any
    val syntaxTreeTransformer = SyntaxTreeTransformer(symbolResolver)
    syntaxTreeTransformer.loadTransformations(semantic)

    // apply semantic analysis
    val ast = semantic.apply()

    // apply transformations if any
    syntaxTreeTransformer.applyTransformations(ast)

    // checks
    check(ast, symbolResolver)

    val r = SemanticResult(tokens, cst, ast.classes, semantic.imports, text.hashCode())
    if (!skipUpdate) {
      this.semanticResult = r
    }
    return r
  }
}