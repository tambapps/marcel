package com.tambapps.marcel.android.marshell.work

import com.tambapps.marcel.android.marshell.ui.shellwork.form.PeriodUnit
import java.time.LocalDateTime

object WorkTags {

  const val SHELL_WORK_TYPE = "shell_work"
  private const val NAME_TAG_PREFIX = "name:"
  private const val DESCRIPTION_TAG_PREFIX = "description:"
  private const val TYPE_TAG_PREFIX = "type:"
  private const val SCRIPT_PATH_TAG_PREFIX = "script_path:"
  private const val SCHEDULE_TAG_PREFIX = "schedule:"
  private const val SILENT_TAG_PREFIX = "silent:"
  private const val NETWORK_REQUIRED_TAG_PREFIX = "network_required:"
  private const val PERIOD_AMOUNT_PREFIX = "periodAmount:"
  private const val PERIOD_UNIT_PREFIX = "periodUnit:"

  fun name(name: String): String {
    return NAME_TAG_PREFIX + name
  }

  fun description(description: String): String {
    return DESCRIPTION_TAG_PREFIX + description
  }

  fun type(type: String): String {
    return TYPE_TAG_PREFIX + type
  }

  fun scriptPath(name: String): String {
    return SCRIPT_PATH_TAG_PREFIX + name
  }

  fun schedule(schedule: String): String {
    return SCHEDULE_TAG_PREFIX + schedule
  }

  fun periodAmount(period: Long): String {
    return PERIOD_AMOUNT_PREFIX + period
  }

  fun periodUnit(period: PeriodUnit): String {
    return PERIOD_UNIT_PREFIX + period
  }

  fun silent(silent: Boolean): String {
    return SILENT_TAG_PREFIX + if (silent) "TRUE" else "FALSE"
  }

  fun networkRequired(networkRequired: Boolean): String {
    return NETWORK_REQUIRED_TAG_PREFIX + if (networkRequired) "TRUE" else "FALSE"
  }

  fun getName(tags: Set<String>): String? {
    return get(tags, NAME_TAG_PREFIX)
  }

  fun getDescription(tags: Set<String>): String? {
    return get(tags, DESCRIPTION_TAG_PREFIX)
  }

  fun getScriptPath(tags: Set<String>): String? {
    return get(tags, SCRIPT_PATH_TAG_PREFIX)
  }

  fun getSchedule(tags: Set<String>): LocalDateTime? {
    val s = get(tags, SCHEDULE_TAG_PREFIX)
    return if (s != null) LocalDateTime.parse(s) else null
  }

  fun getSilent(tags: Set<String>): Boolean {
    return "TRUE" == get(tags, SILENT_TAG_PREFIX)
  }

  fun getNetworkRequired(tags: Set<String>): Boolean {
    return "TRUE" == get(tags, NETWORK_REQUIRED_TAG_PREFIX)
  }

  fun getPeriodAmount(tags: Set<String>): Long? {
    return get(tags, PERIOD_AMOUNT_PREFIX)?.toLong()
  }

  fun getPeriodUnit(tags: Set<String>): PeriodUnit? {
    val s = get(tags, PERIOD_UNIT_PREFIX)
    return if (s != null) PeriodUnit.valueOf(s) else null
  }

  private fun get(tags: Set<String>, tagPrefix: String): String? {
    return tags.find { it.startsWith(tagPrefix) }?.substring(tagPrefix.length)
  }
}