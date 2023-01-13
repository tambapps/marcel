package com.tambapps.marcel.compiler.bytecode

import org.objectweb.asm.ClassWriter
import java.io.Closeable

class MethodWriter(private val classWriter: ClassWriter): Closeable {
  companion object {

  }
  override fun close() {
    TODO("Not yet implemented")
  }
}