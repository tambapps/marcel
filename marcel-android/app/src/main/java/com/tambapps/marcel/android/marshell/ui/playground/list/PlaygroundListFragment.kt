package com.tambapps.marcel.android.marshell.ui.playground.list

import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.ui.ResourceParentFragment
import com.tambapps.marcel.android.marshell.ui.playground.PlaygroundFormFragment

class PlaygroundListFragment: ResourceParentFragment.ChildFragment() {

  companion object {
    fun newInstance() = PlaygroundListFragment()
  }

  override fun onFabClick() {
    navigateTo(PlaygroundFormFragment.newInstance(), R.drawable.plus)
  }
}