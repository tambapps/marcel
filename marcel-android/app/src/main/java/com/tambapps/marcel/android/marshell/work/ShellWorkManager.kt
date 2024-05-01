package com.tambapps.marcel.android.marshell.work

import androidx.work.WorkInfo
import com.tambapps.marcel.android.marshell.room.entity.ShellWork
import com.tambapps.marcel.android.marshell.room.entity.WorkPeriodUnit
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class ShellWorkManager @Inject constructor() {

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
}