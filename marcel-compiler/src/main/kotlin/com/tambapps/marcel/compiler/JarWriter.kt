package com.tambapps.marcel.compiler

import java.io.File
import java.io.FileOutputStream
import java.util.jar.Attributes
import java.util.jar.JarEntry

import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class JarWriter {

  fun writeScriptJar(compilationResult: CompilationResult, outputFile: File) {
    writeScriptJar(compilationResult.classes, outputFile)
  }

  fun writeScriptJar(compiledClasses: List<CompiledClass>, outputFile: File) {
    val manifest = Manifest().apply {
      mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
      mainAttributes[Attributes.Name("Created-By")] = "Marcel"
    }
    JarOutputStream(FileOutputStream(outputFile), manifest).use { outputStream ->
      for (compiledClass in compiledClasses) {
        val jarEntry = JarEntry(compiledClass.className.replace('.', '/') + ".class")
        outputStream.putNextEntry(jarEntry)
        outputStream.write(compiledClass.bytes)
        outputStream.closeEntry()
      }
    }
  }
}