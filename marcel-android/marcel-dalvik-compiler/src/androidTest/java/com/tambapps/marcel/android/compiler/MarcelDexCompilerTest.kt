package com.tambapps.marcel.android.compiler

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tambapps.marcel.compiler.file.SourceFile
import marcel.lang.MarcelDexClassLoader

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MarcelDexCompilerTest {
  private val dir
    get() = InstrumentationRegistry.getInstrumentation().targetContext
      .getDir("classes", Context.MODE_PRIVATE)
  private val classLoader =
    MarcelDexClassLoader()
  private val compiler = MarcelDexCompiler(classLoader)

  @Test
  fun testCompileToDexJar() {
    val dexJarFile = File(dir, "Test.jar")
    compiler.compileToDexJar(SourceFile.from("TestDexJar.mcl", "1 + 1"), dexJarFile)

    val script = classLoader.loadScript("TestDexJar", dexJarFile)
    assertEquals(2, script.run())
  }
}