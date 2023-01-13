package com.tambapps.marcel.parser.ast

import org.objectweb.asm.Type

class ClassNode(val access: Int, val name: String, val parentClassInternalName: String, val methods: MutableList<MethodNode>) {

  constructor(access: Int, name: String, clazz: Class<*>, methods: MutableList<MethodNode>): this(access, name, Type.getInternalName(clazz), methods)
  constructor(access: Int, name: String, methods: MutableList<MethodNode>): this(access, name, Object::class.java, methods)

}