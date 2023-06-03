package com.tambapps.marcel.android.marshell.maven

import com.tambapps.marcel.dalvik.compiler.DexUtils
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository
import java.io.File
import java.io.InputStream

class DexRemoteSavingRepository(root: File) : RemoteSavingMavenRepository(root) {

  override fun doSaveArtifactJar(groupId: String, artifactId: String, version: String,
                                 file: File, inputStream: InputStream) {
    val tempJar = File(repoRoot, getKey(groupId, artifactId, version) + "-temp.jar")
    super.doSaveArtifactJar(groupId, artifactId, version, tempJar, inputStream)
    DexUtils.convertJarToDexFile(tempJar, file)
    tempJar.delete()
  }

}