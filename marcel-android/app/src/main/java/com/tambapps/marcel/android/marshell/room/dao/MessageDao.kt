package com.tambapps.marcel.android.marshell.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tambapps.marcel.android.marshell.room.entity.Message

@Dao
interface MessageDao {

  @Query("SELECT * FROM messages ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
  suspend fun listByRecency(limit: Int, offset: Int): List<Message>

  @Query("SELECT * FROM messages WHERE id = :id")
  suspend fun get(id: Long): Message

  @Query("SELECT * FROM messages WHERE id = :id")
  suspend fun findById(id: Long): Message?

  @Insert
  suspend fun insert(data: Message): Long

  @Update
  suspend fun update(data: Message)

  @Delete
  suspend fun delete(data: Message)

  @Query("DELETE FROM messages")
  suspend fun deleteAll()
}