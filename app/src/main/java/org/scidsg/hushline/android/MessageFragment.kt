package org.scidsg.hushline.android

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.scidsg.hushline.android.database.MessageEntity
import org.scidsg.hushline.android.databinding.FragmentMessageBinding
import org.scidsg.hushline.android.vm.MessagesViewModel

@AndroidEntryPoint
class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ViewModel using by viewModels() extension function
        val messageViewModel: MessagesViewModel by viewModels()

        // Retrieve the data from arguments Bundle
        val arguments = arguments
        arguments?.let { bundle ->
            val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(MessageListFragment.MESSAGE, MessageEntity::class.java) as MessageEntity
            } else {
                bundle.getParcelable(MessageListFragment.MESSAGE) as MessageEntity?
            }

            message?.let { m ->
                binding.messageDate.text = m.timestamp
                binding.messagePreview.text = m.message

                if (!m.read)
                    messageViewModel.markAsRead(m)
            } ?: {
                Toast.makeText(requireContext(), R.string.error_displaying_message, Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}