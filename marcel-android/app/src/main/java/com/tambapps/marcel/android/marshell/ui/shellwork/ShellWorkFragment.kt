package com.tambapps.marcel.android.marshell.ui.shellwork

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkBinding
import com.tambapps.marcel.android.marshell.ui.shellwork.list.ShellWorkListFragment

class ShellWorkFragment : Fragment() {

  abstract class ShellWorkFragmentChild: Fragment(), FabClickListener {}
  interface FabClickListener {
    fun onFabClick()
    fun nextFabResId(): Int = R.drawable.plus
  }

  private var _binding: FragmentShellWorkBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

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
      currentFragment?.onFabClick()
    }
    return root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}