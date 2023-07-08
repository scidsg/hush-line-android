package org.scidsg.hushline.android.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.scidsg.hushline.android.OnMessageClickListener
import org.scidsg.hushline.android.database.MessageEntity
import org.scidsg.hushline.android.databinding.FragmentMessageListItemBinding


class MessageItemAdapter(
    private val context: Context,
    private var itemList: MutableList<MessageEntity>,
    private val messageClickListener: OnMessageClickListener):
    Adapter<MessageItemAdapter.MessageItemViewHolder>() {

    private lateinit var _binding: FragmentMessageListItemBinding
    private val binding get() = _binding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        _binding = FragmentMessageListItemBinding.inflate(inflater, parent, false)

        return MessageItemViewHolder(binding, messageClickListener)
    }

    override fun onBindViewHolder(holder: MessageItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = itemList.size

    fun setItemsList(items: MutableList<MessageEntity>) {
        itemList = items
        notifyItemRangeChanged(0, items.size)
    }

    fun setItems(items: List<MessageEntity>) {
        itemList.addAll(0, items)
        notifyItemRangeInserted(0, items.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeItems(items: List<MessageEntity>) {
        val removedItems = mutableListOf<MessageEntity>()
        itemList.forEach {
            if (!items.contains(it))
                removedItems.add(it)
        }
        removedItems.forEach {
            itemList.remove(it)
        }
        notifyDataSetChanged() //because items removed could be in random positions
    }

    inner class MessageItemViewHolder(
        private val binding: FragmentMessageListItemBinding,
        private val messageClickListener: OnMessageClickListener):
        ViewHolder(binding.root), View.OnClickListener {

        private lateinit var message: MessageEntity

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(message: MessageEntity, position: Int) {
            this.message = message
            binding.messageDate.text = message.timestamp
            binding.messagePreview.text = message.message

            if (!message.read) {//if (position < 2) {
                val typeFace = Typeface.createFromAsset(
                    context.assets, "fonts/roboto_bold.ttf")
                binding.messagePreview.setTypeface(typeFace, Typeface.BOLD)
            } else {
                val typeFace = Typeface.createFromAsset(
                    context.assets, "fonts/roboto_regular.ttf")
                binding.messagePreview.setTypeface(typeFace, Typeface.NORMAL)
            }
        }

        override fun onClick(v: View?) {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                messageClickListener.onMessageClick(this.message, position)
            }
        }
    }
}