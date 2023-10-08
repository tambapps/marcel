package com.tambapps.marcel.compiler.extensions

import com.tambapps.marcel.compiler.util.AsmUtils
import com.tambapps.marcel.semantic.type.JavaPrimitiveType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaType.Companion.boolean
import com.tambapps.marcel.semantic.type.JavaType.Companion.byte
import com.tambapps.marcel.semantic.type.JavaType.Companion.char
import com.tambapps.marcel.semantic.type.JavaType.Companion.double
import com.tambapps.marcel.semantic.type.JavaType.Companion.float
import com.tambapps.marcel.semantic.type.JavaType.Companion.int
import com.tambapps.marcel.semantic.type.JavaType.Companion.long
import com.tambapps.marcel.semantic.type.JavaType.Companion.short
import com.tambapps.marcel.semantic.type.JavaType.Companion.void
import com.tambapps.marcel.semantic.type.JavaTyped
import org.objectweb.asm.Opcodes
import java.lang.Boolean

val JavaTyped.internalName: String get() = AsmUtils.getInternalName(type)
val JavaTyped.signature: String get() {
  if (type.primitive) return descriptor
  val builder = StringBuilder("L$internalName")
  if (type.genericTypes.isNotEmpty()) {
    type.genericTypes.joinTo(buffer = builder, separator = "", prefix = "<", postfix = ">", transform = { it.descriptor })
  }
  builder.append(";")
  type.directlyImplementedInterfaces.joinTo(buffer = builder, separator = "", transform = { it.signature })
  return builder.toString()
}

val JavaTyped.descriptor: String get() {
  val type = this.type
  return if (type.isLoaded) AsmUtils.getClassDescriptor(type.realClazz)
  else {
    val descriptor = AsmUtils.getObjectClassDescriptor(type.className)
    if (type.isAnnotation) "@$descriptor" else descriptor
  }
}

val JavaType.signature: String get() {
  if (primitive) return descriptor
  val builder = StringBuilder("L$internalName")
  if (genericTypes.isNotEmpty()) {
    genericTypes.joinTo(buffer = builder, separator = "", prefix = "<", postfix = ">", transform = { it.descriptor })
  }
  builder.append(";")
  directlyImplementedInterfaces.joinTo(buffer = builder, separator = "", transform = { it.signature })
  return builder.toString()
}
private val PRIMITIVE_CODES = mapOf(
  Pair(void, PrimitiveAsmCodes(Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN, 0,0,0,0, 0)),
  Pair(int, PrimitiveAsmCodes(Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM)),
  Pair(long, PrimitiveAsmCodes(Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LRETURN, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV, Opcodes.LREM)),
  Pair(float, PrimitiveAsmCodes(Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FRETURN, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV, Opcodes.FREM)),
  Pair(double, PrimitiveAsmCodes(Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DRETURN, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV, Opcodes.DREM)),
  // apparently we use int instructions to store booleans
  Pair(boolean, PrimitiveAsmCodes(Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN,  0,0,0,0, 0)),
  Pair(char, PrimitiveAsmCodes(Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM)),
// TODO verify byte and short OpCodes
  Pair(byte, PrimitiveAsmCodes(Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM)),
  Pair(short, PrimitiveAsmCodes(Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM))
)
private val OBJECT_CODES = AsmCodes(Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN)

private open class AsmCodes(
  val loadCode: Int,
  val storeCode: Int,
  val returnCode: Int
)

private class PrimitiveAsmCodes(
  loadCode: Int,
  storeCode: Int,
  returnCode: Int,
  val addCode: Int,
  val subCode: Int,
  val mulCode: Int,
  val divCode: Int,
  val modCode: Int,
) :
  AsmCodes(loadCode, storeCode, returnCode)

val JavaType.loadCode: Int get() = (if (primitive) PRIMITIVE_CODES.getValue(asPrimitiveType) else OBJECT_CODES).loadCode
val JavaType.storeCode: Int get() = (if (primitive) PRIMITIVE_CODES.getValue(asPrimitiveType) else OBJECT_CODES).storeCode
val JavaType.returnCode: Int get() = (if (primitive) PRIMITIVE_CODES.getValue(asPrimitiveType) else OBJECT_CODES).returnCode
val JavaPrimitiveType.addCode: Int get() = PRIMITIVE_CODES.getValue(asPrimitiveType).addCode
val JavaPrimitiveType.subCode: Int get() = PRIMITIVE_CODES.getValue(asPrimitiveType).subCode
val JavaPrimitiveType.mulCode: Int get() = PRIMITIVE_CODES.getValue(asPrimitiveType).mulCode
val JavaPrimitiveType.divCode: Int get() = PRIMITIVE_CODES.getValue(asPrimitiveType).divCode
val JavaPrimitiveType.modCode: Int get() = PRIMITIVE_CODES.getValue(asPrimitiveType).modCode
