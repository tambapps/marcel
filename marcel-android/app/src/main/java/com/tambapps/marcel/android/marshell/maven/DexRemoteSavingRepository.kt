package com.tambapps.marcel.android.marshell.maven

import com.tambapps.marcel.dalvik.compiler.DexUtils
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository
import java.io.File
import java.io.InputStream

class DexRemoteSavingRepository(root: File) : RemoteSavingMavenRepository(root) {

  override fun doSaveArtifactJar(file: File, inputStream: InputStream) {
    DexUtils.convertJarStreamToDexFile(inputStream, file)
  }

}