package com.tambapps.marcel.android.marshell

import com.tambapps.marcel.android.marshell.data.ShellSession

interface ShellHandler {

  val shellSessions: List<ShellSession>

  fun startNewSession(): Boolean // return true if actually started new session
}