package com.tambapps.marcel.android.app.marcel.compiler

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.MarcelCompiler
import com.tambapps.marcel.compiler.SourceFile
import marcel.lang.android.dex.DexBytecodeTranslator
import marcel.lang.android.dex.DexJarWriter
import marcel.lang.android.dex.MarcelDexClassLoader
import java.io.File

class AndroidMarcelCompiler(compilerConfiguration: CompilerConfiguration, private val classLoader: MarcelDexClassLoader) {

    constructor(classLoader: MarcelDexClassLoader): this(CompilerConfiguration(), classLoader)

    private val compiler = MarcelCompiler(compilerConfiguration)

    fun compile(sourceFile: SourceFile, dexJarFile: File) {
        return compile(listOf(sourceFile), dexJarFile)
    }

    fun compile(sourceFiles: Collection<SourceFile>, dexJarFile: File) {
        val compiledClasses = compiler.compile(classLoader, sourceFiles)
        val translator = DexBytecodeTranslator()
        compiledClasses.forEach { translator.addClass(it.className, it.bytes) }
        val bytes = translator.dexBytes

        DexJarWriter(dexJarFile.outputStream()).use { writer ->
            writer.write(compiledClasses.map { it.className }, bytes)
        }
    }
}