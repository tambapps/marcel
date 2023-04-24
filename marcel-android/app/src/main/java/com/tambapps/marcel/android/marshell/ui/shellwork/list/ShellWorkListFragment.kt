package com.tambapps.marcel.android.marshell.ui.shellwork.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkListBinding

class ShellWorkListFragment : Fragment() {

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
}