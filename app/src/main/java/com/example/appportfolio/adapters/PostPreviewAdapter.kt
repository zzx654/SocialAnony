package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.ItemPostBinding
import com.example.appportfolio.databinding.ItemPostpreviewBinding
import com.example.appportfolio.other.Constants.ITEM

class PostPreviewAdapter: ListAdapter<Post, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemPostpreviewBinding>(
            layoutInflater,
            R.layout.item_postpreview,
            parent,
            false
        ).let{
            postViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as postViewHolder).onbind(currentList[position])
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return ITEM
    }
    inner class postViewHolder(val binding:ItemPostpreviewBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(post:Post)
        {
            binding.post=post
            binding.commentimg.visibility= View.VISIBLE
            binding.favoriteimg.visibility= View.VISIBLE
            binding.postitem.onSingleClick {
                postClickListener?.let{ click->
                    click(post)
                }
            }
        }
    }
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Post>(){
            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem==newItem
            }
        }
    }
    var postClickListener:((Post)->Unit)?=null
    fun setOnPostClickLitener(listener:(Post)->Unit){
        postClickListener=listener
    }
}