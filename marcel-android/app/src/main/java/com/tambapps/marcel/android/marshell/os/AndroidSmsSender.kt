package com.tambapps.marcel.android.marshell.os

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.tambapps.marcel.android.marshell.broadcast.MessageDeliveredListener
import com.tambapps.marcel.android.marshell.broadcast.MessageSentListener
import com.tambapps.marcel.android.marshell.room.dao.MessageDao
import com.tambapps.marcel.android.marshell.room.entity.Message
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import marcel.lang.android.AndroidMessage
import java.time.LocalDateTime
import kotlin.reflect.KClass

class AndroidSmsSender(
  @ApplicationContext private val context: Context,
  private val smsManager: SmsManager,
  private val messageDao: MessageDao
) {

  companion object {
    const val MESSAGE_ID_KEY = "messageId"
  }
  fun sendSms(destinationAddress: String, text: String): AndroidMessage {
    val parts = smsManager.divideMessage(text)

    val sms = runBlocking {
      val message = Message(text = text, destination = destinationAddress,
        nbParts = parts.size, status = AndroidMessage.Status.CREATED, createdAt = LocalDateTime.now())
      val id = messageDao.insert(message)
      message.copy(id = id)
    }
    if (parts.size == 1) {
      smsManager.sendTextMessage(destinationAddress, null, parts.first(),
        pendingIntent(MessageSentListener::class, MessageSentListener.ACTION, sms.id),
        pendingIntent(MessageDeliveredListener::class, MessageDeliveredListener.ACTION, sms.id), sms.id)
    } else {
      val sentIntents = List(parts.size) { _ -> pendingIntent(MessageSentListener::class, MessageSentListener.ACTION, sms.id) }
      val deliveredIntents = List(parts.size) { _ -> pendingIntent(MessageDeliveredListener::class, MessageDeliveredListener.ACTION, sms.id) }
      smsManager.sendMultipartTextMessage(destinationAddress, null, parts, sentIntents, deliveredIntents, sms.id)
    }
    return MessageView(sms)
  }

  fun list(page: Int, pageSize: Int) = runBlocking { messageDao.listByRecency(pageSize, pageSize * page) }

  private fun pendingIntent(clazz: KClass<*>, action: String,messageId: Long): PendingIntent {
    val intent = Intent(context, clazz.java).apply { setAction(action) }
    intent.putExtra(MESSAGE_ID_KEY, messageId)
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  }

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