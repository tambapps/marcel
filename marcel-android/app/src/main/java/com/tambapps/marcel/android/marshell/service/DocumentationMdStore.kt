package com.tambapps.marcel.android.marshell.service

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcel.lang.Markdown
import okhttp3.OkHttpClient
import okhttp3.Request
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DocumentationMdStore @Inject constructor(
  private val okHttp: OkHttpClient,
  @Named("documentationCacheDir") private val documentationCacheDir: File
) {
  companion object {
    private const val ROOT_URL = "https://raw.githubusercontent.com/tambapps/marcel/main/documentation/src"
    const val SUMMARY = "/SUMMARY.md"
    const val TAG = "DocumentationMdStore"
  }

  // should be called from background thread
  fun get(path: String?): Result<Node> {
    val cachedFile = getCachedFile(path)
    // file not older than more than one day
    if (cachedFile.exists() && (System.currentTimeMillis() - cachedFile.lastModified()).toDuration(DurationUnit.MILLISECONDS) <= 1.days) {
      return tryGetFromCache(cachedFile)
    }

    val fetchResult = tryFetch(path)
    if (fetchResult.isSuccess || !cachedFile.exists()) {
      return fetchResult
    }
    return tryGetFromCache(cachedFile)
  }

  private fun tryFetch(path: String?): Result<Node> {
    return try {
      Log.d(TAG, "Fetching md doc for path $path")
      okHttp.newCall(Request.Builder().get().url(getUrl(path)).get().build()).execute().use { response ->
        if (response.isSuccessful && response.body != null) {
          val content = response.body!!.string()
          val node = Markdown.PARSER.parse(content)
          storeInCacheIfNecessary(path, content)
          Result.success(node)
        } else {
          // fallback to cache
          Result.failure(RuntimeException("Got HTTP error response code ${response.code}"))
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "An error occurred while fetching md doc $path", e)
      Result.failure(e)
    }
  }

  private fun tryGetFromCache(cachedFile: File): Result<Node> {
    Log.d(TAG, "Getting from cache md doc $cachedFile")
    try {
      val node = Markdown.PARSER.parse(cachedFile.readText())
      return Result.success(node)
    } catch (e: Exception) {
      Log.e(TAG, "An error occurred while getting from cache md doc $cachedFile", e)
      return Result.failure(e)
    }

  }
  private fun getUrl(path: String?): String {
    if (path == null) {
      return "$ROOT_URL/marcel.md"
    }
    return if (path.startsWith("/")) "$ROOT_URL$path"
    else "$ROOT_URL/$path"
  }

  private fun getCachedFile(path: String?) = File(documentationCacheDir, path ?: "home.md").canonicalFile

  private fun storeInCacheIfNecessary(path: String?, content: String) {
    val file = getCachedFile(path)
    if (!file.exists()) {
      if (!file.parentFile!!.isDirectory && !file.parentFile!!.mkdirs()) {
        Log.e(TAG, "Couldn't create directory of file ${file.parentFile}")
        return
      }
      storeInCache(file, content)
    } else if ((System.currentTimeMillis() - file.lastModified()).toDuration(DurationUnit.MILLISECONDS) <= 1.days) {
      storeInCache(file, content)
    }
  }

  private fun storeInCache(file: File, content: String) {
    Log.d(TAG, "storing in cache in file $file")
    try {
      file.writeText(content)
    } catch (e: IOException) {
      Log.e(TAG, "Error while saving md doc $file into cache", e)
    }
  }
}