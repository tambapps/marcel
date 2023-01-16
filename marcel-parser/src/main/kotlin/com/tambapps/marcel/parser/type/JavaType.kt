package com.tambapps.marcel.parser.type

import com.tambapps.marcel.parser.PrimitiveTypes
import org.objectweb.asm.Opcodes

// TODO add an imple for dynamic types e.g. class defined in a marcel script
interface JavaType {

  companion object {
    val VOID = JavaPrimitiveType(PrimitiveTypes.VOID, Opcodes.ALOAD, Opcodes.ASTORE, Opcodes.RETURN, 0,0,0,0)

  }
  val className: String
  val internalName: String
  val descriptor: String
  val storeCode: Int
  val loadCode: Int
  val returnCode: Int

}