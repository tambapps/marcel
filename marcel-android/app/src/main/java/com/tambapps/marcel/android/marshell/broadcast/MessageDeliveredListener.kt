package com.tambapps.marcel.android.marshell.broadcast

import com.tambapps.marcel.android.marshell.room.entity.Message
import dagger.hilt.android.AndroidEntryPoint
import marcel.lang.android.AndroidMessage
import java.time.LocalDateTime

@AndroidEntryPoint
class MessageDeliveredListener: MessageStatusListener() {

  companion object {
    const val ACTION = "SMS_DELIVERED"
  }

  override val listenedStatus = AndroidMessage.Status.DELIVERED

  override fun getNbPartStatus(message: Message) = message.nbPartsDelivered

  override fun update(
    message: Message,
    status: AndroidMessage.Status,
    nbPartStatus: Int,
    statusAt: LocalDateTime?
  ) = message.copy(
    nbPartsDelivered = nbPartStatus,
    status = status,
    sentAt = statusAt
  )
}