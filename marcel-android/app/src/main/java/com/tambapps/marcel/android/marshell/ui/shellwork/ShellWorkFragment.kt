package com.tambapps.marcel.android.marshell.ui.shellwork

import android.os.Handler
import android.os.Looper
import com.tambapps.marcel.android.marshell.ui.ResourceParentFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.list.ShellWorkListFragment

class ShellWorkFragment : ResourceParentFragment() {

  companion object {
    const val TRANSITION_DURATION_MILLIS = 500L
  }
  abstract class ShellWorkFragmentChild: ChildFragment() {

    private var handler: Handler? = null
    private val registeredCallbacks = mutableListOf<Pair<Runnable, Long>>()

    fun registerPeriodicCallback(callback: () -> Unit, delay: Long = 1_000L) {
      if (handler == null) {
        handler = Handler(Looper.getMainLooper())
      }
      val runnable = object: Runnable { //do something
        override fun run() {
          callback()
          handler?.postDelayed(this, 1_000L)
        }
      }
      handler!!.postDelayed(runnable, delay)
      registeredCallbacks.add(Pair(runnable, delay))
    }

    override fun onResume() {
      super.onResume()
      if (handler != null) {
        registeredCallbacks.forEach {
          handler!!.postDelayed(it.first, it.second)
        }
      }
    }

    override fun onPause() {
      super.onPause()
      if (handler != null) {
        registeredCallbacks.forEach {
          handler!!.removeCallbacks(it.first)
        }
      }
    }
  }

  override fun initialFragment() = ShellWorkListFragment.newInstance()

}