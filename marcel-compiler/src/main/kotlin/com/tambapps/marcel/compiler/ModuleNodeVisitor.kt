package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.ast.ModuleNode

interface ModuleNodeVisitor {

    fun visit(moduleNode: ModuleNode, typeResolver: JavaTypeResolver)

}