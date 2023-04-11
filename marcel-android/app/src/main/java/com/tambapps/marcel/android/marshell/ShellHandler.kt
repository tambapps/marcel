package com.tambapps.marcel.android.marshell

import androidx.recyclerview.widget.ListUpdateCallback
import com.tambapps.marcel.android.marshell.data.ShellSession

interface ShellHandler {

  val sessionsCount: Int
  val sessions: List<ShellSession>
  fun getSessionAt(i: Int): ShellSession

  fun registerCallback(callback: ListUpdateCallback)
  fun unregisterCallback(callback: ListUpdateCallback): Boolean

  fun startNewSession(): Boolean // return true if actually started new session
  fun stopSession(shellSession: ShellSession): Boolean
  fun stopSession(position: Int)

  fun navigateToShell(scriptText: CharSequence, position: Int? = null)
}