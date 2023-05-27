package com.tambapps.marcel.android.marshell.ui.fav_scripts.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentScriptListBinding
import com.tambapps.marcel.android.marshell.ui.ResourceParentFragment
import com.tambapps.marcel.android.marshell.ui.fav_scripts.form.ScriptFormFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScriptListFragment: ResourceParentFragment.ChildFragment() {

  companion object {
    fun newInstance() = ScriptListFragment()
  }
  private var _binding: FragmentScriptListBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentScriptListBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onFabClick() {
    navigateTo(ScriptFormFragment.newInstance(), R.drawable.save)
  }
}