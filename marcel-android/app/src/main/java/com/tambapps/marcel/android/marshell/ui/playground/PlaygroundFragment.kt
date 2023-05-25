package com.tambapps.marcel.android.marshell.ui.playground

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.databinding.FragmentPlaygroundBinding

class PlaygroundFragment: Fragment() {

  private var _binding: FragmentPlaygroundBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentPlaygroundBinding.inflate(inflater, container, false)
    return binding.root
  }
}