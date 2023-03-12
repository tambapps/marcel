package com.tambapps.marcel.android.app

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
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

        val compiler = MarcelCompiler()
        val classLoader = MarcelDexClassLoader()
        val dir = context.getDir("classes", Context.MODE_PRIVATE)

        val compiledClasses = compiler.compile(classLoader, listOf(SourceFile("Test.mcl") { "1" }))
        val translator = DexBytecodeTranslator()

        compiledClasses.forEach { translator.addClass(it.className, it.bytes) }

        val dexJarFile = File(dir, "Test.jar")

        DexJarWriter(dexJarFile.outputStream()).use {
            it.write(compiledClasses.map { it.className }, translator.dexBytes)
        }


        val script = classLoader.loadScript("Test", dexJarFile)
        assertEquals(1, script.run())
    }

}