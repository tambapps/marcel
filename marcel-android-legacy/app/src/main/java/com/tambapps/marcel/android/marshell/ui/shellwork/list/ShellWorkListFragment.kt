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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkListBinding
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.service.ShellWorkManager
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkTextDisplay
import com.tambapps.marcel.android.marshell.ui.shellwork.form.ShellWorkFormFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.view.ShellWorkViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
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

    refreshWorks()
    registerPeriodicCallback(this::refreshWorks)
    return root
  }

  private fun refreshWorks() {
    CoroutineScope(Dispatchers.IO).launch {
      val works = shellWorkManager.list()
      withContext(Dispatchers.Main) {
        refreshWorks(works)
      }
    }
  }

  private fun refreshWorks(works: List<ShellWork>) {
    // using _binding to avoid trying to modify views even though the fragment has been destroyed
    if (_binding == null) return
    val oldList = this.shellWorks.toImmutableList()
    val newList = works.sortedWith(compareBy({ it.isFinished }, { it.startTime?.toEpochSecond(ZoneOffset.UTC)?.times(-1) ?: Long.MIN_VALUE }))
    shellWorks.clear()
    shellWorks.addAll(newList)

    val diffs = DiffUtil.calculateDiff(ShellWorkDiffCallback(oldList, newList))
    binding.recyclerView.adapter?.let {
      diffs.dispatchUpdatesTo(it)
    }
  }

  private fun onWorkClick(work: ShellWork) {
    navigateTo(ShellWorkViewFragment.newInstance(work.name), R.drawable.edit)
  }

  private fun onWorkCancel(work: ShellWork) {
    CoroutineScope(Dispatchers.IO).launch {
      shellWorkManager.cancel(work.name)
      withContext(Dispatchers.Main) {
        refreshWorks()
      }
    }
  }

  private fun onWorkDelete(work: ShellWork) {
    CoroutineScope(Dispatchers.IO).launch {
      if (!shellWorkManager.delete(work.name)) return@launch

      withContext(Dispatchers.Main) {
        Toast.makeText(requireContext(), "Successfully deleted work", Toast.LENGTH_SHORT).show()
        val position = shellWorks.indexOf(work)
        if (position < 0) return@withContext
        shellWorks.removeAt(position)
        binding.recyclerView.adapter?.notifyItemRemoved(position)
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onFabClick() {
    navigateTo(ShellWorkFormFragment.newInstance(), R.drawable.plus)
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
                  getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireContext().getColor(R.color.orange))
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

  private class ShellWorkDiffCallback(private val oldList: List<ShellWork>,
                                      private val newList: List<ShellWork>): DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
      oldList.getOrNull(oldItemPosition) == newList.getOrNull(newItemPosition)

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
      // we want to update the duration between next run for periodic works, so they are considered never
      // to be the same
      newList.getOrNull(newItemPosition)?.isPeriodic != true
          && areItemsTheSame(oldItemPosition, newItemPosition)
  }

}