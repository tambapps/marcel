package com.tambapps.marcel.android.marshell

object Routes {
  const val HOME = "home"
  const val SHELL = "shell"
  const val NEW_SHELL = "$SHELL/new"
  const val EDITOR = "editor"
  const val WORK_LIST = "work_list"
  const val WORK_CREATE = "work_create"
  const val WORK_VIEW = "work_view"
  const val SETTINGS = "settings"

  const val WORK_NAME_ARG = "workName"
  const val FILE_ARG = "file"
  const val SESSION_ID = "sessionId"

  const val CONSULT = "$SHELL/{$SESSION_ID}/consult"
  fun consult(sessionId: Any) = "$SHELL/$sessionId/consult"
}