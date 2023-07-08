package org.scidsg.hushline.android.database

import androidx.lifecycle.LiveData
import androidx.room.*
import org.scidsg.hushline.android.database.typeconverter.AnyTypeConverter

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings")
    suspend fun getAllSettings(): List<SettingsEntity>

    @Query("SELECT * FROM settings WHERE key = :key")
    fun getSetting(key: String): LiveData<SettingsEntity>

    @Query("UPDATE settings SET value = :value WHERE key = :key")
    //@TypeConverters(AnyTypeConverter::class)
    suspend fun setSetting(key: String, value: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: SettingsEntity)

    @Delete
    suspend fun delete(setting: SettingsEntity)
}