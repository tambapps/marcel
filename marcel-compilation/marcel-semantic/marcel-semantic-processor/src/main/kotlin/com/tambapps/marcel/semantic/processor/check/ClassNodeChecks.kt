package com.tambapps.marcel.semantic.processor.check

object ClassNodeChecks {

  // TODO these should probably be called by the semantic, in an endChecks method, rather than in the compiler
  val ALL = listOf(ExtendingClassCheck, RecursiveConstructorCheck, ImplementedInterfaceCheck)

}