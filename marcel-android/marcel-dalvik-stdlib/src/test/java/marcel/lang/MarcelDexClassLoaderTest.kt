package marcel.lang

import org.junit.Test

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MarcelDexClassLoaderTest {
  @Test
  fun useAppContext() {
    val classLoader = MarcelDexClassLoader()
    assertNotNull(classLoader)
  }
}