package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.cst.imprt.SimpleImportCstNode
import com.tambapps.marcel.parser.cst.imprt.StaticImportCstNode
import com.tambapps.marcel.parser.cst.imprt.WildcardImportCstNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MarcelImportParserTest {

  @Test
  fun testSimpleImport() {
    val import = TestUtils.parser("import foo.bar.Baz;").import()
    assertTrue(import is SimpleImportCstNode)
    import as SimpleImportCstNode

    assertEquals("foo.bar.Baz", import.className)
    assertNull(import.asName)
  }

  @Test
  fun testSimpleImportAs() {
    val import = TestUtils.parser("import foo.bar.Baz as Gna").import()
    assertTrue(import is SimpleImportCstNode)
    import as SimpleImportCstNode

    assertEquals("foo.bar.Baz", import.className)
    assertEquals("Gna", import.asName)
  }

  @Test
  fun testStaticImport() {
    val import = TestUtils.parser("import static foo.bar.Baz.inga").import()
    assertTrue(import is StaticImportCstNode)
    import as StaticImportCstNode

    assertEquals("foo.bar.Baz", import.className)
    assertEquals("inga", import.memberName)
  }

  @Test
  fun testWildcardImport() {
    val import = TestUtils.parser("import foo.bar.Baz.*").import()
    assertTrue(import is WildcardImportCstNode)
    import as WildcardImportCstNode

    assertEquals("foo.bar.Baz", import.prefix)
  }

}