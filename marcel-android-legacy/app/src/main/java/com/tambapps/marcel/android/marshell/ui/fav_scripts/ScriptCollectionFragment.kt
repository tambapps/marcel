package com.tambapps.marcel.android.marshell.ui.fav_scripts

import com.tambapps.marcel.android.marshell.ui.ResourceParentFragment
import com.tambapps.marcel.android.marshell.ui.fav_scripts.list.ScriptListFragment

class ScriptCollectionFragment: ResourceParentFragment() {

  override fun initialFragment() = ScriptListFragment.newInstance()

}