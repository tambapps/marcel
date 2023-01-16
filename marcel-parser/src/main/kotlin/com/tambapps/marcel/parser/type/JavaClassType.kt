package com.tambapps.marcel.parser.type

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class JavaClassType(clazz: Class<*>): JavaType(clazz.name, Type.getInternalName(clazz), Type.getDescriptor(clazz),
    Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.ARETURN) {

}