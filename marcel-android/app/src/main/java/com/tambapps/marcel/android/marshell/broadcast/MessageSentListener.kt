package com.tambapps.marcel.android.marshell.broadcast

import com.tambapps.marcel.android.marshell.room.entity.Message
import dagger.hilt.android.AndroidEntryPoint
import marcel.lang.android.AndroidMessage
import java.time.LocalDateTime

@AndroidEntryPoint
class MessageSentListener: MessageStatusListener() {

  companion object {
    const val ACTION = "SMS_SENT"
  }

  override val listenedStatus = AndroidMessage.Status.SENT

  override fun getNbPartStatus(message: Message) = message.nbPartsSent

  override fun update(
    message: Message,
    status: AndroidMessage.Status,
    nbPartStatus: Int,
    statusAt: LocalDateTime?
  ) = message.copy(
    nbPartsSent = nbPartStatus,
    status = status,
    sentAt = statusAt
  )
}