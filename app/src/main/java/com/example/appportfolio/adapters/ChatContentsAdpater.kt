package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.ChatImage
import com.example.appportfolio.databinding.ItemGridimgBinding

class ChatContentsAdpater: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemGridimgBinding>(
        layoutInflater,
        R.layout.item_gridimg,
        parent,
        false
        ).let{
            ChatImageViewHolder(it)
        }
    }
    inner class ChatImageViewHolder(val binding: ItemGridimgBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(image: ChatImage, position:Int)
        {
            Glide.with(binding.gridImg.context)
                .load(image.imageUrl)
                .into(binding.gridImg)

            if(activecheck)
                binding.checkbox.visibility= View.VISIBLE
            else
                binding.checkbox.visibility=View.GONE

            binding.checkbox.isChecked=image.isChecked
            binding.checkbox.setOnClickListener {
                chatimages[position].isChecked= binding.checkbox.isChecked
            }
            binding.root.setOnClickListener {
                ImageClickListener?.let{ click->
                    if(!activecheck)
                        click(position)
                }
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatimage=chatimages[position]
        (holder as ChatContentsAdpater.ChatImageViewHolder).onbind(chatimage,position)
    }
    override fun getItemCount(): Int {
        return chatimages.size
    }
    private val diffCallback=object: DiffUtil.ItemCallback<ChatImage>(){
        override fun areContentsTheSame(oldItem: ChatImage, newItem:ChatImage): Boolean {
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

    var activecheck:Boolean=false

    fun updateCheckbox(active:Boolean)
    {
        activecheck=active
    }
    var ImageClickListener:((Int)->Unit)?=null

    fun setOnImageClickListener(listener: (Int) -> Unit){
        ImageClickListener=listener
    }
}