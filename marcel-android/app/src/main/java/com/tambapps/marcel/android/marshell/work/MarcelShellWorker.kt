package com.tambapps.marcel.android.marshell.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MarcelShellWorker
  @AssistedInject constructor(@Assisted appContext: Context,
                              @Assisted workerParams: WorkerParameters,):
  Worker(appContext, workerParams) {
  override fun doWork(): Result {
    val startTime = System.currentTimeMillis()
    // TODO

    val endTime = System.currentTimeMillis()

    return Result.success(Data.Builder()
      .putLong("startTime", startTime)
      .putLong("endTime", endTime)
      .build())
  }
}