package com.tambapps.marcel.android.marshell.work

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriodUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class ShellWorkManager @Inject constructor(
  @ApplicationContext private val applicationContext: Context
) {

  private val workManager by lazy { WorkManager.getInstance(applicationContext) }

  suspend fun list(): List<ShellWork> {
    return listOf(
      ShellWork(
      name = "Work 1",
      workId = UUID.randomUUID(),
        description = "some work",
        isNetworkRequired = true,
        isSilent = false,
        state = WorkInfo.State.ENQUEUED,
        periodAmount = 2,
        periodUnit = WorkPeriodUnit.DAYS,
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