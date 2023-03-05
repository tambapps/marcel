package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.ast.ModuleNode

class CompilationResult(val ast: ModuleNode, val classes: List<CompiledClass>)

class CompiledClass constructor(val className: String, val bytes: ByteArray)