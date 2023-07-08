package org.scidsg.hushline.android.vm

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.scidsg.hushline.android.database.MessageEntity
import org.scidsg.hushline.android.repo.MessageRepository
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val applicationContext: Application
): ViewModel() {

    private val _unreadMessagesLiveData = repository.unreadMessages//MutableLiveData<List<Message>>()
    val unreadMessagesLiveData: LiveData<List<MessageEntity>> = _unreadMessagesLiveData

    private val _messageListLiveData = repository.allMessages//MutableLiveData<List<Message>>()
    val messageListLiveData: LiveData<List<MessageEntity>> = _messageListLiveData

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> = _loadingLiveData

    init {

    }

    fun markAsRead(messageEntity: MessageEntity) {
        viewModelScope.launch {
            repository.markAsRead(messageEntity)
        }
    }

    //test
    fun loadMessages() {
        _loadingLiveData.value = true
        // Simulate a delay or perform your asynchronous data loading operation
        //test
        Handler(Looper.getMainLooper()).postDelayed({
            viewModelScope.launch {

                repository.refreshMessages(applicationContext)
                _loadingLiveData.value = false
            }
        }, 2000)
    }

    /*private fun getMessagesFromRepository(context: Context): List<MessageEntity> {
        // Retrieve the list of items from your data source (e.g., database, API)
    }*/
}