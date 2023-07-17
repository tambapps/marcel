package com.tambapps.marcel.compiler.check

import com.tambapps.marcel.compiler.ClassNodeVisitor

object MarcelCompilerChecks {

  val ALL = listOf<ClassNodeVisitor>(ImplementedInterfaceCheck(), ConflictingMethodCheck(), ExtendingClassCheck())

}