package com.tambapps.marcel.android.marshell

import java.io.File
import java.net.URLEncoder

object Routes {
  const val HOME = "home"
  const val SHELL = "shell"
  const val EDITOR = "editor"
  const val WORKOUT_LIST = "workout_list"
  const val WORKOUT_FORM = "workout_form"
  const val WORKOUT_VIEW = "workout_view"
  const val SETTINGS = "settings"
  const val DOCUMENTATION = "documentation"

  const val WORKOUT_NAME_ARG = "workName"
  const val EDIT_ARG = "edit"
  const val PATH_ARG = "path"
  const val FILE_ARG = "file"
  const val SESSION_ID = "sessionId"

  const val CONSULT = "$SHELL/{$SESSION_ID}/consult"

  fun consultWorkout(workoutName: String) = "$WORKOUT_VIEW/$workoutName"
  fun editWorkout(workoutName: String, edit: Boolean) = "$WORKOUT_FORM?$WORKOUT_NAME_ARG=$workoutName&${EDIT_ARG}=$edit"
  fun consultShell(sessionId: Any) = "$SHELL/$sessionId/consult"
  fun editScript(file: File) = EDITOR + "?" + FILE_ARG + "=" + URLEncoder.encode(file.canonicalPath, "UTF-8")
  fun documentation(path: String?) = if (path != null) DOCUMENTATION + "?" + PATH_ARG + "=" + URLEncoder.encode(path, "UTF-8")
  else DOCUMENTATION
}