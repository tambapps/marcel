package com.tambapps.marcel.compiler.extensions

import com.tambapps.marcel.compiler.util.AsmUtils
import com.tambapps.marcel.semantic.method.JavaMethod
import org.objectweb.asm.Opcodes


val JavaMethod.descriptor: String
  get() = AsmUtils.getMethodDescriptor(parameters, returnType)

val JavaMethod.invokeCode: Int
  get() = if (isStatic) Opcodes.INVOKESTATIC
  else if (ownerClass.isInterface) Opcodes.INVOKEINTERFACE
  else Opcodes.INVOKEVIRTUAL