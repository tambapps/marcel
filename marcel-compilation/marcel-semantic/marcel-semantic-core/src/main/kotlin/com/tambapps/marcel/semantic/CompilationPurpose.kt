package com.tambapps.marcel.semantic

/**
 * Enum representing each purpose of the compilation. This class is in the semantic module rather than the compilation
 * module because it allows to customize the semantic analysis (especially the node transformers)
 */
enum class CompilationPurpose {
    COMPILATION,
    REPL
  }