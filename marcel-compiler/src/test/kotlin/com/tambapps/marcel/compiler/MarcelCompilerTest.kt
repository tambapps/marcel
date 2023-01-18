package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.type.JavaType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.net.URLClassLoader
import java.nio.file.Files

class MarcelCompilerTest {

    private val compiler = MarcelCompiler()
    @Test
    fun testScript() {
        val eval = eval("/test1.marcel")
        assertNotNull(eval)
        assertEquals(JavaType.OBJECT.realClassOrObject, eval.javaClass)
    }


    private fun eval(resourceName: String): Any {
        val result = javaClass.getResourceAsStream(resourceName).reader().use {
            compiler.compile(it, "Test")
        }

        val jarFile = Files.createTempFile("", "${result.className}.jar").toFile()
        JarWriter().writeScriptJar(result.className, result.bytes, jarFile)

        val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), MarcelCompiler::class.java.classLoader)
        val clazz = classLoader.loadClass(result.className)
        return clazz.getMethod("run", Array<String>::class.java).invoke(clazz.getDeclaredConstructor().newInstance(), arrayOf<String>())
    }
}