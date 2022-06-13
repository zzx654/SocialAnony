package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.ChatImage
import com.example.appportfolio.databinding.ItemChatcontentimgBinding

class FullImageAdapter: ListAdapter<ChatImage, RecyclerView.ViewHolder>(diffUtil) {
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

        val chatimage=currentList[position]

        (holder as ChatImageViewHolder).onbind(chatimage)

    }

    companion object{
        val diffUtil=object: DiffUtil.ItemCallback<ChatImage>(){
            override fun areContentsTheSame(oldItem: ChatImage, newItem: ChatImage): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areItemsTheSame(oldItem: ChatImage, newItem: ChatImage): Boolean {
                return oldItem==newItem
            }
        }
    }
    inner class ChatImageViewHolder( val binding: ItemChatcontentimgBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(chatimage: ChatImage)
        {
            Glide.with(binding.img.context)
                .load(chatimage.imageUrl)
                .into(binding.img)
        }
    }
    override fun getItemCount(): Int {
        return currentList.size
    }
}