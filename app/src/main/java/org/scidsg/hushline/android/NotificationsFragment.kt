package org.scidsg.hushline.android

import android.Manifest
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.scidsg.hushline.android.common.C
import org.scidsg.hushline.android.databinding.CustomSwitch1Binding
import org.scidsg.hushline.android.databinding.CustomSwitchBinding
import org.scidsg.hushline.android.databinding.FragmentNotificationsBinding
import org.scidsg.hushline.android.vm.NotificationsViewModel
import java.util.regex.Pattern


@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var notificationManager: NotificationManager

    private lateinit var switchBinding1: CustomSwitchBinding
    private lateinit var switchBinding2: CustomSwitch1Binding

    private lateinit var sharedPrefs: SharedPreferences

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        switchBinding1 = binding.switchInclude1
        switchBinding2 = binding.switchInclude2

        sharedPrefs = requireContext().getSharedPreferences(C.SETTINGS_PREFERENCES, Context.MODE_PRIVATE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notificationsViewModel: NotificationsViewModel by viewModels()

        if (isNotificationPermissionGranted())
            switchCheck(switchBinding1)
        else switchUncheck(switchBinding1)

        switchBinding1.mainSwitch1.setOnClickListener {
            if (switchBinding1.switchThumbChecked.visibility == View.VISIBLE) {
                switchUncheck(switchBinding1)

                removeNotificationPermission()
            } else {
                switchCheck(switchBinding1)

                if (!isNotificationPermissionGranted())
                    requestNotificationPermission()
            }
        }

        switchBinding2.reuseSwitch1.setOnClickListener {
            if (switchBinding2.switchThumbChecked.visibility == View.VISIBLE) {
                switchUncheck(switchBinding2)

                notificationsViewModel.setEmailNotification(false)
                sharedPrefs.edit().putBoolean(C.EMAIL_NOTIFICATION, false).apply()

                //hide form
                binding.hiddenEmailSettings.visibility = View.GONE
            } else {
                switchCheck(switchBinding2)

                notificationsViewModel.setEmailNotification(true)
                sharedPrefs.edit().putBoolean(C.EMAIL_NOTIFICATION, true).apply()

                //show form
                binding.hiddenEmailSettings.visibility = View.VISIBLE
            }
        }

        notificationsViewModel.emailNotificationLiveData.observe(viewLifecycleOwner) {
            if (it !== null) {
                if (it.value.equals("true", true)) {
                    switchCheck(switchBinding2)
                    binding.hiddenEmailSettings.visibility = View.VISIBLE
                } else {
                    switchUncheck(switchBinding2)
                    binding.hiddenEmailSettings.visibility = View.GONE
                }
            } else {
                switchUncheck(switchBinding2)
                binding.hiddenEmailSettings.visibility = View.GONE
            }
        }

        notificationsViewModel.emailAddressLiveData.observe(viewLifecycleOwner) {
            if (it !== null) {
                //prevents textwatcher-database loop
                if (!it.value.equals(binding.emailAddressEdit.text.toString(), false))
                    binding.emailAddressEdit.setText(it.value)
            }
        }

        notificationsViewModel.smtpAddressLiveData.observe(viewLifecycleOwner) {
            if (it !== null) {
                //prevents textwatcher-database loop
                if (!it.value.equals(binding.smtpAddressEdit.text.toString(), false))
                    binding.smtpAddressEdit.setText(it.value)
            }
        }

        notificationsViewModel.passwordLiveData.observe(viewLifecycleOwner) {
            if (it !== null) {
                //prevents textwatcher-database loop
                if (!it.value.equals(binding.passwordEdit.text.toString(), false))
                    binding.passwordEdit.setText(it.value)
            }
        }

        notificationsViewModel.smtpPortLiveData.observe(viewLifecycleOwner) {
            if (it !== null) {
                //prevents textwatcher-database loop
                if (!it.value.equals(binding.smtpPortEdit.text.toString(), false))
                    binding.smtpPortEdit.setText(it.value)
            }
        }

        binding.emailAddressEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO check if email is valid to show an indicator
            }

            override fun afterTextChanged(s: Editable?) {
                val input = binding.emailAddressEdit.text.toString()
                if (Patterns.EMAIL_ADDRESS.matcher(input).matches())
                    notificationsViewModel.setEmailAddress(input)
                else Log.e("NotificationFragment", "Not a valid email")
            }
        })

        binding.smtpAddressEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO check if address is valid to show an indicator
            }

            override fun afterTextChanged(s: Editable?) {
                //TODO check if address is valid before saving
                val input = binding.smtpAddressEdit.text.toString()
                if (Patterns.DOMAIN_NAME.matcher(input).matches())
                    notificationsViewModel.setSMTPAddress(input)
                else Log.e("NotificationFragment", "Not a valid smtp address")
            }
        })

        binding.passwordEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val input = binding.passwordEdit.text.toString()
                if (input.isNotEmpty())
                    notificationsViewModel.setPassword(binding.passwordEdit.text.toString())
                else Log.e("NotificationFragment", "Empty password")
            }
        })

        binding.smtpPortEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO check if port is valid/number to show an indicator
            }

            override fun afterTextChanged(s: Editable?) {
                //TODO check if port is valid/number before saving
                val input = binding.smtpPortEdit.text.toString()
                //Pattern.compile("^[0-9]+\$").matcher(input).matches()
                if (Pattern.matches("^[0-9]+\$", input))
                    notificationsViewModel.setSMTPPort(input)
                else Log.e("NotificationFragment", "Not a valid port")
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun <T: ViewBinding> switchCheck(switch: T) {
        when(switch) {
            is CustomSwitchBinding -> {
                switch.switchThumbUnchecked.visibility = View.GONE
                switch.switchThumbChecked.visibility = View.VISIBLE

                switch.mainSwitch1.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.custom_switch_track_checked_shape, null
                )
            }

            is CustomSwitch1Binding -> {
                switch.switchThumbUnchecked.visibility = View.GONE
                switch.switchThumbChecked.visibility = View.VISIBLE

                switch.reuseSwitch1.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.custom_switch_track_checked_shape, null
                )
            }
        }
    }

    private fun <T: ViewBinding> switchUncheck(switch: T) {
        when(switch) {
            is CustomSwitchBinding -> {
                switch.switchThumbChecked.visibility = View.GONE
                switch.switchThumbUnchecked.visibility = View.VISIBLE

                switch.mainSwitch1.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.custom_switch_track_unchecked_shape, null
                )
            }

            is CustomSwitch1Binding -> {
                switch.switchThumbChecked.visibility = View.GONE
                switch.switchThumbUnchecked.visibility = View.VISIBLE

                switch.reuseSwitch1.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.custom_switch_track_unchecked_shape, null
                )
            }
        }
    }

    private fun isNotificationPermissionGranted() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else true

    private fun requestNotificationPermission() {
        when {
            ActivityCompat.checkSelfPermission(requireActivity(), POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.e("NotificationFragment", "Permission is granted?")
                return
            }
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), POST_NOTIFICATIONS) -> {
                Log.e("NotificationFragment", "Should show rationale?")
                //In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using the app without granting the permission.
                //TODO show dialog to explain why this permission is needed.
                NotificationPermissionRationaleDialog.newInstance(object : DialogClickListener {
                    override fun onPositiveButtonClick(dialog: Dialog) {
                        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    override fun onNegativeButtonClick(dialog: Dialog) {
                        dialog.dismiss()
                    }
                }).show(parentFragmentManager, "permission_rationale")
            }
            else -> {
                Log.e("NotificationFragment", "Request again?")
                manualPermissionSetting()
                //ActivityCompat.requestPermissions(requireActivity(),
                    //arrayOf(POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_CODE)
                //requestNotificationPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                switchUncheck(switchBinding1)
            }
        }

    private fun removeNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().revokeSelfPermissionOnKill(POST_NOTIFICATIONS)
        }
        val i = Intent()
        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:" + requireActivity().packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        requireActivity().startActivity(i)
        //revokeNotificationPermissionLauncher.launch(i)
    }

    private val revokeNotificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK)
                switchCheck(switchBinding1)
            else switchUncheck(switchBinding1)
        }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchCheck(switchBinding1)
            } else {

                //Now further we check if used denied permanently or not
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), POST_NOTIFICATIONS
                    )
                ) {
                    // 1. The user has temporarily denied permission.
                    switchUncheck(switchBinding1)
                } else {
                    // 2. Permission has been denied.
                    // From here, you can access the setting's page.
                    manualPermissionSetting()
                }
            }
        }
    }

    private fun manualPermissionSetting() {
        NotificationPermissionRationaleDialog.newInstance(object : DialogClickListener {
            override fun onPositiveButtonClick(dialog: Dialog) {
                val i = Intent()
                i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                i.addCategory(Intent.CATEGORY_DEFAULT)
                i.data = Uri.parse("package:" + requireActivity().packageName)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                requireActivity().startActivity(i)
            }

            override fun onNegativeButtonClick(dialog: Dialog) {
                dialog.dismiss()
            }
        }).show(parentFragmentManager, "permission_rationale")
    }

    companion object {
        const val NOTIFICATION_PERMISSION_CODE = 11
    }
}