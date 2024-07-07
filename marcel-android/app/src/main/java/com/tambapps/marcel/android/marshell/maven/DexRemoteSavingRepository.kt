package com.tambapps.marcel.android.marshell.maven

import com.tambapps.marcel.android.compiler.DexConverter
import com.tambapps.marcel.dumbbell.storage.RemoteRepositoryStorage
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class DexRemoteSavingRepository(root: File) : RemoteSavingMavenRepository(root, RemoteRepositoryStorage()) {

  private val dexConverter = DexConverter()
  override fun doSaveArtifactJar(file: File, inputStream: InputStream) {
    val tempJarFile =
      Files.createTempFile(file.parentFile!!.toPath(), null, file.name)
        .toFile()
    try {
      Files.copy(inputStream, tempJarFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
      dexConverter.toDexJar(tempJarFile, file)
      if (!file.setReadOnly()) { // needed because of an android security restriction
        throw IOException("Couldn't make file $file read only")
      }
    } finally {
      tempJarFile.delete()
    }
  }
}