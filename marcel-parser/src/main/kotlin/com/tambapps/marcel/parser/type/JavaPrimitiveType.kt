package com.tambapps.marcel.parser.type

import com.tambapps.marcel.parser.PrimitiveTypes
import org.objectweb.asm.Type
import org.objectweb.asm.Opcodes.*


enum class JavaPrimitiveType(override val className: String,
                             override val internalName: String,
                             override val descriptor: String,
                             val loadCode: Int,
                             val storeCode: Int,
                             val retCode: Int,
                             val addCode: Int,
                             val subCode: Int,
                             val mulCode: Int,
                             val divCode: Int): JavaType {
  INT(PrimitiveTypes.INT, ILOAD, ISTORE, IRETURN,IADD,ISUB,IMUL,IDIV),
  LONG(PrimitiveTypes.LONG, LLOAD, LSTORE, LRETURN,LADD,LSUB,LMUL,LDIV),
  FLOAT(PrimitiveTypes.FLOAT, FLOAD, FSTORE, FRETURN,FADD,FSUB,FMUL,FDIV),
  DOUBLE(PrimitiveTypes.DOUBLE, DLOAD, DSTORE, DRETURN,DADD,DSUB,DMUL,DDIV),
  VOID(PrimitiveTypes.VOID, ALOAD, ASTORE, RETURN, 0,0,0,0);

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