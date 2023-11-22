package com.tambapps.marcel.repl.jar

import com.tambapps.marcel.compiler.JarWriter
import java.io.OutputStream

class BasicJarWriterFactory: JarWriterFactory {
  override fun newJarWriter(outputStream: OutputStream): JarWriter {
    return JarWriter(outputStream)
  }
}