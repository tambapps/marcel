package com.tambapps.marcel.android.app.marcel.compiler

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.MarcelCompiler
import com.tambapps.marcel.compiler.SourceFile
import marcel.lang.android.dex.DexBytecodeTranslator
import marcel.lang.android.dex.DexJarWriter
import marcel.lang.android.dex.MarcelDexClassLoader
import java.io.File

class AndroidMarcelCompiler(
  compilerConfiguration: CompilerConfiguration,
  private val classLoader: MarcelDexClassLoader
) {

  constructor(classLoader: MarcelDexClassLoader) : this(CompilerConfiguration(), classLoader)

  private val compiler = MarcelCompiler(compilerConfiguration)

  fun compileToDex(sourceFile: SourceFile, dexJarFile: File) {
    return compileToDex(listOf(sourceFile), dexJarFile)
  }

  fun compileToDex(sourceFiles: Collection<SourceFile>, dexJarFile: File) {
    val bytes = doCompileToDexFromSources(sourceFiles)
    dexJarFile.writeBytes(bytes)
  }

  fun compileToDexJar(sourceFile: SourceFile, dexJarFile: File) {
    compileToDexJar(listOf(sourceFile), dexJarFile)
  }

  fun compileToDexJar(sourceFiles: Collection<SourceFile>, dexJarFile: File) {
    val compiledClasses = compiler.compile(classLoader, sourceFiles)
    DexJarWriter(dexJarFile.outputStream()).use { writer ->
      writer.writeClasses(compiledClasses)
    }
  }

  private fun doCompileToDexFromSources(sourceFiles: Collection<SourceFile>): ByteArray {
    return doCompileToDex(compiler.compile(classLoader, sourceFiles))
  }

  private fun doCompileToDex(compiledClasses: Collection<CompiledClass>): ByteArray {
    val translator = DexBytecodeTranslator()
    compiledClasses.forEach { translator.addClass(it.className, it.bytes) }
    return translator.dexBytes
  }
}