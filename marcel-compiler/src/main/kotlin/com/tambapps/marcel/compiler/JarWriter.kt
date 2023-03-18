package com.tambapps.marcel.compiler

import marcel.lang.util.MarcelVersion
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.function.Consumer
import java.util.jar.Attributes
import java.util.jar.JarEntry

import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.jvm.Throws

open class JarWriter constructor(outputStream: OutputStream, manifest: Manifest): Closeable, Consumer<CompiledClass> {
  constructor(outputStream: OutputStream): this(outputStream, Manifest())
  constructor(file: File): this(FileOutputStream(file))

  // isn't private because it must be accessible for DexJarWriter
  @JvmField
  protected val outputStream = JarOutputStream(outputStream, Manifest(manifest).apply {
    mainAttributes[Attributes.Name.MANIFEST_VERSION] = MarcelVersion.VERSION
    mainAttributes[Attributes.Name("Created-By")] = "Marcel"

  })

  override fun accept(t: CompiledClass) {
    writeClass(t)
  }

  @Throws(IOException::class)
  open fun writeClass(compiledClass: CompiledClass) {
    val jarEntry = JarEntry(compiledClass.className.replace('.', '/') + ".class")
    outputStream.putNextEntry(jarEntry)
    outputStream.write(compiledClass.bytes)
    outputStream.closeEntry()
  }

  @Throws(IOException::class)
  fun writeClasses(compiledClasses: Collection<CompiledClass>) {
    for (compiledClass in compiledClasses) {
      writeClass(compiledClass)
    }
  }

  @Throws(IOException::class)
  override fun close() {
    outputStream.close()
  }
}