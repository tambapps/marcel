package com.tambapps.marcel.repl.jar

import com.tambapps.marcel.compiler.JarWriter
import java.io.File
import java.io.IOException
import java.io.OutputStream
import kotlin.jvm.Throws

/**
 * A provider of jar writer
 */
interface JarWriterFactory {

  @Throws(IOException::class)
  fun newJarWriter(outputStream: OutputStream): JarWriter

  @Throws(IOException::class)
  fun newJarWriter(file: File): JarWriter {
    return newJarWriter(file.outputStream())
  }

}