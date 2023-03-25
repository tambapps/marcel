package com.tambapps.marcel.dalvik.compiler

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tambapps.marcel.dalvik.compiler.AndroidMarcelCompiler
import com.tambapps.marcel.compiler.SourceFile
import com.tambapps.marcel.dalvik.compiler.MarcelDexClassLoader
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Test of evaluating and compiling marcel scripts
 */
@RunWith(AndroidJUnit4::class)
class MarcelEvalTest {
  private val dir
    get() = InstrumentationRegistry.getInstrumentation().targetContext
      .getDir("classes", Context.MODE_PRIVATE)
  private val classLoader =
    MarcelDexClassLoader()
  private val compiler = AndroidMarcelCompiler(classLoader)


  @Test
  fun testSimpleEvalDex() {
    val dexFile = File(dir, "Test.dex")
    compiler.compileToDex(SourceFile("Test.mcl") { "1" }, dexFile)

    val script = classLoader.loadScript("Test", dexFile)
    assertEquals(1, script.run())
  }

  @Test
  fun testSimpleEvalDexJar() {
    val dexJarFile = File(dir, "Test.jar")
    compiler.compileToDexJar(SourceFile("Test.mcl") { "1" }, dexJarFile)

    val script = classLoader.loadScript("Test", dexJarFile)
    assertEquals(1, script.run())
  }

}