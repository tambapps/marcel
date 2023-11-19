package com.tambapps.marcel.parser.owner

import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Opcodes
import java.lang.UnsupportedOperationException


interface Owner {
  val invokeCode: Int
  val classInternalName: String
}

class StaticOwner(override val classInternalName: String): Owner {

  constructor(javaType: JavaType): this(javaType.internalName)

  override val invokeCode = Opcodes.INVOKESTATIC
}

class NoOpOwner: Owner {
  override val invokeCode: Int
    get() = throw UnsupportedOperationException("NoOpOwner have no invokeCode")
  override val classInternalName: String
    get() = throw UnsupportedOperationException("NoOpOwner have no invokeCode")
}