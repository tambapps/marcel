package com.tambapps.marcel.android.marshell.ui.shellwork

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkBinding
import com.tambapps.marcel.android.marshell.ui.shellwork.list.ShellWorkListFragment
import java.time.Duration
import java.time.Instant

class ShellWorkFragment : Fragment() {

  companion object {
    const val TRANSITION_DURATION_MILLIS = 500L
  }
  abstract class ShellWorkFragmentChild: Fragment(), FabClickListener {}
  interface FabClickListener {

    // return true if navigated
    fun onFabClick(): Boolean
    fun nextFabResId(): Int = R.drawable.plus
  }

  private var _binding: FragmentShellWorkBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val currentFragment: ShellWorkFragmentChild? get() = childFragmentManager.findFragmentById(R.id.container) as? ShellWorkFragmentChild
  // used to avoid clicking twice on fab while navigating
  private var lastNavigated: Instant? = null

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
      if ((lastNavigated == null || Duration.between(lastNavigated, Instant.now()) > Duration.ofMillis(TRANSITION_DURATION_MILLIS))
        && currentFragment?.onFabClick() == true) {
        lastNavigated = Instant.now()
      }
    }
    return root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}