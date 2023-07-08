package org.scidsg.hushline.android.database

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MessageEntity::class, SettingsEntity::class], version = 1)
abstract class HushlineDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun settingsDao(): SettingsDao

    companion object {

        @Volatile
        private var INSTANCE: HushlineDatabase? = null

        fun getInstance(context: Context): HushlineDatabase {
            // Multiple threads can ask for the database at the same time, ensure we only initialize
            // it once by using synchronized. Only one thread may enter a synchronized block at a
            // time.
            synchronized(this) {

                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        HushlineDatabase::class.java,
                        "hushline_db"
                    ).fallbackToDestructiveMigration() //TODO proper migration
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}