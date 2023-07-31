package org.scidsg.hushline.android.repo

import android.content.Context
import androidx.lifecycle.LiveData
import org.scidsg.hushline.android.R
import org.scidsg.hushline.android.database.MessageDao
import org.scidsg.hushline.android.database.MessageEntity
import org.scidsg.hushline.android.database.SettingsDao
import org.scidsg.hushline.android.util.ModelConverter
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao) {

    val unreadMessages: LiveData<List<MessageEntity>> = messageDao.getUnread()
    val allMessages: LiveData<List<MessageEntity>> = messageDao.getAll()

    suspend fun refreshMessages(context: Context) {
        //val response = apiService.getMessages() // Call network API to fetch data
        //val messages = response.messages // Extract the items from the response

        //messageDao.insertAll(ModelConverter.convertToMessageEntity(messages)) // Insert the items into the database

        //test
        messageDao.insertAll(test(context))
    }

    suspend fun markAsRead(messageEntity: MessageEntity) {
        messageDao.update(
            MessageEntity(messageEntity.id, messageEntity.timestamp, messageEntity.message, true)
        )
    }

    suspend fun notifyMessage(message: String): Boolean {
        //todo encrypt with pgp
        //send encrypted email
        return true
    }

    fun encryptMessage(message: String, pubKey: String): String {
        return ""
    }

    fun decryptMessage(message: String, privKey: String): String {
        return ""
    }

    fun emailEncryptedMessage(message: String): Boolean {
        return true
    }

    private fun test(context: Context): List<MessageEntity> {
        //test
        val dt = context.getString(R.string.subhead_sample)
        val ms = context.getString(R.string.supporting_text_full_sample)
        val ls = mutableListOf<MessageEntity>()
        ls.add(MessageEntity(0, dt, ms, false))
        ls.add(MessageEntity(0, dt, ms, false))
        ls.add(MessageEntity(0, dt, ms, true))
        ls.add(MessageEntity(0, dt, ms, true))
        //ls.add(MessageEntity(0, dt, ms, true))
        //ls.add(MessageEntity(0, dt, ms, true))
        //ls.add(MessageEntity(0, dt, ms, true))
        //ls.add(MessageEntity(0, dt, ms, true))

        return ls
    }
}