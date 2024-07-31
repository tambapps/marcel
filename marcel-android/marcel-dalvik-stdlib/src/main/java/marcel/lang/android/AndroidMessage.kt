package marcel.lang.android

import java.time.LocalDateTime

interface AndroidMessage {

  val text: String
  val destination: String
  val status: Status
  val createdAt: LocalDateTime
  val sentAt: LocalDateTime?
  val deliveredAt: LocalDateTime?
  val nbParts: Int
  val nbPartsSent: Int
  val nbPartsDelivered: Int

  enum class Status {
    CREATED, SENT, DELIVERED, FAILED
  }

}