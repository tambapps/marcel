package com.tambapps.marcel.compiler

import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader
import java.nio.file.Files
import kotlin.math.absoluteValue

abstract class AbstractCompilerTest {

  protected val compiler = MarcelCompiler()

  protected fun eval(resourceName: String): Any? {
    val className = "Test" + resourceName.hashCode().absoluteValue
    val text = javaClass.getResourceAsStream(resourceName).reader().use {
      it.readText()
    }
    return evalSource(className, text)
  }

  protected fun evalSource(className: String, text: String): Any? {
    val result = compiler.compile(text = text, className = className)

    val jarFile = Files.createTempFile("", "$className.jar").toFile()
    JarWriter(jarFile).use {
      it.writeClass(result)
    }

    val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), MarcelCompiler::class.java.classLoader)
    val clazz = classLoader.loadClass(className)
    return try {
      clazz.getMethod("run", Array<String>::class.java)
        .invoke(clazz.getDeclaredConstructor().newInstance(), arrayOf<String>())
    } catch (e: InvocationTargetException) {
      throw e.targetException
    }
  }
}