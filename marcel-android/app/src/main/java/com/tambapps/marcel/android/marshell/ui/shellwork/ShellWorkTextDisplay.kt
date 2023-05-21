package com.tambapps.marcel.android.marshell.ui.shellwork

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.work.WorkInfo
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.util.TimeUtils
import com.tambapps.marcel.android.marshell.work.ShellWork
import java.time.Duration
import java.time.temporal.ChronoUnit

interface ShellWorkTextDisplay {
  companion object {
    val ORANGE = Color.parseColor("#FFA500")
  }

  fun stateColor(work: ShellWork) = when {
    work.state == WorkInfo.State.SUCCEEDED -> Color.GREEN
    work.state == WorkInfo.State.CANCELLED -> ShellWorkTextDisplay.ORANGE // orange
    work.state == WorkInfo.State.FAILED -> Color.RED
    work.isPeriodic || work.state == WorkInfo.State.RUNNING -> Color.parseColor("#87CEEB")
    else -> Color.WHITE
  }

  fun displayWork(context: Context, work: ShellWork, name: TextView, startTime: TextView,
                  result: TextView, state: TextView, nextRun: TextView, singleLineStateText: Boolean = false) {
    result.text = when {
      work.state != WorkInfo.State.FAILED && work.result != null -> {
        val prefix = if (work.isPeriodic) "Last result" else "Result"
        prefix + ": ${work.result}"
      }
      work.state == WorkInfo.State.FAILED -> work.failedReason?.let { "Error: $it" } ?: "An error occurred"
      else -> null
    }

    name.text = work.name
    state.text = work.state.name
    state.setTextColor(stateColor(work))

    startTime.text = when {
      work.startTime != null ->
        if (work.isFinished) context.getString(
          R.string.work_ran_lasted, TimeUtils.smartToString(work.startTime), TimeUtils.humanReadableFormat(
          Duration.between(work.startTime, work.endTime)))
        else if (work.isPeriodic) when (work.state) {
          WorkInfo.State.RUNNING -> context.getString(R.string.work_started, TimeUtils.smartToString(work.startTime))
          else -> context.getString(
            R.string.work_last_ran_lasted, TimeUtils.smartToString(work.startTime), TimeUtils.humanReadableFormat(
            Duration.between(work.startTime, work.endTime)))
        }
        else context.getString(R.string.work_started, TimeUtils.smartToString(work.startTime))
      work.scheduledAt != null -> context.getString(R.string.scheduled_for, work.scheduledAt)
      work.failedReason == null -> context.getString(R.string.has_not_ran_yet)
      else -> context.getString(R.string.has_not_ran_yet)
    }

    nextRun.visibility = if (work.isPeriodic) View.VISIBLE else View.GONE

    if (work.isPeriodic && !work.state.isFinished) {
      state.text =
        if (work.state == WorkInfo.State.RUNNING) "RUNNING"
        else if (work.periodAmount == 1) context.getString(R.string.periodic_work_state_one, work.periodUnit!!.toString().removeSuffix("s"))
        else context.getString(R.string.periodic_work_state, work.periodAmount, work.periodUnit)
      if (singleLineStateText) state.text = state.text.toString().replace('\n', ' ')
      val durationBetweenNowAndNext = work.durationBetweenNowAndNext
      if (durationBetweenNowAndNext != null) {
        nextRun.visibility = View.VISIBLE
        nextRun.text = when {
          durationBetweenNowAndNext.isNegative -> context.getString(R.string.should_run_shortly)
          work.isPeriodic -> context.getString(R.string.next_run_in, TimeUtils.humanReadableFormat(durationBetweenNowAndNext, ChronoUnit.SECONDS))
          else -> context.getString(R.string.will_run_in, TimeUtils.humanReadableFormat(durationBetweenNowAndNext, ChronoUnit.SECONDS))
        }
      } else {
        nextRun.visibility = View.GONE
      }
    }
    for (tv in listOf(name, startTime, result, state, nextRun)) {
      tv.visibility = if (tv.text.isNullOrBlank()) View.GONE else View.VISIBLE
    }
  }
}