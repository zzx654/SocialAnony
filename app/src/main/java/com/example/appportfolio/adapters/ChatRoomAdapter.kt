package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.Chatroom
import com.example.appportfolio.databinding.ItemChatroomBinding

class ChatRoomAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemChatroomBinding>(
            layoutInflater,
            R.layout.item_chatroom,
            parent,
            false
        ).let{
            ChatRoomViewHolder(it)
        }
    }
    inner class ChatRoomViewHolder(val binding:ItemChatroomBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(chatroom: Chatroom)
        {
            binding.chatroom=chatroom

            binding.root.setOnClickListener {
                roomClickListener?.let{ click->
                    click(chatroom)
                }
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatroom=chatrooms[position]
        (holder as ChatRoomAdapter.ChatRoomViewHolder).onbind(chatroom)
    }

    override fun getItemCount(): Int {
        return chatrooms.size
    }
    private val diffCallback=object: DiffUtil.ItemCallback<Chatroom>(){
        override fun areContentsTheSame(oldItem: Chatroom, newItem:Chatroom): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Chatroom, newItem: Chatroom): Boolean {
            return oldItem==newItem
        }
    }
    val differ= AsyncListDiffer(this,diffCallback)

    var chatrooms:List<Chatroom>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    var roomClickListener:((Chatroom)->Unit)? = null

    fun setroomClickListener(listener: (Chatroom) -> Unit){
        roomClickListener=listener
    }
}