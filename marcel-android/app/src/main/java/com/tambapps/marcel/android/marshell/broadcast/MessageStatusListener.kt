package com.tambapps.marcel.android.marshell.broadcast

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tambapps.marcel.android.marshell.os.AndroidSmsSender
import com.tambapps.marcel.android.marshell.room.dao.MessageDao
import com.tambapps.marcel.android.marshell.room.entity.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import marcel.lang.android.AndroidMessage
import java.time.LocalDateTime
import javax.inject.Inject

abstract class MessageStatusListener: BroadcastReceiver() {

  @Inject lateinit var messageDao: MessageDao

  private val ioScope = CoroutineScope(Dispatchers.IO)

  abstract fun update(message: Message,
                      status: AndroidMessage.Status,
                      nbPartStatus: Int,
                      statusAt: LocalDateTime?): Message

  abstract val listenedStatus: AndroidMessage.Status

  abstract fun getNbPartStatus(message: Message): Int

  override fun onReceive(context: Context?, intent: Intent?) {
    val now = LocalDateTime.now()
    val messageId = intent?.getLongExtra(AndroidSmsSender.MESSAGE_ID_KEY, 0) ?: return
    Log.d(javaClass.simpleName, "Handling $listenedStatus event for message with id $messageId")
    ioScope.launch {
      val message = messageDao.findById(messageId)
      if (message == null) {
        Log.w(javaClass.simpleName, "Couldn't find message with id $messageId")
        return@launch
      }
      var nbPartsStatus = getNbPartStatus(message)
      var error = false
      when (resultCode) {
        Activity.RESULT_OK -> nbPartsStatus++
        else -> error = true
      }
      val status = when {
        error -> AndroidMessage.Status.FAILED
        nbPartsStatus == message.nbParts -> listenedStatus
        else -> message.status
      }
      messageDao.update(update(
        message = message,
        nbPartStatus = nbPartsStatus,
        status = status,
        statusAt = if (status == listenedStatus) now else null
      ))
    }
  }

}