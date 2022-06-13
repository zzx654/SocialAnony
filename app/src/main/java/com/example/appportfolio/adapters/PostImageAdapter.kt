package com.example.appportfolio.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.databinding.ItemPostimageBinding


class PostImageAdapter: ListAdapter<Uri, RecyclerView.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater=LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemPostimageBinding>(
            layoutInflater,
            R.layout.item_postimage,
            parent,
            false
        ).let{
            PostImageViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val imageUri=currentList[position]

        (holder as PostImageViewHolder).onbind(imageUri)
        (holder as PostImageViewHolder).binding.btnDel.setOnClickListener {
            deleteClickListener?.let { delete->
                delete(imageUri)
            }
        }



    }

    inner class PostImageViewHolder( val binding:ItemPostimageBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(imageUri:Uri)
        {
            binding.imgUri=imageUri.toString()

        }
    }
    companion object{
        val diffUtil=object: DiffUtil.ItemCallback<Uri>(){
            override fun areContentsTheSame(oldItem: Uri, newItem:Uri): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
                return oldItem==newItem
            }
        }
    }
    override fun getItemCount(): Int {
        return currentList.size
    }

    var deleteClickListener:((Uri)->Unit)?=null

    fun setOnDeleteClickListener(listener:(Uri)->Unit){
        deleteClickListener=listener
    }
}