package marcel.lang.android

import marcel.lang.compile.IntDefaultValue
import marcel.lang.compile.StringDefaultValue

object AndroidSystem {

  private var instance: AndroidSystemHandler? = null

  @JvmStatic
  fun init(instance: AndroidSystemHandler) {
    AndroidSystem.instance = instance
  }

  @JvmStatic
  fun notify(
    @StringDefaultValue("Marshell notification") title: String?,
    @StringDefaultValue message: String?
  ) = instance!!.notify(title, message)

  @JvmStatic
  fun notify(
    id: Int,
    @StringDefaultValue("Marshell notification") title: String?,
    @StringDefaultValue message: String?
  ) = instance!!.notify(id, title, message)

  @JvmStatic
  fun sendSms(destinationAddress: String, text: String) = instance!!.sendSms(destinationAddress, text)

  @JvmStatic
  fun listSms(@IntDefaultValue(value = 0) page: Int, @IntDefaultValue(value = 10) pageSize: Int) = instance!!.listSms(page, pageSize)

}
