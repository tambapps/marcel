package com.tambapps.marcel.compiler

class CompilationResult(val classes: List<CompiledClass>)

class CompiledClass(val className: String, val bytes: ByteArray)