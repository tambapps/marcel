package com.tambapps.marcel.android.marshell.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tambapps.marcel.android.marshell.util.TimeUtils
import marcel.lang.android.AndroidMessage
import java.time.LocalDateTime

@Entity("messages")
data class Message(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  override val text: String,
  override val destination: String,
  override val status: AndroidMessage.Status,
  override val createdAt: LocalDateTime,
  override val sentAt: LocalDateTime?,
  override val deliveredAt: LocalDateTime?,
  override val nbParts: Int,
  override val nbPartsSent: Int = 0,
  override val nbPartsDelivered: Int = 0,
): AndroidMessage {

  constructor(text: String, destination: String, nbParts: Int, status: AndroidMessage.Status, createdAt: LocalDateTime): this(
    text = text, destination = destination, status = status, createdAt = createdAt,
    nbParts = nbParts,
    sentAt = null,
    deliveredAt = null
  )

  override fun toString() = StringBuilder().apply {
    append("Message(id=$id, destination=$destination, text=$text, status=$status, createdAt=")
    append(TimeUtils.DATE_TIME_FORMATTER.format(createdAt))
    if (sentAt != null) {
      append(", sentAt=")
      append(TimeUtils.DATE_TIME_FORMATTER.format(sentAt))
    }
    if (deliveredAt != null) {
      append(", deliveredAt=")
      append(TimeUtils.DATE_TIME_FORMATTER.format(deliveredAt))
    }
    append(")")
  }.toString()
}