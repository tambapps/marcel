package marcel.lang.extensions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeMarcelMethods {

    /**
     * Format the LocalDateTime using the provided pattern
     * @param self the LocalDateTime
     * @param pattern the pattern
     * @return the formatted LocalDateTime
     */
    public static String format(LocalDateTime self, String pattern) {
        return self.format(DateTimeFormatter.ofPattern(pattern));
    }

}
