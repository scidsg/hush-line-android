package org.scidsg.hushline.android

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class NotificationPermissionRationaleDialog(private val listener: DialogClickListener) : DialogFragment() {

    companion object {
        fun newInstance(listener: DialogClickListener): NotificationPermissionRationaleDialog {
            return NotificationPermissionRationaleDialog(listener)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_permission_rationale, null)


        val okBtn = view.findViewById<Button>(R.id.ok)
        okBtn.setOnClickListener {
            dialog?.dismiss()
            dialog?.let { dl -> listener.onPositiveButtonClick(dl) }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}