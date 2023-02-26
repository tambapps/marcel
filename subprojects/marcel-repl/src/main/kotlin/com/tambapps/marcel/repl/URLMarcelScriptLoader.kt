package com.tambapps.marcel.repl

import marcel.lang.Binding
import marcel.lang.Script
import java.io.File
import java.net.URLClassLoader
import kotlin.jvm.Throws

class URLMarcelScriptLoader(
  private val parentLoader: ClassLoader
): MarcelScriptLoader() {

  constructor(): this(URLMarcelScriptLoader::class.java.classLoader)

  @Throws(ClassNotFoundException::class)
  override fun loadScript(className: String, jarFile: File, binding: Binding?): Script {
    if (!jarFile.isFile) throw IllegalArgumentException("File $jarFile is not a regular file")

    // load the jar into the classpath
    val classLoader = URLClassLoader(getJarUrls(jarFile), parentLoader)
    // and then load the class
    val clazz = classLoader.loadClass(className)
    if (!Script::class.java.isAssignableFrom(clazz)) {
      throw IllegalArgumentException("The loaded class is not a script")
    }
    return if (binding == null) clazz.getDeclaredConstructor().newInstance() as Script
    else clazz.getDeclaredConstructor(Binding::class.java).newInstance(binding) as Script
  }

}