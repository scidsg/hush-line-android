package org.scidsg.hushline.android

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.scidsg.hushline.android.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notificationBtn.setOnClickListener {
            findNavController().navigate(R.id.action_SettingsFragment_to_NotificationsFragment)
        }

        binding.encryptionBtn.setOnClickListener {
            findNavController().navigate(R.id.action_SettingsFragment_to_EncryptionFragment)
        }

        binding.aboutBtn.setOnClickListener {
            findNavController().navigate(R.id.action_SettingsFragment_to_AboutFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}