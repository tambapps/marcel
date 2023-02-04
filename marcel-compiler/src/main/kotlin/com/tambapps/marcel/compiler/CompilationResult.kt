package com.tambapps.marcel.compiler

class CompilationResult(val classes: List<CompiledClass>)

class CompiledClass(val simpleClassName: String, val bytes: ByteArray)