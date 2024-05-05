package com.tambapps.marcel.android.marshell.work

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.google.common.util.concurrent.ListenableFuture
import com.tambapps.marcel.android.marshell.room.dao.ShellWorkDao
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriod
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriodUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class ShellWorkManager @Inject constructor(
  @ApplicationContext private val applicationContext: Context,
  //private val shellWorkDao: ShellWorkDao
) {

  private companion object {
    const val SHELL_WORK_TAG = "type:shell_work"
  }

  private val workManager by lazy { WorkManager.getInstance(applicationContext) }



  suspend fun listFake(): List<ShellWork> {
    return listOf(
      ShellWork(
      name = "Work 1",
      workId = UUID.randomUUID(),
        description = "some work",
        isNetworkRequired = true,
        isSilent = false,
        state = WorkInfo.State.ENQUEUED,
        period = WorkPeriod(amount = 2, unit = WorkPeriodUnit.DAYS),
        startTime = LocalDateTime.now().minusSeconds(36000),
        endTime = LocalDateTime.now().minusSeconds(26000),
        scheduledAt = null,
        logs = null,
        result = null,
        failedReason = null,
        scriptText = null
    )
    )
  }

  suspend fun create() {

  }
}