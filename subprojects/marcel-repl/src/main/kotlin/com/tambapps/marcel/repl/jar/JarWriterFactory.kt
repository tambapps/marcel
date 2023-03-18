package com.tambapps.marcel.repl.jar

import com.tambapps.marcel.compiler.JarWriter
import java.io.File
import java.io.OutputStream

interface JarWriterFactory {

  fun newJarWriter(outputStream: OutputStream): JarWriter

  fun newJarWriter(file: File): JarWriter {
    return newJarWriter(file.outputStream())
  }

}