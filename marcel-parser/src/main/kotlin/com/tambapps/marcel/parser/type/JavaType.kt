package com.tambapps.marcel.parser.type

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.PrimitiveTypes
import org.objectweb.asm.Opcodes

// TODO add an imple for dynamic types e.g. class defined in a marcel script
interface JavaType {

  companion object {

    val VOID = JavaPrimitiveType(PrimitiveTypes.VOID, Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN, 0,0,0,0)
    val INT = JavaPrimitiveType(PrimitiveTypes.INT, Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.IRETURN, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV)
    val LONG = JavaPrimitiveType(PrimitiveTypes.LONG, Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.LRETURN, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV)
    val FLOAT = JavaPrimitiveType(PrimitiveTypes.FLOAT, Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.FRETURN, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV)
    val DOUBLE = JavaPrimitiveType(PrimitiveTypes.DOUBLE, Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.DRETURN, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV)

    // TODO might be useless
    val TOKEN_TYPE_MAP = mapOf(
        Pair(TokenType.TYPE_INT, INT),
        Pair(TokenType.TYPE_LONG, LONG),
        Pair(TokenType.TYPE_VOID, VOID),
        Pair(TokenType.TYPE_FLOAT, FLOAT),
        Pair(TokenType.TYPE_DOUBLE, DOUBLE),
    )
  }

  val className: String
  val internalName: String
  val descriptor: String
  val storeCode: Int
  val loadCode: Int
  val returnCode: Int

}