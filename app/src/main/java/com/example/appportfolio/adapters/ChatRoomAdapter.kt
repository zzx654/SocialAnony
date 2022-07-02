package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.Chatroom
import com.example.appportfolio.databinding.ItemChatroomBinding
import com.example.appportfolio.other.Constants.ITEM

class ChatRoomAdapter: ListAdapter<Chatroom,RecyclerView.ViewHolder>(diffUtil) {
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
        val chatroom=currentList[position]
        (holder as ChatRoomAdapter.ChatRoomViewHolder).onbind(chatroom)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return ITEM
    }
    companion object{
        val diffUtil=object: DiffUtil.ItemCallback<Chatroom>(){
            override fun areContentsTheSame(oldItem: Chatroom, newItem:Chatroom): Boolean {
                return (oldItem.hashCode() == newItem.hashCode())&&oldItem.isread==newItem.isread
            }

            override fun areItemsTheSame(oldItem: Chatroom, newItem: Chatroom): Boolean {
                return oldItem==newItem
            }
        }
    }

    var roomClickListener:((Chatroom)->Unit)? = null

    fun setroomClickListener(listener: (Chatroom) -> Unit){
        roomClickListener=listener
    }
}