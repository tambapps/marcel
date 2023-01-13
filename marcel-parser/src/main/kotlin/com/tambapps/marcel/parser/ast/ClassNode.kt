package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.type.JavaType

class ClassNode(val access: Int, val name: String, val parentType: JavaType, val methods: MutableList<MethodNode>) {

}