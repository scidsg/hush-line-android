package org.scidsg.hushline.android.vm

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.scidsg.hushline.android.database.SettingsEntity
import org.scidsg.hushline.android.repo.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val applicationContext: Application
): ViewModel() {
    private val _runningStatusLiveData = repository.runningStatus
    val runningStatusLiveData: LiveData<SettingsEntity> = _runningStatusLiveData

    private val _reuseAddressStatusLiveData = repository.reuseStatus
    val reuseAddressStatusLiveData: LiveData<SettingsEntity> = _reuseAddressStatusLiveData

    fun setRunningStatus(value: Boolean) {
        viewModelScope.launch {
            repository.setRunningStatus(value)
        }
    }

    fun setReuseStatus(value: Boolean) {
        viewModelScope.launch {
            repository.setReuseStatus(value)
        }
    }
}