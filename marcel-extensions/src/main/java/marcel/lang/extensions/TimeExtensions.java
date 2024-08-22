package marcel.lang.extensions;

import marcel.lang.compile.ExtensionClass;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// not loaded by default. Need explicit import extension
@ExtensionClass
public class TimeExtensions {

  public static Duration getDays(int $self) {
    return Duration.ofDays($self);
  }

  public static Duration getHours(int $self) {
    return Duration.ofHours($self);
  }

  public static Duration getMinutes(int $self) {
    return Duration.ofMinutes($self);
  }

  public static Duration getSeconds(int $self) {
    return Duration.ofSeconds($self);
  }

  public static Duration getMillis(int $self) {
    return Duration.ofDays($self);
  }

  public static Duration getNanos(int $self) {
    return Duration.ofNanos($self);
  }

  public static Duration getDays(Integer $self) {
    return Duration.ofDays($self);
  }

  public static Duration getHours(Integer $self) {
    return Duration.ofHours($self);
  }

  public static Duration getMinutes(Integer $self) {
    return Duration.ofMinutes($self);
  }

  public static Duration getSeconds(Integer $self) {
    return Duration.ofSeconds($self);
  }

  public static Duration getMillis(Integer $self) {
    return Duration.ofDays($self);
  }

  public static Duration getNanos(Integer $self) {
    return Duration.ofNanos($self);
  }

  public static Duration getDays(long $self) {
    return Duration.ofDays($self);
  }

  public static Duration getHours(long $self) {
    return Duration.ofHours($self);
  }

  public static Duration getMinutes(long $self) {
    return Duration.ofMinutes($self);
  }

  public static Duration getSeconds(long $self) {
    return Duration.ofSeconds($self);
  }

  public static Duration getMillis(long $self) {
    return Duration.ofDays($self);
  }

  public static Duration getNanos(long $self) {
    return Duration.ofNanos($self);
  }

  public static Duration getDays(Long $self) {
    return Duration.ofDays($self);
  }

  public static Duration getHours(Long $self) {
    return Duration.ofHours($self);
  }

  public static Duration getMinutes(Long $self) {
    return Duration.ofMinutes($self);
  }

  public static Duration getSeconds(Long $self) {
    return Duration.ofSeconds($self);
  }

  public static Duration getMillis(Long $self) {
    return Duration.ofDays($self);
  }

  public static Duration getNanos(Long $self) {
    return Duration.ofNanos($self);
  }

  public static long daysUntil(LocalDate $self, LocalDate other) {
    return ChronoUnit.DAYS.between($self, other);
  }

  /**
   * Format the LocalDateTime using the provided pattern
   * @param $self the LocalDateTime
   * @param pattern the pattern
   * @return the formatted LocalDateTime
   */
  public static String format(LocalDateTime $self, String pattern) {
    return $self.format(DateTimeFormatter.ofPattern(pattern));
  }
}
