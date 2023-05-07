package com.tambapps.marcel.android.marshell.ui.shellwork.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkListBinding
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.form.ShellWorkFormFragment

class ShellWorkListFragment : ShellWorkFragment.ShellWorkFragmentChild() {

  companion object {
    fun newInstance() = ShellWorkListFragment()
  }
  private var _binding: FragmentShellWorkListBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    _binding = FragmentShellWorkListBinding.inflate(inflater, container, false)
    val root: View = binding.root


    return root
  }


  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onFabClick() {
    val fragment = ShellWorkFormFragment.newInstance()
    parentFragmentManager.beginTransaction()
      .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
      .add(R.id.container, fragment, fragment.javaClass.name)
      .show(fragment)
      .hide(this)
      .commitNow()
  }

}