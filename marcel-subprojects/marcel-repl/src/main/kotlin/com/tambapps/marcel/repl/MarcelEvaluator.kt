package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.repl.jar.JarWriter
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.Script
import java.io.File
import java.util.concurrent.ThreadLocalRandom

open class MarcelEvaluator constructor(
  val binding: Binding,
  private val replCompiler: MarcelReplCompiler,
  private val scriptLoader: MarcelClassLoader,
  private val jarWriter: JarWriter,
  private val tempDir: File
) {

  val lastNode: ClassNode? get() = replCompiler.semanticResult?.scriptNode
  val definedTypes get() = replCompiler.definedTypes
  val definedFunctions get() = replCompiler.definedFunctions
  val imports get() = replCompiler.imports
  val collectedDumbbells get() = replCompiler.collectedDumbbells

  @Throws(
    MarcelLexerException::class, MarcelParserException::class, MarcelSemanticException::class,
    ClassNotFoundException::class
  )
  fun eval(code: String): Any? {
    val result = replCompiler.compile(code)
    replCompiler.addImports(result.semanticResult.imports)
    val scriptNode = result.semanticResult.scriptNode

    if (result.otherClasses.isNotEmpty()) {
      addLibraryJar(scriptNode?.type?.className, result.otherClasses)
    }

    if (scriptNode == null) return null

    if (scriptNode.fields.isNotEmpty()) {
      throw MarcelSemanticException(scriptNode.fields.first().token, "Cannot define field variables in Marshell. Use global or local variables only")
    }

    val className = scriptNode.type.simpleName
    val jarFile = File(tempDir.parentFile, "$className.jar")
    jarWriter.write(jarFile, result.compiledScript)
    try {
      val script = scriptLoader.loadScript(className, jarFile, binding)
      onScriptLoaded(script)
      return script.run()
    } finally {
      scriptLoader.removeJar(jarFile)
    }
  }

  fun evalJarFile(jarFile: File, className: String?): Any? {
    if (className == null) {
      scriptLoader.addJar(jarFile)
      return null
    } else {
      val script = scriptLoader.loadScript(className, jarFile, binding)
      onScriptLoaded(script)
      return script.run()
    }
  }

  private fun addLibraryJar(prefix: String?,
                            compiledClasses: List<CompiledClass>) {
    val actualPrefix = prefix ?: ThreadLocalRandom.current().nextInt(0, Int.MAX_VALUE - 1).toString()
    val libraryJar = File(tempDir, "${actualPrefix}_library.jar")
    jarWriter.write(libraryJar, compiledClasses)
    scriptLoader.addJar(libraryJar)
  }

  fun addImport(importArgs: String) {
    replCompiler.addRawImport("import $importArgs")
  }

  // caller should catch exception and dumbbell exception
  fun evalJarFile(jarFile: File, className: String?, dumbbells: List<String>): Any? {
    // import dumbbells
    for (artifactString in dumbbells) {
      val pulledArtifacts = Dumbbell.pull(artifactString)
      pulledArtifacts.forEach {
        if (it.jarFile != null) {
          replCompiler.marcelClassLoader.addJar(it.jarFile)
        }
      }
    }

    // then run script
    return evalJarFile(jarFile, className)
  }

  protected open fun onScriptLoaded(script: Script) {}
}