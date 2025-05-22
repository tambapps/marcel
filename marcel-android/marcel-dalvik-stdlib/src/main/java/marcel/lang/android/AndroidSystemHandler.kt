package marcel.lang.android

import java.util.concurrent.ThreadLocalRandom

interface AndroidSystemHandler {

  fun notify(title: String?, message: String?) {
    notify(ThreadLocalRandom.current().nextInt(), title, message)
  }

  fun notify(id: Int, title: String?, message: String?)

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
  fun listSms(page: Int, pageSize: Int): List<AndroidMessage>
}
