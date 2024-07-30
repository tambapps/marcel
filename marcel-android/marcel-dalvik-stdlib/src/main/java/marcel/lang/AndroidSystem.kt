package marcel.lang

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
}
