package org.scidsg.hushline.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.scidsg.hushline.android.database.HushlineDatabase
import org.scidsg.hushline.android.database.MessageDao
import org.scidsg.hushline.android.database.SettingsDao

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideMessageDao(@ApplicationContext appContext: Context) : MessageDao {
        return HushlineDatabase.getInstance(appContext).messageDao()
    }

    @Provides
    fun provideSettingsDao(@ApplicationContext appContext: Context) : SettingsDao {
        return HushlineDatabase.getInstance(appContext).settingsDao()
    }
}