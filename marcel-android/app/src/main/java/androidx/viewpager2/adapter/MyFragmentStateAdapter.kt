package androidx.viewpager2.adapter

import androidx.fragment.app.Fragment

abstract class MyFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {


  override fun placeFragmentInViewHolder(holder: FragmentViewHolder) {
    super.placeFragmentInViewHolder(holder)
    val fragment = mFragments[holder.itemId]
    onBindFragment(fragment!!, holder.adapterPosition)
  }

  fun getFragmentAt(i: Int): Fragment {
    return mFragments[getItemId(i)]!!
  }

  abstract fun onBindFragment(fragment: Fragment, position: Int)
}