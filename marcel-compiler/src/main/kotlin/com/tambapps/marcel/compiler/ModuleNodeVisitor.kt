package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.ast.ModuleNode

/**
 * Class allowing to process annotations. It is run between the parsing phase and the compiling phase
 */
interface ModuleNodeVisitor {

    fun visit(moduleNode: ModuleNode)

}