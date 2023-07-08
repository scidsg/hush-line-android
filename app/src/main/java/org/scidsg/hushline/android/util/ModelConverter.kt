package org.scidsg.hushline.android.util

import org.scidsg.hushline.android.database.MessageEntity
import org.scidsg.hushline.android.model.Message

object ModelConverter {

    fun convertToMessageEntity(message: Message) =
        MessageEntity(0, message.timestamp, message.message, message.read)

    fun convertToMessageEntity(messages: Array<Message>): Array<MessageEntity> {
        val m = mutableListOf<MessageEntity>()
        for (message in messages) {
            m.add(MessageEntity(0, message.timestamp, message.message, message.read))
        }
        return m.toTypedArray()
    }
}