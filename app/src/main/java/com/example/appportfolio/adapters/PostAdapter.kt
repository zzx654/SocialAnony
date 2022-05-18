package com.example.appportfolio.adapters

import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.ItemPostBinding
import com.google.android.material.chip.Chip

class PostAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ItemPostBinding>(
            layoutInflater,
            R.layout.item_post,
            parent,
            false
        ).let{
            postViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post=posts[position]
        (holder as PostAdapter.postViewHolder).onbind(post)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    inner class postViewHolder(val binding:ItemPostBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(post:Post)
        {

            binding.post=post

            binding.postitem.setOnClickListener {
                PostClickListener?.let{ click->
                    click(post)
                }
            }
            binding.cgTag.removeAllViews()
            post.tags?.let{


                binding.cgTag.visibility= View.VISIBLE
                var tags:List<String> = listOf()
                if(it.contains("#"))
                    tags=it.split("#")
                else
                    tags+=it

                for(tag in tags)
                {
                    val chip= Chip(binding.cgTag.context).apply{
                        text="#"+tag
                        chipStrokeWidth=0f
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP , 16f)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            chipBackgroundColor=
                                AppCompatResources.getColorStateList(binding.cgTag.context, R.color.chipback)
                            setChipStrokeColorResource(R.color.black)
                            setTextColor(ContextCompat.getColor(binding.cgTag.context, R.color.chiptext))
                            setTextSize(12f)
                        }
                        setOnClickListener {
                            tagClickListener?.let{ click->

                                click(tag)
                            }

                        }
                    }
                    binding.cgTag.addView(chip)
                }
            }
            binding.commentimg.visibility=View.VISIBLE
            binding.favoriteimg.visibility=View.VISIBLE


        }
    }

    private val diffCallback=object: DiffUtil.ItemCallback<Post>(){
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem==newItem
        }
    }
    val differ= AsyncListDiffer(this,diffCallback)

    var posts:List<Post>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    var tagClickListener:((String)->Unit)?=null

    fun setOntagClickListener(listener: (String) -> Unit){
        tagClickListener=listener
    }

    var PostClickListener:((Post)->Unit)?=null

    fun setOnPostClickListener(listener: (Post) -> Unit){
        PostClickListener=listener
    }
}