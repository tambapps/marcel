package com.tambapps.marcel.android.marshell.ui.shellwork.list

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkListBinding
import com.tambapps.marcel.android.marshell.service.ShellWorkManager
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkTextDisplay
import com.tambapps.marcel.android.marshell.ui.shellwork.form.ShellWorkFormFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.view.ShellWorkViewFragment
import com.tambapps.marcel.android.marshell.work.ShellWork
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneOffset
import javax.inject.Inject

@AndroidEntryPoint
class ShellWorkListFragment : ShellWorkFragment.ShellWorkFragmentChild(), ShellWorkTextDisplay {

  companion object {
    fun newInstance() = ShellWorkListFragment()
  }

  private var _binding: FragmentShellWorkListBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!
  private val shellWorks = mutableListOf<ShellWork>()
  @Inject
  lateinit var shellWorkManager: ShellWorkManager

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    _binding = FragmentShellWorkListBinding.inflate(inflater, container, false)
    val root: View = binding.root

    shellWorks.clear()
    binding.apply {
      recyclerView.apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(activity)
        adapter = MyAdapter(shellWorks,
          this@ShellWorkListFragment::onWorkClick,
          this@ShellWorkListFragment::onWorkCancel,
          this@ShellWorkListFragment::onWorkDelete)
      }
    }

    binding.swipeRefresh.apply {
      isRefreshing = true
      setOnRefreshListener {
        refreshWorks()
      }
    }

    CoroutineScope(Dispatchers.IO).launch {
      val liveData = shellWorkManager.listLive()
      withContext(Dispatchers.Main) {
        liveData.observe(viewLifecycleOwner, this@ShellWorkListFragment::refreshWorks)
      }
    }
    return root
  }

  fun refreshWorks() {
    CoroutineScope(Dispatchers.IO).launch {
      val works = shellWorkManager.list()
      withContext(Dispatchers.Main) {
        refreshWorks(works)
      }
    }
  }

  private fun refreshWorks(works: List<ShellWork>) {
    shellWorks.clear()
    shellWorks.addAll(works)
    shellWorks.sortWith(compareBy({ it.isFinished }, { it.startTime?.toEpochSecond(ZoneOffset.UTC)?.times(-1) ?: Long.MIN_VALUE }))
    binding.recyclerView.adapter?.notifyDataSetChanged()
    binding.swipeRefresh.isRefreshing = false
  }
  private fun onWorkClick(work: ShellWork) {
    val fragment = ShellWorkViewFragment.newInstance(work.id)
    parentFragmentManager.commit {
      setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
      // to handle back press
      addToBackStack(null)
      add(R.id.container, fragment, fragment.javaClass.name)
      show(fragment)
      hide(this@ShellWorkListFragment)
    }
    shellWorkFragment?.notifyNavigated(R.drawable.edit)
  }

  private fun onWorkCancel(work: ShellWork) {
    CoroutineScope(Dispatchers.IO).launch {
      shellWorkManager.cancel(work.id)
      withContext(Dispatchers.Main) {
        refreshWorks()
      }
    }
  }

  private fun onWorkDelete(work: ShellWork) {
    CoroutineScope(Dispatchers.IO).launch {
      if (!shellWorkManager.delete(work.id)) return@launch

      withContext(Dispatchers.Main) {
        Toast.makeText(requireContext(), "Successfully deleted work", Toast.LENGTH_SHORT).show()
        val position = shellWorks.indexOf(work)
        if (position < 0) return@withContext
        shellWorks.removeAt(position)
        binding.recyclerView.adapter?.notifyItemRemoved(position)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    refreshWorks()
  }
  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onFabClick(): Boolean {
    val fragment = ShellWorkFormFragment.newInstance()
    parentFragmentManager.commit {
      setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
      // to handle back press
      addToBackStack(null)
      add(R.id.container, fragment, fragment.javaClass.name)
      show(fragment)
      hide(this@ShellWorkListFragment)
    }
    shellWorkFragment?.notifyNavigated(R.drawable.save)
    return false // returning false because we want to modify the fab's icon with notifyNavigated
  }

  inner class MyAdapter(private val works: List<ShellWork>,
                  private val onWorkClick: (ShellWork) -> Unit,
                  private val onWorkCancel: (ShellWork) -> Unit,
                  private val onWorkDelete: (ShellWork) -> Unit) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    inner class MyViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
      val title = root.findViewById<TextView>(R.id.title)
      val result = root.findViewById<TextView>(R.id.result)
      val state = root.findViewById<TextView>(R.id.state)
      val startTime = root.findViewById<TextView>(R.id.startTime)
      val nextRun = root.findViewById<TextView>(R.id.nextRun)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
      val layout =  LayoutInflater.from(parent.context)
        .inflate(R.layout.work_summary_layout, parent, false)
      return MyViewHolder(layout)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
      val work = works[position]
      val context = holder.root.context
      holder.apply {
        root.setOnClickListener {
          onWorkClick.invoke(work)
        }

        root.setOnLongClickListener {
          if (!work.isFinished) {
            AlertDialog.Builder(context).setTitle(context.getString(R.string.cancel_work_q, work.name))
              .setNeutralButton(android.R.string.no, null)
              .setPositiveButton("yes") { _: DialogInterface, _: Int ->
                onWorkCancel(work)
              }.create()
              .apply {
                setOnShowListener {
                  getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ShellWorkTextDisplay.ORANGE)
                }
              }.show()
          } else {
            AlertDialog.Builder(context).setTitle(R.string.delete_shell_work)
              .setMessage(context.getString(R.string.delete_work_q, work.name))
              .setNeutralButton(R.string.cancel, null)
              .setPositiveButton(R.string.delete) { _: DialogInterface, _: Int ->
                onWorkDelete(work)
              }
              .create()
              .apply {
                setOnShowListener {
                  getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                }
              }.show()
          }
          return@setOnLongClickListener true
        }
        displayWork(context = requireContext(),
          name = title, result = result,
          work = work, startTime = startTime, state = state, nextRun = nextRun)
      }
    }

    override fun getItemCount(): Int {
      return works.size
    }
  }

}