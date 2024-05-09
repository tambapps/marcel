package com.tambapps.marcel.android.marshell.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle

@Composable
fun LifecycleStateListenerEffect(
  onResume: (() -> Unit)?
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
  DisposableEffect(lifecycleState) {
    when (lifecycleState) {
      Lifecycle.State.DESTROYED -> {}
      Lifecycle.State.INITIALIZED -> {}
      Lifecycle.State.CREATED -> {}
      Lifecycle.State.STARTED -> {}
      Lifecycle.State.RESUMED -> onResume?.invoke()
    }
    onDispose {  }
  }

}