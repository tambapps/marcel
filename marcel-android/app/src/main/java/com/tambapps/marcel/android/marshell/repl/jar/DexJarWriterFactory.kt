package com.tambapps.marcel.android.marshell.repl.jar

import com.tambapps.marcel.android.compiler.DexJarWriter
import com.tambapps.marcel.repl.jar.JarWriterFactory
import java.io.OutputStream

class DexJarWriterFactory: JarWriterFactory {
    override fun newJarWriter(outputStream: OutputStream) = DexJarWriter(outputStream)
}