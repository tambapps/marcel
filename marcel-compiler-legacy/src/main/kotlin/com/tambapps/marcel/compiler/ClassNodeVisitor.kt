package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.ast.ClassNode

interface ClassNodeVisitor {

    fun visit(classNode: ClassNode, typeResolver: JavaTypeResolver)

}