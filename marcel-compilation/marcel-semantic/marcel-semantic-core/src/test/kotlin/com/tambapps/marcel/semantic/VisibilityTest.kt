package com.tambapps.marcel.semantic

import com.tambapps.marcel.semantic.type.JavaType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VisibilityTest {

  @Test
  fun testPublicVisibility() {
    assertTrue(Visibility.PUBLIC.canAccess(JavaType.Object, JavaType.Object))
    assertTrue(Visibility.PUBLIC.canAccess(JavaType.Object, JavaType.String))
    assertTrue(Visibility.PUBLIC.canAccess(JavaType.String, JavaType.Object))
    assertTrue(Visibility.PUBLIC.canAccess(JavaType.Object, JavaType.DynamicObject))
    assertTrue(Visibility.PUBLIC.canAccess(JavaType.DynamicObject, JavaType.Object))
  }

  @Test
  fun testProtectedVisibility() {
    assertTrue(Visibility.PROTECTED.canAccess(JavaType.Object, JavaType.Object))
    assertTrue(Visibility.PROTECTED.canAccess(JavaType.Object, JavaType.String))
    assertTrue(Visibility.PROTECTED.canAccess(JavaType.String, JavaType.Object))
    assertTrue(Visibility.PROTECTED.canAccess(JavaType.DynamicObject, JavaType.Object))
    assertFalse(Visibility.PROTECTED.canAccess(JavaType.Object, JavaType.DynamicObject))
  }

  @Test
  fun testInternalVisibility() {
    assertTrue(Visibility.INTERNAL.canAccess(JavaType.Object, JavaType.Object))
    assertTrue(Visibility.INTERNAL.canAccess(JavaType.Object, JavaType.String))
    assertTrue(Visibility.INTERNAL.canAccess(JavaType.String, JavaType.Object))
    assertFalse(Visibility.INTERNAL.canAccess(JavaType.DynamicObject, JavaType.Object))
    assertFalse(Visibility.INTERNAL.canAccess(JavaType.Object, JavaType.DynamicObject))
  }

  @Test
  fun testPrivateVisibility() {
    assertTrue(Visibility.PRIVATE.canAccess(JavaType.Object, JavaType.Object))
    assertFalse(Visibility.PRIVATE.canAccess(JavaType.Object, JavaType.String))
    assertFalse(Visibility.PRIVATE.canAccess(JavaType.String, JavaType.Object))
    assertFalse(Visibility.PRIVATE.canAccess(JavaType.DynamicObject, JavaType.Object))
    assertFalse(Visibility.PRIVATE.canAccess(JavaType.Object, JavaType.DynamicObject))
  }

}