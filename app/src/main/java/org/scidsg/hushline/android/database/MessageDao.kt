package org.scidsg.hushline.android.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages ORDER BY id DESC")
    fun getAll(): LiveData<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE read = :read ORDER BY id DESC")
    fun getUnread(read: Boolean = false): LiveData<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE read = :read")
    suspend fun getUnreadCount(read: Boolean = false): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(messages: List<MessageEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun findByTimeRange(startDate: String, endDate: String): LiveData<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Delete
    suspend fun delete(message: MessageEntity)

    @Delete
    suspend fun delete(messages: List<MessageEntity>)
}