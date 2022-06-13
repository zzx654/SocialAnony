package com.example.appportfolio.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.*
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.data.entities.Comment
import com.example.appportfolio.databinding.*
import com.example.appportfolio.other.Constants.COMMENT_VIEW_TYPE
import com.example.appportfolio.other.Constants.LOADING_VIEW_TYPE
import com.example.appportfolio.other.Constants.REPLY_VIEW_TYPE


class CommentAdapter: ListAdapter<Comment,RecyclerView.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        val viewholder=when(viewType){
            LOADING_VIEW_TYPE->{
                DataBindingUtil.inflate<NetworkStateItemBinding>(
                    layoutInflater,
                    R.layout.network_state_item,
                    parent,
                    false
                ).let{
                    NetworkStateItemViewHolder(it)
                }
            }
            COMMENT_VIEW_TYPE->{
                return DataBindingUtil.inflate<ItemCommentBinding>(
                    layoutInflater,
                    R.layout.item_comment,
                    parent,
                    false
                ).let {
                    commentViewHolder(it)
                }
            }
            else->{
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
        return viewholder
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position)== COMMENT_VIEW_TYPE)
            (holder as CommentAdapter.commentViewHolder).onbind(currentList[position])
        else if(getItemViewType(position)== REPLY_VIEW_TYPE)
            (holder as CommentAdapter.replyViewHolder).onbind(currentList[position])
    }

    override fun getItemCount(): Int {
        return currentList.size
    }
    override fun getItemViewType(position: Int): Int {
            return if(currentList[position].commentid==null) LOADING_VIEW_TYPE else if(currentList[position].depth==0) COMMENT_VIEW_TYPE else REPLY_VIEW_TYPE
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

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Comment>() {
            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                return oldItem.commentid == newItem.commentid
            }
        }
    }

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
    inner class NetworkStateItemViewHolder(val binding: NetworkStateItemBinding):RecyclerView.ViewHolder(binding.root)
}