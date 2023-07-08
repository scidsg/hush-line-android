package org.scidsg.hushline.android

import android.app.Dialog

interface DialogClickListener {

    fun onPositiveButtonClick(dialog: Dialog)

    fun onNegativeButtonClick(dialog: Dialog)
}