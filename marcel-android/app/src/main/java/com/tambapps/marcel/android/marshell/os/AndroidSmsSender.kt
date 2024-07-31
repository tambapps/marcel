package com.tambapps.marcel.android.marshell.os

import android.telephony.SmsManager
import com.tambapps.marcel.android.marshell.room.dao.MessageDao
import com.tambapps.marcel.android.marshell.room.entity.Message
import kotlinx.coroutines.runBlocking
import marcel.lang.android.AndroidMessage
import java.time.LocalDateTime

class AndroidSmsSender(
  private val smsManager: SmsManager,
  private val messageDao: MessageDao
) {

  fun sendSms(destinationAddress: String, text: String): AndroidMessage {
    // TODO handle delivery intent to listen to success or failure
    val parts = smsManager.divideMessage(text)

    val sms = runBlocking {
      val message = Message(text = text, destination = destinationAddress,
        nbParts = parts.size, status = AndroidMessage.Status.CREATED, createdAt = LocalDateTime.now())
      val id = messageDao.insert(message)
      message.copy(id = id)
    }
    if (parts.size == 1) {
      smsManager.sendTextMessage(destinationAddress, null, parts.first(), null, null, sms.id)
    } else {
      smsManager.sendMultipartTextMessage(destinationAddress, null, parts, null, null, sms.id)
    }
    return MessageView(sms)
  }

  fun list() = runBlocking { messageDao.listByRecency() }


  private inner class MessageView(
    private var message: Message
  ): AndroidMessage {
    override val text: String
      get() = message.text
    override val destination: String
      get() = message.destination
    override val status: AndroidMessage.Status
      get() = updateAndGet(Message::status)
    override val createdAt: LocalDateTime
      get() = message.createdAt
    override val sentAt: LocalDateTime?
      get() = updateAndGet(Message::sentAt)
    override val deliveredAt: LocalDateTime?
      get() = updateAndGet(Message::deliveredAt)
    override val nbParts: Int
      get() = message.nbParts
    override val nbPartsSent: Int
      get() = updateAndGet(Message::nbPartsSent)
    override val nbPartsDelivered: Int
      get() = updateAndGet(Message::nbPartsDelivered)

    private inline fun <T> updateAndGet(getter: (Message) -> T): T {
      message = runBlocking { messageDao.get(message.id) }
      return getter.invoke(message)
    }

    override fun toString() = updateAndGet(Message::toString)
  }
}