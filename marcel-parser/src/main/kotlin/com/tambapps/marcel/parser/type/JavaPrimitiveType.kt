package com.tambapps.marcel.parser.type

import com.tambapps.marcel.parser.Types
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
  INT(Types.INT, ILOAD, ISTORE, IRETURN,IADD,ISUB,IMUL,IDIV),
  LONG(Types.LONG, LLOAD, LSTORE, LRETURN,LADD,LSUB,LMUL,LDIV),
  FLOAT(Types.FLOAT, FLOAD, FSTORE, FRETURN,FADD,FSUB,FMUL,FDIV),
  DOUBLE(Types.DOUBLE, DLOAD, DSTORE, DRETURN,DADD,DSUB,DMUL,DDIV),
  VOID(Types.VOID_P, ALOAD, ASTORE, RETURN, 0,0,0,0);

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