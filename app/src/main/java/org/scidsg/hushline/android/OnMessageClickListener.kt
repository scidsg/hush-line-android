package org.scidsg.hushline.android

import org.scidsg.hushline.android.database.MessageEntity

interface OnMessageClickListener {

    fun onMessageClick(message: MessageEntity, position: Int)
}