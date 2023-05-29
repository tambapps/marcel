package com.tambapps.marcel.android.marshell.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomWarnings
import androidx.room.Update
import androidx.room.Upsert
import com.tambapps.marcel.android.marshell.room.entity.CacheableScript

@Dao
interface CacheableScriptDao {

  companion object {
    // we doesn't fetch scriptText and logs for list endpoint
    const val MAIN_COLUMNS = "name, hash, script_class_name"
  }

  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  // we don't fetch scriptText and cachedJar for list endpoint
  @Query("SELECT name, hash, script_class_name FROM cacheable_scripts")
  suspend fun findAll(): List<CacheableScript>

  // we don't fetch cachedJar
  @Query("SELECT name, text, hash, script_class_name FROM cacheable_scripts WHERE name = :name")
  suspend fun findByName(name: String): CacheableScript?

  @Query("SELECT EXISTS (SELECT * FROM cacheable_scripts WHERE name = :name)")
  suspend fun existsByName(name: String): Boolean

  @Upsert
  suspend fun upsert(data: CacheableScript)
  @Insert
  suspend fun insert(data: CacheableScript)

  @Update
  suspend fun update(data: CacheableScript)

  @Delete
  suspend fun delete(data: CacheableScript)
}