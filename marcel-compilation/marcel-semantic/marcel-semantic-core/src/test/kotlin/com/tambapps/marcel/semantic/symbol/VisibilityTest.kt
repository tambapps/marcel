package com.tambapps.marcel.semantic.symbol

import com.tambapps.marcel.semantic.symbol.type.JavaType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class VisibilityTest {

  @Test
  fun testPublicVisibility() {
    Assertions.assertTrue(Visibility.PUBLIC.canAccess(JavaType.Companion.Object, JavaType.Companion.Object))
    Assertions.assertTrue(Visibility.PUBLIC.canAccess(JavaType.Companion.Object, JavaType.Companion.String))
    Assertions.assertTrue(Visibility.PUBLIC.canAccess(JavaType.Companion.String, JavaType.Companion.Object))
    Assertions.assertTrue(Visibility.PUBLIC.canAccess(JavaType.Companion.Object, JavaType.Companion.DynamicObject))
    Assertions.assertTrue(Visibility.PUBLIC.canAccess(JavaType.Companion.DynamicObject, JavaType.Companion.Object))
  }

  @Test
  fun testProtectedVisibility() {
    Assertions.assertTrue(Visibility.PROTECTED.canAccess(JavaType.Companion.Object, JavaType.Companion.Object))
    Assertions.assertTrue(Visibility.PROTECTED.canAccess(JavaType.Companion.Object, JavaType.Companion.String))
    Assertions.assertTrue(Visibility.PROTECTED.canAccess(JavaType.Companion.String, JavaType.Companion.Object))
    Assertions.assertTrue(Visibility.PROTECTED.canAccess(JavaType.Companion.DynamicObject, JavaType.Companion.Object))
    Assertions.assertFalse(Visibility.PROTECTED.canAccess(JavaType.Companion.Object, JavaType.Companion.DynamicObject))
  }

  @Test
  fun testInternalVisibility() {
    Assertions.assertTrue(Visibility.INTERNAL.canAccess(JavaType.Companion.Object, JavaType.Companion.Object))
    Assertions.assertTrue(Visibility.INTERNAL.canAccess(JavaType.Companion.Object, JavaType.Companion.String))
    Assertions.assertTrue(Visibility.INTERNAL.canAccess(JavaType.Companion.String, JavaType.Companion.Object))
    Assertions.assertFalse(Visibility.INTERNAL.canAccess(JavaType.Companion.DynamicObject, JavaType.Companion.Object))
    Assertions.assertFalse(Visibility.INTERNAL.canAccess(JavaType.Companion.Object, JavaType.Companion.DynamicObject))
  }

  @Test
  fun testPrivateVisibility() {
    Assertions.assertTrue(Visibility.PRIVATE.canAccess(JavaType.Companion.Object, JavaType.Companion.Object))
    Assertions.assertFalse(Visibility.PRIVATE.canAccess(JavaType.Companion.Object, JavaType.Companion.String))
    Assertions.assertFalse(Visibility.PRIVATE.canAccess(JavaType.Companion.String, JavaType.Companion.Object))
    Assertions.assertFalse(Visibility.PRIVATE.canAccess(JavaType.Companion.DynamicObject, JavaType.Companion.Object))
    Assertions.assertFalse(Visibility.PRIVATE.canAccess(JavaType.Companion.Object, JavaType.Companion.DynamicObject))
  }

}