package com.tambapps.marcel.cl

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.MarcelJarOutputStream
import com.tambapps.marcel.compiler.MarcelCompiler
import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader
import java.nio.file.Files
import kotlin.math.absoluteValue

abstract class AbstractCompilerTest {

  protected val compiler = MarcelCompiler(CompilerConfiguration())

  protected fun eval(resourceName: String): Any? {
    val className = "Test" + resourceName.hashCode().absoluteValue
    val text = javaClass.getResourceAsStream(resourceName).reader().use {
      it.readText()
    }
    return evalSource(className, text)
  }

  protected fun evalSource(className: String, text: String): Any? {
    val result = compiler.compile(text = text, fileName = className)

    val jarFile = Files.createTempFile("", "$className.jar").toFile()
    MarcelJarOutputStream(jarFile).use {
      it.writeClasses(result)
    }

    val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), MarcelCompiler::class.java.classLoader)
    val clazz = classLoader.loadClass(className)
    return try {
      clazz.getMethod("run", Array<String>::class.java)
        .invoke(clazz.getDeclaredConstructor().newInstance(), arrayOf<String>())
    } catch (e: InvocationTargetException) {
      throw e.targetException
    } finally {
        jarFile.delete()
    }
  }
}