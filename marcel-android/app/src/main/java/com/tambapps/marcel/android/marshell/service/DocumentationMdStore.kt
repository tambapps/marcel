package com.tambapps.marcel.android.marshell.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import javax.inject.Inject

class DocumentationMdStore @Inject constructor(
  private val parser: Parser,
  private val okHttp: OkHttpClient
) {
  companion object {
    private const val ROOT_URL = "https://raw.githubusercontent.com/tambapps/marcel/main/documentation/src"
    const val SUMMARY = "/SUMMARY.md"
  }

  suspend fun get(path: String?, onSuccess: (Node) -> Unit, onError: () -> Unit) {
    okHttp.newCall(Request.Builder().get().url(getUrl(path)).get().build()).execute().use { response ->
      if (response.isSuccessful && response.body != null) {
        withContext(Dispatchers.Main) {
          // TODO cache response somewhere?
          val node = parser.parse(response.body!!.string())
          onSuccess.invoke(node)
        }
      } else {
        onError.invoke()
      }
    }
  }

  private fun getUrl(path: String?): String {
    if (path == null) {
      return "$ROOT_URL/marcel.md"
    }
    return if (path.startsWith("/")) "$ROOT_URL$path"
    else "$ROOT_URL/$path"
  }
}