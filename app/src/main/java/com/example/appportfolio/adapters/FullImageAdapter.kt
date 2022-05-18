package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.ChatImage
import com.example.appportfolio.databinding.ItemChatcontentimgBinding

class FullImageAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemChatcontentimgBinding>(
            layoutInflater,
            R.layout.item_chatcontentimg,
            parent,
            false
        ).let{
            ChatImageViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val chatimage=chatimages[position]

        (holder as ChatImageViewHolder).onbind(chatimage)

    }
    private val diffCallback=object: DiffUtil.ItemCallback<ChatImage>(){
        override fun areContentsTheSame(oldItem: ChatImage, newItem: ChatImage): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: ChatImage, newItem: ChatImage): Boolean {
            return oldItem==newItem
        }
    }
    val differ= AsyncListDiffer(this,diffCallback)

    var chatimages:List<ChatImage>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    inner class ChatImageViewHolder( val binding: ItemChatcontentimgBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(chatimage: ChatImage)
        {
            Glide.with(binding.img.context)
                .load(chatimage.imageUrl)
                .into(binding.img)
        }
    }
    override fun getItemCount(): Int {
        return chatimages.size
    }
}