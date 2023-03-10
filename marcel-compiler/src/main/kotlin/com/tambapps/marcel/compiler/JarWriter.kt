package com.tambapps.marcel.compiler

import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.function.Consumer
import java.util.jar.Attributes
import java.util.jar.JarEntry

import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class JarWriter constructor(outputStream: OutputStream): Closeable, Consumer<CompiledClass> {
  constructor(file: File): this(FileOutputStream(file))

  private val manifest = Manifest().apply {
    mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
    mainAttributes[Attributes.Name("Created-By")] = "Marcel"
  }
  private val outputStream = JarOutputStream(outputStream, manifest)

  override fun accept(t: CompiledClass) {
    writeClass(t)
  }

  fun writeClass(compiledClass: CompiledClass) {
    val jarEntry = JarEntry(compiledClass.className.replace('.', '/') + ".class")
    outputStream.putNextEntry(jarEntry)
    outputStream.write(compiledClass.bytes)
    outputStream.closeEntry()
  }

  fun writeClasses(compiledClasses: List<CompiledClass>) {
    for (compiledClass in compiledClasses) {
      writeClass(compiledClass)
    }
  }

  override fun close() {
    outputStream.close()
  }
}