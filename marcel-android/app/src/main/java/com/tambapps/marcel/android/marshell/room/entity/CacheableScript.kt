package com.tambapps.marcel.android.marshell.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("cacheable_scripts")
data class CacheableScript(
  @PrimaryKey val name: String,
  @ColumnInfo val text: String?,
  @ColumnInfo val hash: String,
  @ColumnInfo("script_class_name") val scriptClassName: String?,
  @ColumnInfo(name = "cached_jar", typeAffinity = ColumnInfo.BLOB) val cachedJar: ByteArray?
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as CacheableScript

    if (name != other.name) return false
    if (hash != other.hash) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + hash.hashCode()
    return result
  }
}
