package org.scidsg.hushline.android

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        switchBinding1 = binding.switchInclude1
        switchBinding2 = binding.switchInclude2

        sharedPrefs = requireContext().getSharedPreferences(C.SETTINGS_PREFERENCES, Context.MODE_PRIVATE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ViewModel using by viewModels() extension function
        val messageViewModel: MessagesViewModel by viewModels()
        val homeViewModel: HomeViewModel by viewModels()

        binding.newMessageIndicator.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_MessageListFragment)
        }

        switchBinding1.mainSwitch1.setOnClickListener {
            if (switchBinding1.switchThumbChecked.visibility == View.VISIBLE) {
                switchUncheck(switchBinding1)

                hideRunningIndicator(binding.hushlineRunningIndicator)
                binding.newMessageIndicator.visibility = View.GONE

                homeViewModel.setRunningStatus(false)
                sharedPrefs.edit().putBoolean(C.HUSHLINE_STATUS, false).apply()
            } else {
                switchCheck(switchBinding1)

                showRunningIndicator(binding.hushlineRunningIndicator)
                binding.newMessageIndicator.visibility = View.VISIBLE

                homeViewModel.setRunningStatus(true)
                sharedPrefs.edit().putBoolean(C.HUSHLINE_STATUS, true).apply()
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
            shareIntent.putExtra(Intent.EXTRA_TEXT, "onion address")
            startActivity(Intent.createChooser(shareIntent, requireActivity().getString(R.string.share_dialog_title)))
        }

        binding.viewQRCode.setOnClickListener {
            try {
                val qrCodeBitmap = generateQRCode("onion address", 370, 370)
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

        homeViewModel.runningStatusLiveData.observe(viewLifecycleOwner) {
            if (it !== null) {
                if (it.value.equals("true", true)) {
                    switchCheck(switchBinding1)
                    showRunningIndicator(binding.hushlineRunningIndicator)
                    binding.newMessageIndicator.visibility = View.VISIBLE
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
}