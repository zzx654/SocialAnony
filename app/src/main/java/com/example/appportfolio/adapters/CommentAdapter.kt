package com.example.appportfolio.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.data.entities.Comment
import com.example.appportfolio.databinding.ItemCommentBinding
import com.example.appportfolio.databinding.ItemReplyBinding
import com.example.appportfolio.other.Constants.COMMENT
import com.example.appportfolio.other.Constants.REPLY

class CommentAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        if(viewType== COMMENT) {
            return DataBindingUtil.inflate<ItemCommentBinding>(
                layoutInflater,
                R.layout.item_comment,
                parent,
                false
            ).let {
                commentViewHolder(it)
            }
        }
        else{
            return DataBindingUtil.inflate<ItemReplyBinding>(
                layoutInflater,
                R.layout.item_reply,
                parent,
                false
            ).let{
                replyViewHolder(it)
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val comment=comments[position]
        if(getItemViewType(position)== COMMENT)
            (holder as CommentAdapter.commentViewHolder).onbind(comment)
        else
            (holder as CommentAdapter.replyViewHolder).onbind(comment)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun getItemViewType(position: Int): Int {
        if(comments[position].depth==0)
        {
            return COMMENT
        }
        else{
            return REPLY
        }
    }
    inner class replyViewHolder(val binding:ItemReplyBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(comment: Comment)
        {
            binding.comment=comment

            binding.like.onSingleClick {
                FavoriteClickListener?.let{ toggle->
                    toggle(comment)
                }
            }
            binding.commentmenu.setOnClickListener {
                menuClickListener?.let{ click->
                    click(comment)
                }
            }
            binding.imgProfile.setOnClickListener {
                profileClickListener?.let{ click->
                    click(comment)

                }
            }
        }
    }
    inner class commentViewHolder(val binding: ItemCommentBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(comment:Comment) {
            binding.comment=comment

            binding.like.onSingleClick {
                FavoriteClickListener?.let{ toggle->
                    toggle(comment)
                }
            }
            binding.root.setOnClickListener {
                rootClickListener?.let{ click->
                    click(comment)
                }

            }
            binding.commentmenu.setOnClickListener {
                menuClickListener?.let{ click->
                    click(comment)
                }
            }
            binding.imgProfile.setOnClickListener {
                profileClickListener?.let{ click->
                    click(comment)

                }
            }
        }

    }

    private val diffCallback=object: DiffUtil.ItemCallback<Comment>(){
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem==newItem
        }
    }
    val differ= AsyncListDiffer(this,diffCallback)

    var comments:List<Comment>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    var FavoriteClickListener:((Comment)->Unit)? = null

    var rootClickListener:((Comment)->Unit)? = null

    var menuClickListener:((Comment)->Unit)? = null

    var profileClickListener:((Comment)->Unit)? = null

    fun setOnFavoriteClickListener(listener: (Comment) -> Unit){
        FavoriteClickListener=listener
    }
    fun setOnrootClickListener(listener: (Comment) -> Unit){
        rootClickListener=listener
    }
    fun setOnMenuClickListener(listener: (Comment) -> Unit){
        menuClickListener=listener
    }
    fun setOnProfileClickListener(listener:(Comment)-> Unit){
        profileClickListener=listener
    }
}