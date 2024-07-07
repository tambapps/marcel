package com.tambapps.marcel.android.compiler

import android.util.Log
import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import com.android.tools.r8.OutputMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DexConverter {

  private companion object {
    const val TAG = "DexConverter"
  }

  // outputFile may be a file or a directory
  fun toDexJar(jarFile: File, outputFile: File) {
    require(jarFile.exists()) { "The provided file doesn't exists" }
    require(jarFile.isFile()) { "The provided file isn't a regular file" }
    val outputDirectory = if (outputFile.isDirectory()) outputFile else outputFile.parentFile
    if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
      throw DexException("Couldn't create directory $outputDirectory")
    }
    try {
      Log.d(TAG, "running D8 on jar $jarFile")
      val command = D8Command.builder()
        .addProgramFiles(Paths.get(jarFile.absolutePath))
        .setOutput(Paths.get(outputDirectory.absolutePath), OutputMode.DexIndexed)
        .setMinApiLevel(BuildConfig.MIN_SDK_VERSION)
        .build()
      D8.run(command)
      Log.d(TAG, "Successfully ran D8 on jar $jarFile")
    } catch (e: Exception) {
      throw DexException(e.message, e)
    }
    val dexFiles = outputDirectory.listFiles { dir, name -> name.endsWith(".dex") } ?: emptyArray()

    val dexJarOutputFile = if (outputFile.isDirectory) File(outputFile, jarFile.name) else outputFile
    try {
      ZipOutputStream(FileOutputStream(dexJarOutputFile)).use { jarOs ->
        for (dexFile in dexFiles) {
          val entry = ZipEntry(dexFile.name)
          val dalvikBytecode = dexFile.readBytes()
          entry.setSize(dalvikBytecode.size.toLong())
          jarOs.putNextEntry(entry)
          jarOs.write(dalvikBytecode)
          jarOs.closeEntry()
        }
      }
    } catch (e: IOException) {
      throw DexException("Couldn't write dex jar: ${e.message}", e)
    } finally {
      dexFiles.forEach { it.delete() }
    }
  }
}