package com.tambapps.marcel.android.marshell.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentResourceParentBinding
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment

abstract class ResourceParentFragment: Fragment() {

  abstract class ChildFragment(
    private val previousFragmentFabDrawable: Int?
  ): Fragment(), FabClickListener {
    constructor(): this(null)

    val resourceParentFragment get() = parentFragment as? ResourceParentFragment
    val parentFab get() = resourceParentFragment?.fab

    override fun onAttach(context: Context) {
      super.onAttach(context)
      if (previousFragmentFabDrawable != null && parentFragment != null) {
        val callback = object : OnBackPressedCallback(
          true // default to enabled
        ) {
          override fun handleOnBackPressed() {
            parentFab?.setImageResource(previousFragmentFabDrawable)
            parentFragmentManager.popBackStack()
          }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
          this, // LifecycleOwner
          callback
        )
      }
    }

  }

  interface FabClickListener {

    // return true if navigated
    fun onFabClick(): Boolean
    fun nextFabResId(): Int = R.drawable.plus
  }

  val fab get() = binding.fab
  private var _binding: FragmentResourceParentBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val currentFragment: ChildFragment? get() = childFragmentManager.findFragmentById(R.id.container) as? ChildFragment

  abstract fun initialFragment(): ChildFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (savedInstanceState == null) {
      val fragment = initialFragment()
      childFragmentManager.beginTransaction()
        .replace(R.id.container, fragment, fragment.javaClass.name)
        .commitNow()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    _binding = FragmentResourceParentBinding.inflate(inflater, container, false)
    val root: View = binding.root

    binding.fab.setOnClickListener {
      if (currentFragment?.onFabClick() == true) {
        notifyNavigated()
      }
    }
    return root
  }

  fun notifyNavigated(resDrawable: Int = R.drawable.plus) {
    binding.fab.hide()
    Handler(Looper.getMainLooper()).postDelayed({
      binding.fab.setImageResource(resDrawable)
      binding.fab.show()
    }, ShellWorkFragment.TRANSITION_DURATION_MILLIS)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}