package com.tambapps.marcel.semantic.visitor

import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.type.JavaTypeResolver

interface ClassNodeVisitor {

    fun visit(classNode: ClassNode, typeResolver: JavaTypeResolver)

}