package com.tambapps.marcel.android.marshell.ui.shellwork.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkFormBinding

class ShellWorkFormFragment : Fragment() {

  companion object {
    fun newInstance() = ShellWorkFormFragment()
  }
  private var _binding: FragmentShellWorkFormBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    _binding = FragmentShellWorkFormBinding.inflate(inflater, container, false)
    val root: View = binding.root

    binding.title.text = "New Shell Work"

    return root
  }


  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}