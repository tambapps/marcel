package marcel.lang.android

import marcel.lang.compile.IntDefaultValue
import marcel.lang.compile.StringDefaultValue
import java.util.concurrent.ThreadLocalRandom
// using kotlin to benefit from javaParameters (see build.gradle)
interface AndroidSystem {

  fun notify(
    @StringDefaultValue("Marshell notification") title: String?,
    @StringDefaultValue message: String?
  ) {
    notify(ThreadLocalRandom.current().nextInt(), title, message)
  }

  fun notify(
    id: Int,
    @StringDefaultValue("Marshell notification") title: String?,
    @StringDefaultValue message: String?
  )

  /**
   * Sends an SMS and returns a view of the sent message. The information retrieved from this returned
   * message will always be up to date.
   */
  fun sendSms(destinationAddress: String, text: String): AndroidMessage

  /**
   * List all the messages. Not that as opposed to send SMS, the messages returned are not a view they are
   * represent the state of the message had when this method was called. Information retrieved from those message
   * may not be up to date
   */
  fun listSms(@IntDefaultValue(value = 0) page: Int, @IntDefaultValue(value = 10) pageSize: Int): List<AndroidMessage>
}
