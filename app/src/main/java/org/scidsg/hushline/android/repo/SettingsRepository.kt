package org.scidsg.hushline.android.repo

import androidx.lifecycle.LiveData
import org.scidsg.hushline.android.common.C
import org.scidsg.hushline.android.database.SettingsDao
import org.scidsg.hushline.android.database.SettingsEntity
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {
    val runningStatus: LiveData<SettingsEntity> = settingsDao.getSetting(C.HUSHLINE_STATUS)
    val reuseStatus: LiveData<SettingsEntity> = settingsDao.getSetting(C.REUSE_ONION_ADDRESS)
    val emailNotification: LiveData<SettingsEntity> = settingsDao.getSetting(C.EMAIL_NOTIFICATION)
    val emailAddressData: LiveData<SettingsEntity> = settingsDao.getSetting(C.EMAIL_ADDRESS)
    val smtpAddressData: LiveData<SettingsEntity> = settingsDao.getSetting(C.SMTP_ADDRESS)
    val passwordData: LiveData<SettingsEntity> = settingsDao.getSetting(C.PASSWORD)
    val smtpPortData: LiveData<SettingsEntity> = settingsDao.getSetting(C.SMTP_PORT)
    val rotatePGP: LiveData<SettingsEntity> = settingsDao.getSetting(C.ROTATE_PGP)

    suspend fun setRunningStatus(value: Boolean) {
        settingsDao.insert(SettingsEntity(0, C.HUSHLINE_STATUS, "$value"))
    }

    suspend fun setReuseStatus(value: Boolean) {
        settingsDao.insert(SettingsEntity(0, C.REUSE_ONION_ADDRESS, "$value"))
    }

    suspend fun setEmailNotification(value: Boolean) {
        settingsDao.insert(SettingsEntity(0, C.EMAIL_NOTIFICATION, "$value"))
    }

    suspend fun setEmailAddress(value: String) {
        settingsDao.insert(SettingsEntity(0, C.EMAIL_ADDRESS, value))
    }

    suspend fun setSMTPAddress(value: String) {
        settingsDao.insert(SettingsEntity(0, C.SMTP_ADDRESS, value))
    }

    suspend fun setPassword(value: String) {
        settingsDao.insert(SettingsEntity(0, C.PASSWORD, value))
    }

    suspend fun setSMTPPort(value: String) {
        settingsDao.insert(SettingsEntity(0, C.SMTP_PORT, value))
    }

    suspend fun setRotatePGP(value: Boolean) {
        settingsDao.insert(SettingsEntity(0, C.ROTATE_PGP, "$value"))
    }
}