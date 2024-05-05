package com.tambapps.marcel.android.marshell.maven

import com.tambapps.marcel.android.compiler.DexUtils
import com.tambapps.marcel.dumbbell.storage.RemoteRepositoryStorage
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository
import java.io.File
import java.io.InputStream

class DexRemoteSavingRepository(root: File) : RemoteSavingMavenRepository(root, RemoteRepositoryStorage()) {

  override fun doSaveArtifactJar(file: File, inputStream: InputStream) {
    DexUtils.convertJarStreamToDexFile(inputStream, file)
  }

}