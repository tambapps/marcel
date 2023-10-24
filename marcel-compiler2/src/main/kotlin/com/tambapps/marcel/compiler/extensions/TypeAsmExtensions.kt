package com.tambapps.marcel.compiler.extensions

import com.tambapps.marcel.compiler.util.AsmUtils
import com.tambapps.marcel.semantic.type.JavaArrayType
import com.tambapps.marcel.semantic.type.JavaPrimitiveType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaType.Companion.boolean
import com.tambapps.marcel.semantic.type.JavaType.Companion.booleanArray
import com.tambapps.marcel.semantic.type.JavaType.Companion.byte
import com.tambapps.marcel.semantic.type.JavaType.Companion.byteArray
import com.tambapps.marcel.semantic.type.JavaType.Companion.char
import com.tambapps.marcel.semantic.type.JavaType.Companion.charArray
import com.tambapps.marcel.semantic.type.JavaType.Companion.double
import com.tambapps.marcel.semantic.type.JavaType.Companion.doubleArray
import com.tambapps.marcel.semantic.type.JavaType.Companion.float
import com.tambapps.marcel.semantic.type.JavaType.Companion.floatArray
import com.tambapps.marcel.semantic.type.JavaType.Companion.int
import com.tambapps.marcel.semantic.type.JavaType.Companion.intArray
import com.tambapps.marcel.semantic.type.JavaType.Companion.long
import com.tambapps.marcel.semantic.type.JavaType.Companion.longArray
import com.tambapps.marcel.semantic.type.JavaType.Companion.short
import com.tambapps.marcel.semantic.type.JavaType.Companion.shortArray
import com.tambapps.marcel.semantic.type.JavaType.Companion.void
import org.objectweb.asm.Opcodes

val JavaType.internalName: String get() = AsmUtils.getInternalName(type)

// long and double takes 2 slots instead of 1 for other types
val JavaType.takes2Slots get() = this == long || this == double

val JavaArrayType.typeCode get() = when {
  this == intArray -> Opcodes.T_INT
  this == longArray -> Opcodes.T_LONG
  this == floatArray -> Opcodes.T_FLOAT
  this == doubleArray -> Opcodes.T_DOUBLE
  this == booleanArray -> Opcodes.T_BOOLEAN
  this == shortArray -> Opcodes.T_SHORT
  this == byteArray -> Opcodes.T_BYTE
  this == charArray -> Opcodes.T_CHAR
  else -> 0
}
val JavaArrayType.arrayLoadCode get() = when {
  this == intArray -> Opcodes.IALOAD
  this == longArray -> Opcodes.LALOAD
  this == floatArray -> Opcodes.FALOAD
  this == doubleArray -> Opcodes.DALOAD
  this == booleanArray -> Opcodes.BALOAD
  this == shortArray -> Opcodes.SALOAD
  this == byteArray -> Opcodes.BALOAD
  this == charArray -> Opcodes.CALOAD
  else -> Opcodes.AALOAD
}
val JavaArrayType.arrayStoreCode get() = when {
  this == intArray -> Opcodes.IASTORE
  this == longArray -> Opcodes.LASTORE
  this == floatArray -> Opcodes.FASTORE
  this == doubleArray -> Opcodes.DASTORE
  this == booleanArray -> Opcodes.BASTORE
  this == shortArray -> Opcodes.SASTORE
  this == byteArray -> Opcodes.BASTORE
  this == charArray -> Opcodes.CASTORE
  else -> Opcodes.AASTORE
}

val JavaType.descriptor: String get() {
  val type = this.type
  return if (type.isLoaded) AsmUtils.getClassDescriptor(type.realClazz)
  else {
    val descriptor = AsmUtils.getObjectClassDescriptor(type.className)
    if (type.isAnnotation) "@$descriptor" else descriptor
  }
}

// not on JavaTyped because it can be ambiguous with method signature which is different
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
private val PRIMITIVE_CODES_MAP = mapOf(
  Pair(void, PrimitiveAsmCodes(Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN, 0,0,0,0, 0, 0, 0)),
  Pair(int, PrimitiveAsmCodes(Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM, Opcodes.ISHL, Opcodes.ISHR)),
  Pair(long, PrimitiveAsmCodes(Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LRETURN, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV, Opcodes.LREM, Opcodes.LSHL, Opcodes.LSHR)),
  Pair(float, PrimitiveAsmCodes(Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FRETURN, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV, Opcodes.FREM, 0, 0)),
  Pair(double, PrimitiveAsmCodes(Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DRETURN, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV, Opcodes.DREM, 0, 0)),
  // apparently we use int instructions to store booleans
  Pair(boolean, PrimitiveAsmCodes(Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN,  0,0,0,0, 0, 0, 0)),
  Pair(char, PrimitiveAsmCodes(Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM, 0, 0)),
// TODO verify byte and short OpCodes
  Pair(byte, PrimitiveAsmCodes(Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM, 0, 0)),
  Pair(short, PrimitiveAsmCodes(Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM, 0, 0))
)
private val OBJECT_CODES = AsmCodes(Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.ARETURN)

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
  val shlCode: Int, // left shift
  val shrCode: Int, // right shift
) :
  AsmCodes(loadCode, storeCode, returnCode)

val JavaType.loadCode: Int get() = (if (primitive) PRIMITIVE_CODES_MAP.getValue(asPrimitiveType) else OBJECT_CODES).loadCode
val JavaType.storeCode: Int get() = (if (primitive) PRIMITIVE_CODES_MAP.getValue(asPrimitiveType) else OBJECT_CODES).storeCode
val JavaType.returnCode: Int get() = (if (primitive) PRIMITIVE_CODES_MAP.getValue(asPrimitiveType) else OBJECT_CODES).returnCode
val JavaPrimitiveType.addCode: Int get() = PRIMITIVE_CODES_MAP.getValue(asPrimitiveType).addCode
val JavaPrimitiveType.subCode: Int get() = PRIMITIVE_CODES_MAP.getValue(asPrimitiveType).subCode
val JavaPrimitiveType.mulCode: Int get() = PRIMITIVE_CODES_MAP.getValue(asPrimitiveType).mulCode
val JavaPrimitiveType.divCode: Int get() = PRIMITIVE_CODES_MAP.getValue(asPrimitiveType).divCode
val JavaPrimitiveType.modCode: Int get() = PRIMITIVE_CODES_MAP.getValue(asPrimitiveType).modCode
val JavaPrimitiveType.shlCode: Int get() = PRIMITIVE_CODES_MAP.getValue(asPrimitiveType).shlCode
val JavaPrimitiveType.shrCode: Int get() = PRIMITIVE_CODES_MAP.getValue(asPrimitiveType).shrCode
