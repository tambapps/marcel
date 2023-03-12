package com.tambapps.marcel.android.app

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tambapps.marcel.android.app.marcel.compiler.AndroidMarcelCompiler
import com.tambapps.marcel.compiler.MarcelCompiler
import com.tambapps.marcel.compiler.SourceFile
import marcel.lang.android.dex.DexBytecodeTranslator
import marcel.lang.android.dex.DexJarWriter
import marcel.lang.android.dex.MarcelDexClassLoader

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File

/**
 * Test of evaluating and compiling marcel scripts
 */
@RunWith(AndroidJUnit4::class)
class MarcelEvalTest {
    @Test
    fun testSimpleEval() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = context.getDir("classes", Context.MODE_PRIVATE)
        val classLoader = MarcelDexClassLoader()
        val compiler = AndroidMarcelCompiler(classLoader)

        val dexJarFile = File(dir, "Test.jar")

        compiler.compile(SourceFile("Test.mcl") { "1" }, dexJarFile)

        val script = classLoader.loadScript("Test", dexJarFile)
        assertEquals(1, script.run())
    }

}