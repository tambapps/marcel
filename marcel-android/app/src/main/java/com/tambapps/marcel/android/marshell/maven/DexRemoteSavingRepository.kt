package com.tambapps.marcel.android.marshell.maven

import com.tambapps.marcel.android.compiler.DexUtils
import com.tambapps.marcel.dumbbell.storage.RemoteRepositoryStorage
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository
import java.io.File
import java.io.IOException
import java.io.InputStream

class DexRemoteSavingRepository(root: File) : RemoteSavingMavenRepository(root, RemoteRepositoryStorage()) {

  override fun doSaveArtifactJar(file: File, inputStream: InputStream) {
    DexUtils.convertJarStreamToDexFile(inputStream, file)
    if (!file.setReadOnly()) { // needed because of an android security restriction
      throw IOException("Couldn't make the file read only")
    }
  }

}