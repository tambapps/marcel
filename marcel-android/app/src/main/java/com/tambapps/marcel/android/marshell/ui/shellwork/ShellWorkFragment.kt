package com.tambapps.marcel.android.marshell.ui.shellwork

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkBinding
import com.tambapps.marcel.android.marshell.ui.shellwork.list.ShellWorkListFragment

class ShellWorkFragment : Fragment() {

  companion object {
    const val TRANSITION_DURATION_MILLIS = 500L
  }
  abstract class ShellWorkFragmentChild: Fragment(), FabClickListener {

    private var handler: Handler? = null
    private val registeredCallbacks = mutableListOf<Pair<Runnable, Long>>()
    val shellWorkFragment get() = parentFragment as? ShellWorkFragment

    val fab get() = shellWorkFragment?.fab

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
  interface FabClickListener {

    // return true if navigated
    fun onFabClick(): Boolean
    fun nextFabResId(): Int = R.drawable.plus
  }

  private var _binding: FragmentShellWorkBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!
  val fab get() = binding.fab

  private val currentFragment: ShellWorkFragmentChild? get() = childFragmentManager.findFragmentById(R.id.container) as? ShellWorkFragmentChild

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (savedInstanceState == null) {
      childFragmentManager.beginTransaction()
        .replace(R.id.container, ShellWorkListFragment.newInstance(), ShellWorkListFragment::class.java.name)
        .commitNow()
    }
  }
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    _binding = FragmentShellWorkBinding.inflate(inflater, container, false)
    val root: View = binding.root

    binding.fab.setOnClickListener {
      if (currentFragment?.onFabClick() == true) {
        notifyNavigated()
      }
    }
    return root
  }

  fun notifyNavigated(resDrawable: Int = R.drawable.plus) {
    binding.fab.hide()
    Handler(Looper.getMainLooper()).postDelayed({
      binding.fab.setImageResource(resDrawable)
      binding.fab.show()
    }, TRANSITION_DURATION_MILLIS)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}