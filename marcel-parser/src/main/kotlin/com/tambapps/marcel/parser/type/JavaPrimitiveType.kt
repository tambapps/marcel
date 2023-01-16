package com.tambapps.marcel.parser.type

import com.tambapps.marcel.parser.PrimitiveTypes
import org.objectweb.asm.Type
import org.objectweb.asm.Opcodes.*

// TODO merge this with JavaClassType and JavaType

class JavaPrimitiveType(override val className: String,
                             override val internalName: String,
                             override val descriptor: String,
                             override val loadCode: Int,
                             override val storeCode: Int,
                             override val returnCode: Int,
                             val addCode: Int,
                             val subCode: Int,
                             val mulCode: Int,
                             val divCode: Int): JavaType {
  companion object {
    val INT = JavaPrimitiveType(PrimitiveTypes.INT, ILOAD, ISTORE, IRETURN,IADD,ISUB,IMUL,IDIV)
    val LONG = JavaPrimitiveType(PrimitiveTypes.LONG, LLOAD, LSTORE, LRETURN,LADD,LSUB,LMUL,LDIV)
    val FLOAT = JavaPrimitiveType(PrimitiveTypes.FLOAT, FLOAD, FSTORE, FRETURN,FADD,FSUB,FMUL,FDIV)
    val DOUBLE = JavaPrimitiveType(PrimitiveTypes.DOUBLE, DLOAD, DSTORE, DRETURN,DADD,DSUB,DMUL,DDIV)
  }

  constructor(clazz: Class<*>,
              loadCode: Int,
              storeCode: Int,
              retCode: Int,
              addCode: Int,
              subCode: Int,
              mulCode: Int,
              divCode: Int): this(clazz.name, Type.getInternalName(clazz),
    Type.getDescriptor(clazz), loadCode, storeCode, retCode, addCode, subCode, mulCode, divCode)

  override fun toString(): String {
    return className
  }

  }