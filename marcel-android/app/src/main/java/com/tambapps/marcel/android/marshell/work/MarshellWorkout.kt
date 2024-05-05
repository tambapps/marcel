package com.tambapps.marcel.android.marshell.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MarshellWorkout @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
): CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    return Result.success()
  }
}