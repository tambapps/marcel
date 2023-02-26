package com.tambapps.marcel.repl

import marcel.lang.Binding
import marcel.lang.Script
import java.io.File
import java.net.URL

abstract class MarcelScriptLoader {

  private val libraryJars = mutableSetOf<File>()

  abstract fun loadScript(className: String, jarFile: File, binding: Binding? = null): Script

  fun addLibraryJar(jarFile: File): Boolean {
    return libraryJars.add(jarFile)
  }

  fun removeLibraryJar(jarFile: File): Boolean {
    return libraryJars.remove(jarFile)
  }

  protected fun getJarUrls(mainJarFile: File): Array<URL?> {
    val urls = arrayOfNulls<URL>(libraryJars.size + 1)
    urls[0] = mainJarFile.toURI().toURL()
    var i = 1
    for (jarFile in libraryJars) {
      urls[i++] = jarFile.toURI().toURL()
    }
    return urls
  }
}