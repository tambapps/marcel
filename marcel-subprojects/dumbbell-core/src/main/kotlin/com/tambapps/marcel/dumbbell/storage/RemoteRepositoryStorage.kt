package com.tambapps.marcel.dumbbell.storage

import com.tambapps.maven.dependency.resolver.exception.ResourceNotFound
import com.tambapps.maven.dependency.resolver.storage.RepositoryStorage
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream

class RemoteRepositoryStorage constructor(
  private val repoUrl: String = MAVEN_REPO_URL,
  private val client: OkHttpClient = OkHttpClient()
) : RepositoryStorage {
  companion object {
    const val MAVEN_REPO_URL: String = "https://repo1.maven.org/maven2"
  }

  @Throws(IOException::class)
  override fun exists(resourcePath: String): Boolean {
    fetch(resourcePath).use { response ->
      return response.isSuccessful
    }
  }

  @Throws(IOException::class)
  override fun get(resourcePath: String): InputStream {
    val response = fetch(resourcePath)
    return responseStream(response, resourcePath)
  }

  @Throws(IOException::class)
  private fun fetch(resourcePath: String): Response {
    return client.newCall(Request.Builder().url("$repoUrl/$resourcePath").build()).execute()
  }

  @Throws(IOException::class)
  private fun responseStream(response: Response, resourcePath: String): InputStream {
    if (response.isSuccessful) {
      return ResponseBodyInputStream(response)
    } else if (response.code == 404) {
      throw ResourceNotFound()
    } else {
      throw IOException(String.format("Requesting resource %s failed: %s", resourcePath, response.message))
    }
  }

  internal class ResponseBodyInputStream(private val response: Response) : InputStream() {
    private val inputStream = response.body!!.byteStream()

    @Throws(IOException::class)
    override fun read(): Int {
      return inputStream.read()
    }

    @Throws(IOException::class)
    override fun available(): Int {
      return inputStream.available()
    }

    override fun markSupported(): Boolean {
      return inputStream.markSupported()
    }

    @Synchronized
    override fun mark(readlimit: Int) {
      inputStream.mark(readlimit)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
      return inputStream.read(b)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
      return inputStream.read(b, off, len)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun reset() {
      inputStream.reset()
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
      return inputStream.skip(n)
    }

    @Throws(IOException::class)
    override fun close() {
      try {
        inputStream.close()
      } finally {
        response.close()
      }
    }
  }

}
