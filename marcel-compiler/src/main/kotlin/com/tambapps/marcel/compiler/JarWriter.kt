package com.tambapps.marcel.compiler

import java.io.File
import java.io.FileOutputStream
import java.util.jar.Attributes
import java.util.jar.JarEntry

import java.util.jar.JarOutputStream
import java.util.jar.Manifest


class JarWriter {

  fun writeScriptJar(className: String, classBytes: ByteArray, outputFile: File) {
    val manifest = Manifest().apply {
      mainAttributes[Attributes.Name.MAIN_CLASS] = className
      mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
      mainAttributes[Attributes.Name("Created-By")] = "Marcel"
    }

    JarOutputStream(FileOutputStream(outputFile), manifest).use { outputStream ->
      val jarEntry = JarEntry(className.replace('.', '/') + ".class")
      outputStream.putNextEntry(jarEntry)
      outputStream.write(classBytes)
      outputStream.closeEntry()
    }

  }
}