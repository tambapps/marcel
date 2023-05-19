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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo.State
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentShellWorkListBinding
import com.tambapps.marcel.android.marshell.service.ShellWorkManager
import com.tambapps.marcel.android.marshell.ui.shellwork.ShellWorkFragment
import com.tambapps.marcel.android.marshell.ui.shellwork.form.ShellWorkFormFragment
import com.tambapps.marcel.android.marshell.util.TimeUtils
import com.tambapps.marcel.android.marshell.work.ShellWork
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject

@AndroidEntryPoint
class ShellWorkListFragment : ShellWorkFragment.ShellWorkFragmentChild() {

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

    CoroutineScope(Dispatchers.IO).launch {
      val worksLiveData = shellWorkManager.list()
      withContext(Dispatchers.Main) {
        worksLiveData.observe(viewLifecycleOwner) { works ->
          shellWorks.clear()
          shellWorks.addAll(works)
          binding.recyclerView.adapter?.notifyDataSetChanged()
        }
      }
    }
    return root
  }

  private fun onWorkClick(work: ShellWork) {
    Toast.makeText(requireContext(), "TODO", Toast.LENGTH_SHORT).show()
  }

  private fun onWorkCancel(work: ShellWork) {
    shellWorkManager.cancel(work.id)
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

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onFabClick() {
    val fragment = ShellWorkFormFragment.newInstance()
    parentFragmentManager.beginTransaction()
      .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
      .add(R.id.container, fragment, fragment.javaClass.name)
      .show(fragment)
      .hide(this)
      .commitNow()
  }

  class MyAdapter(private val works: List<ShellWork>,
                  private val onWorkClick: (ShellWork) -> Unit,
                  private val onWorkCancel: (ShellWork) -> Unit,
                  private val onWorkDelete: (ShellWork) -> Unit) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    companion object {
      val ORANGE = Color.parseColor("#FFA500")
    }
    class MyViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
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
        title.text = work.name
        result.text = if (work.result != null) "Result: ${work.result}" else null
        state.text = work.state.name
        state.setTextColor(stateColor(work))
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
                  getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ORANGE)
                }
              }.show()
          } else {
            AlertDialog.Builder(context).setTitle(R.string.delete_shell_work)
              .setMessage(R.string.delete_work_q)
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
        startTime.text = when {
          work.startTime != null ->
            if (work.isFinished) context.getString(R.string.work_ran_lasted, TimeUtils.smartToString(work.startTime), TimeUtils.humanReadableFormat(
              Duration.between(work.startTime, work.endTime)))
            else if (work.isPeriodic) when (work.state) {
              State.RUNNING -> context.getString(R.string.work_started, TimeUtils.smartToString(work.startTime))
              else -> context.getString(R.string.work_last_ran_lasted, TimeUtils.smartToString(work.startTime), TimeUtils.humanReadableFormat(
                Duration.between(work.startTime, work.endTime)))
            }
            else context.getString(R.string.work_started, TimeUtils.smartToString(work.startTime))
          work.scheduledAt != null -> context.getString(R.string.scheduled_for, work.scheduledAt)
          else -> ""
        }
        nextRun.visibility = if (work.isPeriodic) View.VISIBLE else View.GONE
        if (work.isPeriodic) {
          state.text = (state.text.toString() + "\n" + context.getString(R.string.every) + " " + work.periodAmount + " " + work.periodUnit)
          val durationBetweenNowAndNext = work.durationBetweenNowAndNext
          if (durationBetweenNowAndNext != null) {
            nextRun.visibility = View.VISIBLE
            nextRun.text = if (durationBetweenNowAndNext.isNegative) "Will run shortly" else context.getString(R.string.next_run_in, TimeUtils.humanReadableFormat(durationBetweenNowAndNext))
          }
        }
      }
    }

    private fun stateColor(work: ShellWork) = when {
      work.state == State.SUCCEEDED -> Color.GREEN
      work.state == State.CANCELLED -> ORANGE // orange
      work.state == State.FAILED -> Color.RED
      work.isPeriodic || work.state == State.RUNNING -> Color.parseColor("#87CEEB")
      else -> Color.WHITE
    }
    override fun getItemCount(): Int {
      return works.size
    }
  }

}