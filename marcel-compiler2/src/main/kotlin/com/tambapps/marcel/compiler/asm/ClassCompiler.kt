package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.CompiledClass
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.semantic.ast.ClassNode

class ClassCompiler(private val compilerConfiguration: CompilerConfiguration,
                    private val typeResolver: JavaTypeResolver
) {

  fun compileDefinedClasses(classNode: Collection<ClassNode>): List<CompiledClass> {
    TODO()
  }
}