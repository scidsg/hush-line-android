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
class EncryptionViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val applicationContext: Application
): ViewModel() {

    private val _rotatePGPLiveData = repository.rotatePGP
    val rotatePGPLiveData: LiveData<SettingsEntity> = _rotatePGPLiveData

    fun setRotatePGP(value: Boolean) {
        viewModelScope.launch {
            repository.setRotatePGP(value)
        }
    }
}