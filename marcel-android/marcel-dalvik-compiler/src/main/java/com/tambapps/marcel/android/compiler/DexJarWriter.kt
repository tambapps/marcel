package com.tambapps.marcel.android.compiler;

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.repl.jar.JarWriter
import java.io.File
import java.io.IOException
import java.nio.file.Files

class DexJarWriter: JarWriter() {

  private val dexConverter = DexConverter()

  override fun write(file: File, classes: List<CompiledClass>) {
    val tempJarFile = Files.createTempFile(file.parentFile!!.toPath(), null, file.name).toFile()
    try {
      super.write(tempJarFile, classes)
      dexConverter.toDexJar(tempJarFile, file)
      if (!file.setReadOnly()) { // needed because of an android security restriction
        throw IOException("Couldn't make file $file read only")
      }
    } finally {
      tempJarFile.delete()
    }
  }
}
