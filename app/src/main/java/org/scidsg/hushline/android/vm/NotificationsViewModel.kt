package org.scidsg.hushline.android.vm

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.scidsg.hushline.android.database.SettingsEntity
import org.scidsg.hushline.android.repo.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val applicationContext: Application
): ViewModel() {

    private val _emailNotificationLiveData = repository.emailNotification
    val emailNotificationLiveData: LiveData<SettingsEntity> = _emailNotificationLiveData

    private val _emailAddressLiveData = repository.emailAddressData
    val emailAddressLiveData: LiveData<SettingsEntity> = _emailAddressLiveData

    private val _smtpAddressLiveData = repository.smtpAddressData
    val smtpAddressLiveData: LiveData<SettingsEntity> = _smtpAddressLiveData

    private val _passwordLiveData = repository.passwordData
    val passwordLiveData: LiveData<SettingsEntity> = _passwordLiveData

    private val _smtpPortLiveData = repository.smtpPortData
    val smtpPortLiveData: LiveData<SettingsEntity> = _smtpPortLiveData

    fun setEmailNotification(value: Boolean) {
        viewModelScope.launch {
            repository.setEmailNotification(value)
        }
    }

    fun setEmailAddress(value: String) {
        viewModelScope.launch {
            repository.setEmailAddress(value)
        }
    }

    fun setSMTPAddress(value: String) {
        viewModelScope.launch {
            repository.setSMTPAddress(value)
        }
    }

    fun setPassword(value: String) {
        viewModelScope.launch {
            repository.setPassword(value)
        }
    }

    fun setSMTPPort(value: String) {
        viewModelScope.launch {
            repository.setSMTPPort(value)
        }
    }
}