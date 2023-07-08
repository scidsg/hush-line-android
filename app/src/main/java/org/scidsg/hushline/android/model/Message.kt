package org.scidsg.hushline.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(val timestamp: String,
                   val message: String,
                   val read: Boolean) : Parcelable
