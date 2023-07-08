package org.scidsg.hushline.android

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.scidsg.hushline.android.common.C
import org.scidsg.hushline.android.databinding.CustomSwitch1Binding
import org.scidsg.hushline.android.databinding.CustomSwitchBinding
import org.scidsg.hushline.android.databinding.FragmentEncryptionBinding
import org.scidsg.hushline.android.vm.EncryptionViewModel

@AndroidEntryPoint
class EncryptionFragment : Fragment() {

    private var _binding: FragmentEncryptionBinding? = null

    private lateinit var switchBinding1: CustomSwitchBinding

    private lateinit var sharedPrefs: SharedPreferences

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEncryptionBinding.inflate(inflater, container, false)
        switchBinding1 = binding.switchInclude1

        sharedPrefs = requireContext().getSharedPreferences(C.SETTINGS_PREFERENCES, Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val encryptionViewModel: EncryptionViewModel by viewModels()

        switchBinding1.mainSwitch1.setOnClickListener {
            if (switchBinding1.switchThumbChecked.visibility == View.VISIBLE) {
                switchUncheck(switchBinding1)
                binding.rotatePGPKeysContainer.alpha = 0.5f

                encryptionViewModel.setRotatePGP(false)
                sharedPrefs.edit().putBoolean(C.ROTATE_PGP, false).apply()
            } else {
                switchCheck(switchBinding1)
                binding.rotatePGPKeysContainer.alpha = 1.0f

                encryptionViewModel.setRotatePGP(true)
                sharedPrefs.edit().putBoolean(C.ROTATE_PGP, true).apply()
            }
        }

        encryptionViewModel.rotatePGPLiveData.observe(viewLifecycleOwner) {
            if (it !== null) {
                if (it.value.equals("true", true)) {
                    switchCheck(switchBinding1)
                    binding.rotatePGPKeysContainer.alpha = 1.0f
                } else {
                    switchUncheck(switchBinding1)
                    binding.rotatePGPKeysContainer.alpha = 0.5f
                }
            } else {
                switchUncheck(switchBinding1)
                binding.rotatePGPKeysContainer.alpha = 0.5f
            }
        }

        binding.uploadCustomPGPText.setOnClickListener {

        }

        binding.learnMore.setOnClickListener {
            val url = "https://www.openpgp.org/about/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            requireActivity().startActivity(intent)
        }
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
}