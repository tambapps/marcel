package com.tambapps.marcel.parser.ast

import org.objectweb.asm.Type

class ClassNode(val name: String, val parentClassInternalName: String, val methods: MutableList<MethodNode>) {

  constructor(name: String, clazz: Class<*>, methods: MutableList<MethodNode>): this(name, Type.getInternalName(clazz), methods)
  constructor(name: String, methods: MutableList<MethodNode>): this(name, Object::class.java, methods)

}