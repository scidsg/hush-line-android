package org.scidsg.hushline.android

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.briarproject.android.dontkillmelib.DozeUtils
import org.scidsg.hushline.android.common.C
import org.scidsg.hushline.android.databinding.CustomSwitch1Binding
import org.scidsg.hushline.android.databinding.CustomSwitchBinding
import org.scidsg.hushline.android.databinding.FragmentHomeBinding
import org.scidsg.hushline.android.vm.HomeViewModel
import org.scidsg.hushline.android.vm.MessagesViewModel


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var switchBinding1: CustomSwitchBinding
    private lateinit var switchBinding2: CustomSwitch1Binding

    private lateinit var sharedPrefs: SharedPreferences
    //private val homeViewModel: HomeViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        switchBinding1 = binding.switchInclude1
        switchBinding2 = binding.switchInclude2

        sharedPrefs =
                requireContext().getSharedPreferences(C.SETTINGS_PREFERENCES, Context.MODE_PRIVATE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ViewModel using by viewModels() extension function
        val messageViewModel: MessagesViewModel by viewModels()

        binding.newMessageIndicator.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_MessageListFragment)
        }

        switchBinding1.mainSwitch1.setOnClickListener {
            if (switchBinding1.switchThumbChecked.visibility == View.VISIBLE) {
                switchUncheck(switchBinding1)

                //TODO stop tor
                //if (isRunningIndicatorHidden(binding.hushlineRunningIndicator))
                hideRunningIndicator(binding.hushlineRunningIndicator)
                binding.newMessageIndicator.visibility = View.GONE

                homeViewModel.setRunningStatus(false)
                sharedPrefs.edit().putBoolean(C.HUSHLINE_STATUS, false).apply()

            } else {
                //if (isRunningIndicatorShown(binding.hushlineRunningIndicator))
                showRunningIndicator(binding.hushlineRunningIndicator)
                checkBeforeLaunch()
            }
        }

        switchBinding2.reuseSwitch1.setOnClickListener {
            if (switchBinding2.switchThumbChecked.visibility == View.VISIBLE) {
                switchUncheck(switchBinding2)

                homeViewModel.setReuseStatus(false)
                sharedPrefs.edit().putBoolean(C.REUSE_ONION_ADDRESS, false).apply()
            } else {
                switchCheck(switchBinding2)

                homeViewModel.setReuseStatus(true)
                sharedPrefs.edit().putBoolean(C.REUSE_ONION_ADDRESS, true).apply()
            }
        }

        binding.shareAddress.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, binding.onionAddress.text.toString()
                .replace("\n", ""))
            startActivity(Intent.createChooser(shareIntent, requireActivity().getString(R.string.share_dialog_title)))
        }

        binding.viewQRCode.setOnClickListener {
            try {
                val qrCodeBitmap = generateQRCode(binding.onionAddress.text.toString()
                    .replace("\n", ""), 370, 370)
                qrCodeBitmap?.let {
                    QRCodeDialog.newInstance(qrCodeBitmap, object : DialogClickListener {
                        override fun onPositiveButtonClick(dialog: Dialog) {
                        }

                        override fun onNegativeButtonClick(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    }).show(parentFragmentManager, "qr_code")
                }
            } catch (e: WriterException) {
                Toast.makeText(requireContext(), R.string.qr_code_failed, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        binding.mainInstruction.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.torproject.org"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.android.chrome")

            try {
                startActivity(intent)
            } catch (e: Exception) {
                // If Chrome is not available, open the link in the default browser
                intent.setPackage(null)
                startActivity(intent)
            }
        }

        homeViewModel.runningStatusLiveData.observe(viewLifecycleOwner) {
            if (it !== null) {
                if (it.value.equals("true", true)) {
                    //switchCheck(switchBinding1)
                    //showRunningIndicator(binding.hushlineRunningIndicator)
                    //binding.newMessageIndicator.visibility = View.VISIBLE

                    //todo check if tor & webserver is already running
                    //checkBeforeLaunch()
                } else {
                    switchUncheck(switchBinding1)
                    hideRunningIndicator(binding.hushlineRunningIndicator)
                    binding.newMessageIndicator.visibility = View.GONE
                }
            } else {
                switchUncheck(switchBinding1)
                hideRunningIndicator(binding.hushlineRunningIndicator)
                binding.newMessageIndicator.visibility = View.GONE
            }
        }
        //checkPrefRunningStatus()

        homeViewModel.reuseAddressStatusLiveData.observe(viewLifecycleOwner) {
            if (it !== null) {
                if (it.value.equals("true", true))
                    switchCheck(switchBinding2)
                else
                    switchUncheck(switchBinding2)
            } else
                switchUncheck(switchBinding2)
        }

        lifecycleScope.launch {
            homeViewModel.runState.collect {
                when (it) {
                    is UIState.Starting -> {
                        binding.coloredCircle.background = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.colored_circle_shape_orange, null
                        )
                        binding.hushlineRunningText.text = getString(R.string.hushline_starting)
                        disableIndicatorViews()
                        showRunningIndicator(binding.hushlineRunningIndicator)
                    }
                    is UIState.Started -> {
                        enableIndicatorViews()
                        binding.coloredCircle.background = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.colored_circle_shape_green, null
                        )
                        binding.hushlineRunningText.text = getString(R.string.hushline_is_running)
                        binding.onionAddress.text = it.onionAddress//"http://${it.onion}.onion"
                        switchCheck(switchBinding1)
                        binding.newMessageIndicator.visibility = View.VISIBLE

                        homeViewModel.setRunningStatus(true)
                        //sharedPrefs.edit().putBoolean(C.HUSHLINE_STATUS, true).apply()
                    }
                    is UIState.Stopping -> {
                        binding.coloredCircle.background = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.colored_circle_shape_orange, null
                        )
                        binding.hushlineRunningText.text = getString(R.string.hushline_stopping)
                        disableIndicatorViews()
                    }
                    is UIState.Stopped -> {
                        binding.coloredCircle.background = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.colored_circle_shape_red, null
                        )
                        binding.hushlineRunningText.text = getString(R.string.hushline_stopped)
                        enableIndicatorViews()
                        switchUncheck(switchBinding1)
                        binding.newMessageIndicator.visibility = View.VISIBLE
                        homeViewModel.setRunningStatus(false)
                        hideRunningIndicator(binding.hushlineRunningIndicator)
                    }
                    is UIState.Error -> {
                        binding.coloredCircle.background = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.colored_circle_shape_red, null
                        )
                        binding.hushlineRunningText.text = getString(R.string.hushline_error)
                        enableIndicatorViews()
                        switchUncheck(switchBinding1)
                        binding.newMessageIndicator.visibility = View.VISIBLE
                        homeViewModel.setRunningStatus(false)
                        hideRunningIndicator(binding.hushlineRunningIndicator)
                    }
                }
            }
        }

        messageViewModel.unreadMessagesLiveData.observe(viewLifecycleOwner) {
            if (it !== null) {
                if (it.isNotEmpty()) {
                    binding.newMessageCountContainer.visibility = View.VISIBLE
                    binding.newMessageCount.text = it.size.toString()
                } else binding.newMessageCountContainer.visibility = View.GONE
            } else binding.newMessageCountContainer.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun <T: ViewBinding> animateSwitchCheck(switch: T) {
        when(switch) {
            is CustomSwitchBinding -> {
                switch.switchThumbUnchecked.visibility = View.GONE
                switch.switchThumbChecked.visibility = View.VISIBLE
                val animation: Animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_right)
                switch.switchThumbChecked.startAnimation(animation)

                switch.mainSwitch1.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.custom_switch_track_checked_shape, null
                )
            }

            is CustomSwitch1Binding -> {
                switch.switchThumbUnchecked.visibility = View.GONE
                switch.switchThumbChecked.visibility = View.VISIBLE
                val animation: Animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_right)
                switch.switchThumbChecked.startAnimation(animation)

                switch.reuseSwitch1.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.custom_switch_track_checked_shape, null
                )
            }
        }
    }

    private fun <T: ViewBinding> animateSwitchUncheck(switch: T) {
        when(switch) {
            is CustomSwitchBinding -> {
                switch.switchThumbChecked.visibility = View.GONE
                switch.switchThumbUnchecked.visibility = View.VISIBLE
                val animation: Animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_left)
                switch.switchThumbUnchecked.startAnimation(animation)

                switch.mainSwitch1.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.custom_switch_track_unchecked_shape, null
                )
            }

            is CustomSwitch1Binding -> {
                switch.switchThumbChecked.visibility = View.GONE
                switch.switchThumbUnchecked.visibility = View.VISIBLE
                val animation: Animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_left)
                switch.switchThumbUnchecked.startAnimation(animation)

                switch.reuseSwitch1.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.custom_switch_track_unchecked_shape, null
                )
            }
        }
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

    private fun <T: View> hideRunningIndicator(view: T) {
        if (view.visibility != View.GONE) {
            view.visibility = View.GONE
            val animation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.slide_down)
            view.startAnimation(animation)
        }
    }

    private fun <T: View> showRunningIndicator(view: T) {
        if (view.visibility != View.VISIBLE) {
            view.visibility = View.VISIBLE
            val animation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.slide_up)
            view.startAnimation(animation)
            checkPrefAddressReuseStatus()
        }
    }

    private fun <T: View> isRunningIndicatorHidden(view: T): Boolean {
        return view.visibility == View.GONE
    }

    private fun <T: View> isRunningIndicatorShown(view: T): Boolean {
        return view.visibility == View.VISIBLE
    }

    private fun checkPrefRunningStatus() {
        val status = sharedPrefs.getBoolean(C.HUSHLINE_STATUS, false)
        if (status) {
            switchCheck(switchBinding1)
            showRunningIndicator(binding.hushlineRunningIndicator)
        } else {
            switchUncheck(switchBinding1)
            hideRunningIndicator(binding.hushlineRunningIndicator)
        }
    }

    private fun checkPrefAddressReuseStatus() {
        val status = sharedPrefs.getBoolean(C.REUSE_ONION_ADDRESS, false)
        if (status) {
            switchCheck(switchBinding2)
        } else switchUncheck(switchBinding2)
    }

    @Throws(WriterException::class)
    private fun generateQRCode(text: String, width: Int, height: Int): Bitmap? {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix: BitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        return bitmap
    }

    private fun disableIndicatorViews() {
        binding.mainInstruction.isEnabled = false
        binding.mainInstruction.alpha = 0.5f
        binding.reuseAddressContainer.isEnabled = false
        binding.reuseAddressContainer.alpha = 0.5f
        binding.shareAddress.isEnabled = false
        binding.shareAddress.alpha = 0.5f
        binding.viewQRCode.isEnabled = false
        binding.viewQRCode.alpha = 0.5f
        binding.switchInclude1.mainSwitch1.isEnabled = false
        binding.switchInclude1.mainSwitch1.alpha = 0.5f
    }

    private fun enableIndicatorViews() {
        binding.mainInstruction.isEnabled = true
        binding.mainInstruction.alpha = 1.0f
        binding.reuseAddressContainer.isEnabled = true
        binding.reuseAddressContainer.alpha = 1.0f
        binding.shareAddress.isEnabled = true
        binding.shareAddress.alpha = 1.0f
        binding.viewQRCode.isEnabled = true
        binding.viewQRCode.alpha = 1.0f
        binding.switchInclude1.mainSwitch1.isEnabled = true
        binding.switchInclude1.mainSwitch1.alpha = 1.0f
    }

    private val batteryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult ->
        // we just ignore the result and don't check for battery optimization again
        // assuming the user will understand if they didn't allow background
        // TODO we might want to do user testing here to see if the assumption holds
        homeViewModel.runHushLine()
    }

    private fun checkBeforeLaunch() {
        if (homeViewModel.needsDozeWhitelisting
        ) {
            try {
                batteryLauncher.launch(DozeUtils.getDozeWhitelistingIntent(requireContext()))
            } catch (e: ActivityNotFoundException) {
                // this is really unusual (happened once on a Samsung Galaxy A5 with SDK 23), just pray and proceed
                homeViewModel.runHushLine()
            }
        } else {
            homeViewModel.runHushLine()
        }
    }
}