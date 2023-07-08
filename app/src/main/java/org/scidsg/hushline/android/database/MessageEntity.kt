package org.scidsg.hushline.android.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "messages")
@Parcelize
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val timestamp: String,
    val message: String,
    val read: Boolean
): Parcelable
