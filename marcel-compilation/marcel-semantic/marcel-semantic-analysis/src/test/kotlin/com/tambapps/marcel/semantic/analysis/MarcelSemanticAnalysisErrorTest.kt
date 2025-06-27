package com.tambapps.marcel.semantic.analysis

import com.tambapps.marcel.semantic.analysis.TestUtils.applySemantic
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.string.shouldContain

class MarcelSemanticAnalysisErrorTest : AnnotationSpec() {

  @Test
  fun variableNotDefinedOnDeclaration() {
    val exception = shouldThrow<MarcelSemanticException> {
      applySemantic("""
        List<int> l = [1, 2, 3]
        int i = l.find { i == 2 }
      """.trimIndent())
    }
    exception.message shouldContain "Variable i is not defined"
  }
}