package com.tambapps.marcel.android.marshell.service

import com.tambapps.marcel.android.marshell.room.dao.CacheableScriptDao
import com.tambapps.marcel.android.marshell.room.entity.CacheableScript
import com.tambapps.marcel.compiler.JarWriter
import com.tambapps.marcel.repl.ReplCompilerResult
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.inject.Inject


class CacheableScriptService @Inject constructor(
  private val dao: CacheableScriptDao
) {

  suspend fun existsByName(name: String) = dao.existsByName(name)

  suspend fun create(name: String, scriptText: String, compilerResult: ReplCompilerResult) {
    val scriptCompiledClass = compilerResult.compiledScript.find { it.isScript }
    val bos = ByteArrayOutputStream()

    JarWriter(bos).use {
      it.writeClasses(compilerResult.allCompiledClasses)
    }

    val script = CacheableScript(name, scriptText, hash(scriptText), scriptCompiledClass?.className, bos.toByteArray())
    dao.insert(script)
  }

  private fun hash(text: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    messageDigest.update(text.toByteArray())
    return String(messageDigest.digest())
  }
}