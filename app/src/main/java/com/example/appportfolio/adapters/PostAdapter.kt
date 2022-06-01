package com.example.appportfolio.adapters

import android.os.Build
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.ContainerProfileBinding
import com.example.appportfolio.databinding.ItemPostBinding
import com.example.appportfolio.databinding.NearpostsRgBinding
import com.example.appportfolio.databinding.NetworkStateItemBinding
import com.example.appportfolio.other.Constants.NONE_HEADER
import com.example.appportfolio.other.Constants.RG_HEADER
import com.example.appportfolio.other.Resource
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.fragments.ImageFragment
import com.google.android.material.chip.Chip

class PostAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var HEADER_TYPE=NONE_HEADER
    private val POST_VIEW_TYPE=0
    private val PROFILE_VIEW_TYPE=1
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
            PROFILE_VIEW_TYPE->{
                DataBindingUtil.inflate<ContainerProfileBinding>(
                    layoutInflater,
                    R.layout.container_profile,
                    parent,
                    false
                ).let{
                    profileViewHolder(it)
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
        else if(getItemViewType(position)==PROFILE_VIEW_TYPE)
            (holder as profileViewHolder).onbind()
        else if(getItemViewType(position)!=LOADING_VIEW_TYPE){
            if(HEADER_TYPE== NONE_HEADER)
                (holder as postViewHolder).onbind(posts[position])
            else
                (holder as postViewHolder).onbind(posts[position-1])
        }
    }

    override fun getItemCount(): Int {
        return if(HEADER_TYPE== NONE_HEADER)
            posts.size
        else
            posts.size+1
    }

    override fun getItemViewType(position: Int): Int {
        val minusoffset=if(HEADER_TYPE== NONE_HEADER) 0 else 1
        if(position==0)
        {
            when(HEADER_TYPE)
            {
                NONE_HEADER->return POST_VIEW_TYPE
                RG_HEADER->return RADIOGROUP_VIEW_TYPE
                else->return PROFILE_VIEW_TYPE
            }
        }
        else
        {
            return if(posts[position-minusoffset].postid==null) LOADING_VIEW_TYPE else POST_VIEW_TYPE
        }


    }
    fun setheadertype(type:Int){
        HEADER_TYPE=type
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
    inner class rgViewHolder(val binding:NearpostsRgBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind()
        {
            binding.rgDistance.setOnCheckedChangeListener { group, checkedId ->
                checkedDistance=when(checkedId){
                    R.id.rb5->5
                    R.id.rb10->10
                    R.id.rb15->15
                    R.id.rb20->20
                    R.id.rb25->25
                    else->null
                }
                DistanceChangedListener?.let{ checked->
                    checked()
                }

            }
        }
    }
    inner class profileViewHolder(val binding:ContainerProfileBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind()
        {
            if(followingperson==1)
                binding.imgfollow.setImageResource(R.drawable.favorite_on)
            else
                binding.imgfollow.setImageResource(R.drawable.favorite_off)

            if (userprofileimage == null) {
                when (usergender) {
                    "남자" -> binding.profileimage.setImageResource(R.drawable.icon_male)
                    "여자" -> binding.profileimage.setImageResource(R.drawable.icon_female)
                    else -> binding.profileimage.setImageResource(R.drawable.icon_none)
                }
            } else {
                Glide.with(binding.profileimage.context)
                    .load(userprofileimage)
                    .into(binding.profileimage)
                var img = userprofileimage
                binding.profileimage.onSingleClick {
                    ProfileimgClickListener?.let{ click->
                        click(img)
                    }

                }
            }
            binding.btnfollow.setOnClickListener {
                followClickListener?.let{ click->
                    click(followingperson)

                }
            }
            binding.btnchat.onSingleClick {
                chatClickListener?.let{ click->
                    click()

                }

            }
            binding.tvnickname.text = usernickname

            binding.tvgenderage.text = "${usergender} · ${SocialApplication.getAge(userage!!)} "
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

    fun setOnProfileClickListener(listener:(String?)->Unit){
        ProfileimgClickListener=listener
    }
    var ProfileimgClickListener:((String?)->Unit)?=null

    var PostClickListener:((Post)->Unit)?=null

    var checkedDistance:Int?=5

    var followingperson:Int=0

    var usergender:String="none"
    var usernickname:String=""
    var userprofileimage:String?=null
    var userage:Int?=null
    fun setuserinfo(profileimage:String?,nickname:String,gender:String,age:Int?){
        userprofileimage=profileimage
        usernickname=nickname
        usergender=gender
        userage=age
    }
    var followClickListener:((Int)->Unit)?=null
    fun setOnFollowClickListener(listener: (Int) ->Unit){
        followClickListener=listener
    }
    fun setfollowing(followingstate:Int){
        followingperson=followingstate
    }
    var chatClickListener:(()->Unit)?=null
    fun setOnChatClickListener(listener:()->Unit){
        chatClickListener=listener
    }

    fun setOnPostClickListener(listener: (Post) -> Unit){
        PostClickListener=listener
    }
    var DistanceChangedListener:(()->Unit)?=null
    fun setOnDistanceChangedListener(listener:()->Unit){
        DistanceChangedListener=listener
    }

    inner class NetworkStateItemViewHolder(val binding:NetworkStateItemBinding):RecyclerView.ViewHolder(binding.root)

}