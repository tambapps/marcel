package com.tambapps.marcel.compiler

import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader
import java.nio.file.Files
import kotlin.math.absoluteValue

abstract class AbstractCompilerTest {

  protected val compiler = MarcelCompiler(CompilerConfiguration())

  protected fun eval(resourceName: String): Any? {
    val className = "Test" + resourceName.hashCode().absoluteValue
    val text = javaClass.getResourceAsStream(resourceName)!!.reader().use {
      it.readText()
    }
    return evalSource(className, text)
  }

  protected fun getResourceText(resourceName: String) = javaClass.getResourceAsStream(resourceName)!!.reader().use {
    it.readText()
  }


  protected fun evalSource(className: String, text: String): Any? {
    val jarFile = writeJar(className, text)

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

  protected fun writeJar(className: String, text: String): File {
    return writeJar(compiler.compile(text = text, fileName = className), className)
  }
  protected fun writeJar(result: List<CompiledClass>, className: String): File {

    val jarFile = Files.createTempFile("", "$className.jar").toFile()
    JarWriter(jarFile).use {
      it.writeClasses(result)
    }
    return jarFile
  }
}