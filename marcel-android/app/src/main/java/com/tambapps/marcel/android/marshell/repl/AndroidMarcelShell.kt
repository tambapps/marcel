package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.repl.jar.JarWriterFactory
import com.tambapps.marcel.repl.printer.Printer
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.MarcelDexClassLoader
import java.io.File

class AndroidMarcelShell(
    printer: Printer,
    tempDir: File,
    override val initScriptFile: File?
) : MarcelShell(
    compilerConfiguration = CompilerConfiguration(dumbbellEnabled = true),
    printer = printer,
    marcelClassLoader = MarcelDexClassLoader(),
    jarWriterFactory = DexJarWriterFactory(),
    tempDir = tempDir,
    binding = Binding(),
) {
    override suspend fun readLine(linesBufferSize: Int): String {
        TODO("Not yet implemented")
    }
}