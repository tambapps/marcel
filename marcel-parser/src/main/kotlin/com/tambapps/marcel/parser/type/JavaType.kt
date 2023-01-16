package com.tambapps.marcel.parser.type

import com.tambapps.marcel.parser.PrimitiveTypes
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class JavaType(
  val className: String,
  val internalName: String,
  val descriptor: String,
  val storeCode: Int,
  val loadCode: Int,
  val returnCode: Int) {

  // constructor for non-primitive classes
  constructor(clazz: Class<*>): this(clazz.name, Type.getInternalName(clazz), Type.getDescriptor(clazz), Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ARETURN)

  // constructor for primitive types
  constructor(clazz: Class<*>,
              loadCode: Int,
              storeCode: Int,
              returnCode: Int): this(clazz.name, Type.getInternalName(clazz), Type.getDescriptor(clazz), storeCode, loadCode, returnCode)

  companion object {

    val OBJECT = JavaType(Any::class.java)

    val int = JavaType(PrimitiveTypes.INT, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.IRETURN)
    val long = JavaType(PrimitiveTypes.LONG, Opcodes.LSTORE, Opcodes.LLOAD, Opcodes.LRETURN)
    val float = JavaType(PrimitiveTypes.FLOAT, Opcodes.FSTORE, Opcodes.FLOAD, Opcodes.FRETURN)
    val double = JavaType(PrimitiveTypes.DOUBLE, Opcodes.DSTORE, Opcodes.DLOAD, Opcodes.DRETURN)
    val void = JavaType(PrimitiveTypes.VOID, Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.RETURN)

    val ADD_OPERATOR = mapOf(
      Pair(int, Opcodes.IADD),
      Pair(long, Opcodes.LADD),
      Pair(float, Opcodes.FADD),
      Pair(double, Opcodes.DADD),
    )
    val SUB_OPERATOR = mapOf(
      Pair(int, Opcodes.ISUB),
      Pair(long, Opcodes.LSUB),
      Pair(float, Opcodes.FSUB),
      Pair(double, Opcodes.DSUB),
    )

    val MUL_OPERATOR = mapOf(
      Pair(int, Opcodes.IMUL),
      Pair(long, Opcodes.LMUL),
      Pair(float, Opcodes.FMUL),
      Pair(double, Opcodes.DMUL),
    )

    val DIV_OPERATOR = mapOf(
      Pair(int, Opcodes.IDIV),
      Pair(long, Opcodes.LDIV),
      Pair(float, Opcodes.FDIV),
      Pair(double, Opcodes.DDIV),
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as JavaType

    if (className != other.className) return false

    return true
  }

  override fun hashCode(): Int {
    return className.hashCode()
  }

  override fun toString(): String {
    return className
  }
}