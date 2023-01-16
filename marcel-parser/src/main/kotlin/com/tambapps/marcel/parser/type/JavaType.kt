package com.tambapps.marcel.parser.type

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.PrimitiveTypes
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

// TODO add an imple for dynamic types e.g. class defined in a marcel script
open class JavaType(
    val className: String,
    val internalName: String,
    val descriptor: String,
    val storeCode: Int,
    val loadCode: Int,
    val returnCode: Int) {

  constructor(clazz: Class<*>): this(clazz.name, Type.getInternalName(clazz), Type.getDescriptor(clazz),
  Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ARETURN)
  companion object {

    val OBJECT = JavaType(Object::class.java)


    val void = JavaPrimitiveType(PrimitiveTypes.VOID, Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN, 0,0,0,0)
    val int = JavaPrimitiveType(PrimitiveTypes.INT, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)
    val long = JavaPrimitiveType(PrimitiveTypes.LONG, Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LRETURN, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV)
    val float = JavaPrimitiveType(PrimitiveTypes.FLOAT, Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FRETURN, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV)
    val double = JavaPrimitiveType(PrimitiveTypes.DOUBLE, Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DRETURN, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV)

    val TOKEN_TYPE_MAP = mapOf(
        Pair(TokenType.TYPE_INT, int),
        Pair(TokenType.TYPE_LONG, long),
        Pair(TokenType.TYPE_VOID, void),
        Pair(TokenType.TYPE_FLOAT, float),
        Pair(TokenType.TYPE_DOUBLE, double),
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JavaType) return false
    return className == other.className
  }

  override fun hashCode(): Int {
    return className.hashCode()
  }

  override fun toString(): String {
    return className
  }

}

class JavaPrimitiveType(className: String,
                        internalName: String,
                        descriptor: String,
                        loadCode: Int,
                        storeCode: Int,
                        returnCode: Int,
                        val addCode: Int,
                        val subCode: Int,
                        val mulCode: Int,
                        val divCode: Int): JavaType(className, internalName, descriptor, storeCode, loadCode, returnCode) {

  constructor(clazz: Class<*>,
              loadCode: Int,
              storeCode: Int,
              retCode: Int,
              addCode: Int,
              subCode: Int,
              mulCode: Int,
              divCode: Int): this(clazz.name, Type.getInternalName(clazz),
      Type.getDescriptor(clazz), loadCode, storeCode, retCode, addCode, subCode, mulCode, divCode)

}