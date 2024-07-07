package com.tambapps.marcel.repl.jar

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.MarcelJarOutputStream
import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

// open because of dex jars
open class JarWriter {

  @Throws(IOException::class)
  open fun write(file: File, classes: List<CompiledClass>) {
    MarcelJarOutputStream(file).use {
      it.writeClasses(classes)
    }
  }

}