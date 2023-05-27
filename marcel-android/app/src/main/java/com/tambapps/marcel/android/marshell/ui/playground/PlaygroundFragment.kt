package com.tambapps.marcel.android.marshell.ui.playground

import com.tambapps.marcel.android.marshell.ui.ResourceParentFragment
import com.tambapps.marcel.android.marshell.ui.playground.list.PlaygroundListFragment

class PlaygroundFragment: ResourceParentFragment() {

  override fun initialFragment() = PlaygroundListFragment.newInstance()

}