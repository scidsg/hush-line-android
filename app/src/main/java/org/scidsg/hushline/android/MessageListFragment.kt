package org.scidsg.hushline.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.scidsg.hushline.android.adapter.MessageItemAdapter
import org.scidsg.hushline.android.database.MessageEntity
import org.scidsg.hushline.android.databinding.FragmentMessageListBinding
import org.scidsg.hushline.android.vm.MessagesViewModel

@AndroidEntryPoint
class MessageListFragment : Fragment(), OnMessageClickListener {

    private var _binding: FragmentMessageListBinding? = null
    private lateinit var messageItemAdapter: MessageItemAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMessageListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageItemAdapter = MessageItemAdapter(requireContext(), mutableListOf<MessageEntity>(),
            this)
        binding.messagesList.adapter = messageItemAdapter

        // Initialize the ViewModel using by viewModels() extension function
        val messageViewModel: MessagesViewModel by viewModels()

        messageViewModel.messageListLiveData.observe(viewLifecycleOwner) { messages ->
            if (messages == null || messages.isEmpty()) {
                binding.messagesList.visibility = View.GONE
                binding.emptyListIndicator.visibility = View.VISIBLE
            } else {
                binding.emptyListIndicator.visibility = View.GONE
                binding.messagesList.visibility = View.VISIBLE
                //messageItemAdapter.setItemsList(messages as MutableList<MessageEntity>)
                val count = messageItemAdapter.itemCount
                if (count < messages.size)
                    messageItemAdapter.setItems(messages.subList(0, messages.size - count))
                else if (count > messages.size)
                    messageItemAdapter.removeItems(messages)
            }
        }

        messageViewModel.loadingLiveData.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.messagesList.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.messagesList.visibility = View.VISIBLE
            }
        }
        messageViewModel.loadMessages()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMessageClick(message: MessageEntity, position: Int) {
        val bundle = Bundle()
        bundle.putParcelable(MESSAGE, message)
        bundle.putInt(MESSAGE_POS, position)
        findNavController().navigate(R.id.action_MessageListFragment_to_MessageFragment, bundle)
    }

    companion object {
        const val MESSAGE = "message"
        const val MESSAGE_POS = "message_pos"
    }
}