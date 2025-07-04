package com.tambapps.marcel.semantic.analysis

import com.tambapps.marcel.semantic.analysis.TestUtils.applySemantic
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.symbol.type.NullSafetyMode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.string.shouldContain

class MarcelSemanticAnalysisNullnessErrorTest : AnnotationSpec() {

  @Test
  fun `non nullable variable`() {
    val exception = shouldThrow<MarcelSemanticException> {
      applySemantic("""
        Integer a = null
      """.trimIndent(), NullSafetyMode.DEFAULT)
    }
    exception.message shouldContain "Cannot assign nullable value to non null"
  }

  @Test
  fun `non nullable variable initialized with nullable method`() {
    val exception = shouldThrow<MarcelSemanticException> {
      applySemantic("""
        Integer a = foo(1)
        fun Integer? foo(Integer a) -> a = 1
      """.trimIndent(), NullSafetyMode.DEFAULT)
    }
    exception.message shouldContain "Cannot assign nullable value to non null"
  }


  @Test
  fun `non nullable field`() {
    val exception = shouldThrow<MarcelSemanticException> {
      applySemantic("""
        private Integer a = 1
        a = null
      """.trimIndent(), NullSafetyMode.DEFAULT)
    }
    exception.message shouldContain "Cannot assign nullable value to non null"
  }

  @Test
  fun `non nullable method parameter`() {
    val exception = shouldThrow<MarcelSemanticException> {
      applySemantic("""
        fun int foo(Integer a) -> a = 1
        Integer? b = null
        foo(b)
      """.trimIndent(), NullSafetyMode.DEFAULT)
    }
    exception.message shouldContain "Cannot pass nullable value to non null"
  }

  @Test
  fun `non nullable method return`() {
    val exception = shouldThrow<MarcelSemanticException> {
      applySemantic("""
        fun Integer foo(Integer a) -> null
      """.trimIndent(), NullSafetyMode.DEFAULT)
    }
    exception.message shouldContain "Cannot return nullable value on non-nullable function"
  }

  @Test
  fun `non nullable method parameter default value null`() {
    val exception = shouldThrow<MarcelSemanticException> {
      applySemantic("""
        fun Integer foo(Integer a = null) -> a
      """.trimIndent(), NullSafetyMode.DEFAULT)
    }
    exception.message shouldContain "Cannot have null default value for"
  }

  @Test
  fun `non nullable method parameter default value`() {
    val exception = shouldThrow<MarcelSemanticException> {
      applySemantic("""
        static fun Integer? bar() -> null 
        fun Integer foo(Integer a = bar()) -> a
      """.trimIndent(), NullSafetyMode.DEFAULT)
    }
    exception.message shouldContain "Cannot have a nullable default value for a non-nullable parameter"
  }

}