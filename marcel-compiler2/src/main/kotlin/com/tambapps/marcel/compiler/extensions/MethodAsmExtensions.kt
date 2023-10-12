package com.tambapps.marcel.compiler.extensions

import com.tambapps.marcel.compiler.util.AsmUtils
import com.tambapps.marcel.semantic.method.JavaMethod
import org.objectweb.asm.Opcodes


val JavaMethod.descriptor: String
  get() = AsmUtils.getMethodDescriptor(parameters, returnType)

val JavaMethod.signature: String
  get() {
    val builder = StringBuilder()
    // using rawType because these are the one used in compiled classes
    parameters.joinTo(buffer = builder, separator = "", transform = { it.rawType.descriptor }, prefix = "(", postfix = ")")
    builder.append(returnType.descriptor)
    return builder.toString()
  }

val JavaMethod.invokeCode: Int
  get() = if (isStatic) Opcodes.INVOKESTATIC
  else if (ownerClass.isInterface) Opcodes.INVOKEINTERFACE
  else Opcodes.INVOKEVIRTUAL