package com.tambapps.marcel.semantic.processor.check

/**
 * Object regrouping all class node post semantic checks
 *
 */
object ClassNodePostSemanticChecks {

  val ALL get() = listOf(ExtendingClassCheck(), RecursiveConstructorCheck(), ImplementedInterfaceCheck())

}