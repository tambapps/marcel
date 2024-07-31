package com.tambapps.marcel.android.marshell.os

import android.Manifest
import android.telephony.SmsManager
import com.tambapps.marcel.android.marshell.service.PermissionManager

class AndroidSmsSender(
  private val smsManager: SmsManager,
) {

  fun sendSms(destinationAddress: String, text: String) {

    // TODO handle delivery intent to listen to success or failure
    val parts = smsManager.divideMessage(text)
    if (parts.size == 1) {
      smsManager.sendTextMessage(destinationAddress, null, parts.first(), null, null)
    } else {
      smsManager.sendMultipartTextMessage(destinationAddress, null, parts, null, null)
    }
  }
}