package com.tambapps.marcel.android.marshell.ui.shellwork

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkBinding
import com.tambapps.marcel.android.marshell.ui.shellwork.form.ShellWorkFormFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.list.ShellWorkListFragment

class ShellWorkFragment : Fragment() {

  private var _binding: FragmentShellWorkBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val currentFragment: Fragment get() = childFragmentManager.findFragmentById(R.id.container)!!

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
      showNewShellWorkFragment()
    }
    return root
  }

  private fun showNewShellWorkFragment() {
    val fragment = ShellWorkFormFragment.newInstance()
    childFragmentManager.beginTransaction()
      .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
      .add(R.id.container, fragment, fragment.javaClass.name)
      .show(fragment)
      .hide(currentFragment)
      .commitNow()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}