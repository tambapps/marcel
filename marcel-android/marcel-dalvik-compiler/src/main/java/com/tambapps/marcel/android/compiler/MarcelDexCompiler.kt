package com.tambapps.marcel.android.compiler

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.MarcelCompiler
import com.tambapps.marcel.compiler.file.SourceFile
import marcel.lang.MarcelDexClassLoader
import java.io.File
import java.nio.file.Files

class MarcelDexCompiler(
  compilerConfiguration: CompilerConfiguration,
  private val classLoader: MarcelDexClassLoader
) {

  constructor(classLoader: MarcelDexClassLoader) : this(CompilerConfiguration(), classLoader)

  private val compiler = MarcelCompiler(compilerConfiguration)
  private val dexConverter = DexConverter()

  fun compileToDexJar(sourceFile: SourceFile, dexJarFile: File) {
    compileToDexJar(listOf(sourceFile), dexJarFile)
  }

  fun compileToDexJar(sourceFiles: Collection<SourceFile>, dexJarFile: File) {
    val tempJarFile = Files.createTempFile(dexJarFile.parentFile!!.toPath(), null, dexJarFile.name).toFile()
    try {
      compiler.compileToJar(classLoader, sourceFiles, tempJarFile)
      dexConverter.toDexJar(tempJarFile, dexJarFile)
    } finally {
      tempJarFile.delete()
    }
  }

}