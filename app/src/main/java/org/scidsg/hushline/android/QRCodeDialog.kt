package org.scidsg.hushline.android

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class QRCodeDialog(private val bitmap: Bitmap, private val listener: DialogClickListener) : DialogFragment() {

    companion object {
        fun newInstance(bitmap: Bitmap, listener: DialogClickListener): QRCodeDialog {
            return QRCodeDialog(bitmap, listener)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_qr_code_preview, null)

        // For example, you can access the TextView and Button views and set their text or click listeners
        val qrImage = view.findViewById<ImageView>(R.id.qrcode)
        qrImage.setImageBitmap(bitmap)

        val closeBtn = view.findViewById<ImageView>(R.id.close)
        closeBtn.setOnClickListener {
            dialog?.dismiss()
            dialog?.let { dl -> listener.onNegativeButtonClick(dl) }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}