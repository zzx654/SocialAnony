package com.example.appportfolio.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.ItemPostBinding
import com.example.appportfolio.databinding.NearpostsRgBinding
import com.example.appportfolio.databinding.NetworkStateItemBinding
import com.example.appportfolio.other.Constants.NONE_HEADER
import com.google.android.material.chip.Chip

class PostAdapter: ListAdapter<Post,RecyclerView.ViewHolder>(diffUtil) {
    private var HEADER_TYPE=NONE_HEADER
    private val POST_VIEW_TYPE=0
    private val RADIOGROUP_VIEW_TYPE=2
    private val LOADING_VIEW_TYPE=3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        val viewHolder=when(viewType){
            RADIOGROUP_VIEW_TYPE->{
                DataBindingUtil.inflate<NearpostsRgBinding>(
                    layoutInflater,
                    R.layout.nearposts_rg,
                    parent,
                    false
                ).let{
                    rgViewHolder(it)
                }
            }
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
            else->{
                DataBindingUtil.inflate<ItemPostBinding>(
                    layoutInflater,
                    R.layout.item_post,
                    parent,
                    false
                ).let{
                    postViewHolder(it)
                }
            }
        }
        return viewHolder

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position)==RADIOGROUP_VIEW_TYPE)
            (holder as rgViewHolder).onbind()
        else if(getItemViewType(position)!=LOADING_VIEW_TYPE){
            if(HEADER_TYPE== NONE_HEADER)
                (holder as postViewHolder).onbind(currentList[position])
            else
                (holder as postViewHolder).onbind(currentList[position-1])
        }
    }

    override fun getItemCount(): Int {
        return if(HEADER_TYPE== NONE_HEADER)
            currentList.size
        else
            currentList.size+1
    }

    override fun getItemViewType(position: Int): Int {
        val minusoffset=if(HEADER_TYPE== NONE_HEADER) 0 else 1
        return if(position==0) {
            when(HEADER_TYPE) {
                NONE_HEADER-> POST_VIEW_TYPE
                else-> RADIOGROUP_VIEW_TYPE
            }

        } else
            if(currentList[position-minusoffset].postid==null) LOADING_VIEW_TYPE else POST_VIEW_TYPE



    }
    fun setheadertype(type:Int){
        HEADER_TYPE=type
    }
    inner class postViewHolder(val binding:ItemPostBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind(post:Post)
        {

            binding.post=post

            binding.postitem.setOnClickListener {
                Log.d("ClickPost","clicked")
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
                        text= "#$tag"
                        chipStrokeWidth=0f
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP , 16f)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            chipBackgroundColor=
                                AppCompatResources.getColorStateList(binding.cgTag.context, R.color.chipback)
                            setChipStrokeColorResource(R.color.black)
                            setTextColor(ContextCompat.getColor(binding.cgTag.context, R.color.chiptext))
                            textSize = 12f
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
    inner class rgViewHolder(val binding:NearpostsRgBinding):RecyclerView.ViewHolder(binding.root){
        @SuppressLint("ClickableViewAccessibility")
        fun onbind()
        {
            checkedChip?.let{
                binding.cgDistance.check(it)
            }

            binding.cgDistance.setOnCheckedStateChangeListener { group, checkedIds ->


                var idstr=checkedIds.toString().replace("[","")
                idstr=idstr.replace("]","")

                val curcheckedDistance=when(idstr){
                    R.id.chip5.toString()->5
                    R.id.chip10.toString()->10
                    R.id.chip15.toString()->15
                    R.id.chip20.toString()->20
                    R.id.chip25.toString()->25
                    R.id.chip50.toString()->50
                    R.id.chip75.toString()->75
                    R.id.chip100.toString()->100
                    else->null
                }
                DistanceChangedListener?.let{ checked->
                    curcheckedDistance?.let{
                        checked(curcheckedDistance,idstr.toInt())
                    }
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



    var tagClickListener:((String)->Unit)?=null

    fun setOntagClickListener(listener: (String) -> Unit){
        tagClickListener=listener
    }

    var PostClickListener:((Post)->Unit)?=null

    var checkedDistance:Int?=5

    fun setOnPostClickListener(listener: (Post) -> Unit){
        PostClickListener=listener
    }
    var DistanceChangedListener:((Int,Int)->Unit)?=null

    var checkedChip:Int?=R.id.chip5
    fun setOnDistanceChangedListener(listener:(Int,Int)->Unit){
        DistanceChangedListener=listener
    }

    inner class NetworkStateItemViewHolder(val binding:NetworkStateItemBinding):RecyclerView.ViewHolder(binding.root)

}