package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import org.objectweb.asm.ClassWriter

/**
 * Marcel class writer
 */
class MarcelAsmClassWriter(
  private val symbolResolver: MarcelSymbolResolver
) : ClassWriter(COMPUTE_MAXS or COMPUTE_FRAMES) {


  /**
   * This is an important override as sometimes ASM tries to load class that we're trying to compile
   */
  override fun getCommonSuperClass(type1: String, type2: String): String {
    var class1 = resolve(type1.replace('/', '.'))
    val class2 = resolve(type2.replace('/', '.'))
    if (class1.isAssignableFrom(class2)) {
      return type1
    }
    if (class2.isAssignableFrom(class1)) {
      return type2
    }
    if (class1.isInterface || class2.isInterface) {
      return JavaType.Object.internalName
    }
    do {
      class1 = class1.superType!!
    } while (!class1.isAssignableFrom(class2))
    return class1.internalName
  }

  private fun resolve(type: String) = try { symbolResolver.of(type) }
    catch (e: MarcelSemanticException) { throw TypeNotPresentException(type, e) }
}