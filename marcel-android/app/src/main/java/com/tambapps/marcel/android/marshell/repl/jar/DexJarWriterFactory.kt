package com.tambapps.marcel.android.marshell.repl.jar

import com.tambapps.marcel.android.compiler.DexJarFileWriter
import com.tambapps.marcel.android.compiler.DexJarWriter
import com.tambapps.marcel.compiler.JarWriter
import com.tambapps.marcel.repl.jar.JarWriterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class DexJarWriterFactory: JarWriterFactory {

    override fun newJarWriter(file: File): JarWriter {
        return DexJarFileWriter(file, FileOutputStream(file))
    }

    override fun newJarWriter(outputStream: OutputStream) = DexJarWriter(outputStream)
}