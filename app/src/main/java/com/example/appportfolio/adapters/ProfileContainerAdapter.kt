package com.example.appportfolio.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.databinding.ContainerProfileBinding

class ProfileContainerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<ContainerProfileBinding>(
            layoutInflater,
            R.layout.container_profile,
            parent,
            false
        ).let{
            profileViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as profileViewHolder).onbind()
    }

    override fun getItemCount(): Int=1
    inner class profileViewHolder(val binding: ContainerProfileBinding):RecyclerView.ViewHolder(binding.root){
        fun onbind()
        {
            binding.tvfollowercount.text=followercount.toString()
            binding.tvfollowingcount.text=followingcount.toString()
            binding.tvpostscount.text=postscount.toString()
            if(toolsVis){
                binding.tools.visibility= View.VISIBLE
                binding.line.visibility=View.VISIBLE
            }else{
                binding.tools.visibility= View.GONE
                binding.line.visibility=View.GONE
            }
            binding.editprofile.visibility=if(editprofileVis)View.VISIBLE else View.GONE
            followingperson?.let{
                if(followingperson==1)
                    binding.imgfollow.setImageResource(R.drawable.favorite_on)
                else
                    binding.imgfollow.setImageResource(R.drawable.favorite_off)
            }

            binding.following.onSingleClick {
                followingClickListener?.let{ click->
                    click()
                }
            }
            binding.follower.onSingleClick {
                followerClickListener?.let{ click->
                    click()
                }
            }

            if (userprofileimage == null) {
                when (usergender) {
                    "남자" -> binding.profileimage.setImageResource(R.drawable.icon_male)
                    "여자" -> binding.profileimage.setImageResource(R.drawable.icon_female)
                    else -> binding.profileimage.setImageResource(R.drawable.icon_none)
                }
            } else {
                Glide.with(binding.profileimage.context)
                    .load(userprofileimage)
                    .placeholder(ColorDrawable(ContextCompat.getColor(binding.profileimage.context, R.color.gray)))
                    .error(ColorDrawable(ContextCompat.getColor(binding.profileimage.context, R.color.gray)))
                    .into(binding.profileimage)
            }
            binding.profileimage.onSingleClick {
                ProfileimgClickListener?.let{ click->
                    click()
                }
            }
            binding.btnfollow.setOnClickListener {
                followClickListener?.let{ click->
                    click(followingperson!!)

                }
            }
            binding.btnchat.onSingleClick {
                chatClickListener?.let{ click->
                    click()

                }

            }
            binding.tvnickname.text = usernickname

            usergender?.let{
                val genderstr=if(it=="남자")"♂" else if(it=="여자")"♀" else "비공개"
                binding.tvgenderage.text = "(${genderstr} · ${userage?.let { SocialApplication.getAge(it) }}) "
            }

        }
    }
    var followingperson:Int?=null

    fun setOnProfileClickListener(listener:()->Unit){
        ProfileimgClickListener=listener
    }
    var toolsVis=false
    var editprofileVis=false
    var ProfileimgClickListener:(()->Unit)?=null
    var usergender:String?=null
    var usernickname:String=""
    var userprofileimage:String?=null
    var followingcount:Int=0
    var followercount=0
    var userage:Int?=null
    var followClickListener:((Int)->Unit)?=null
    var followingClickListener:(()->Unit)?=null
    var followerClickListener:(()->Unit)?=null
    var postscount:Int=0
    fun setOnFollowClickListener(listener: (Int) ->Unit){
        followClickListener=listener
    }
    fun setOnFollowingClickListener(listener: () -> Unit){
        followingClickListener=listener
    }
    fun setOnFollowerClickListener(listener: () -> Unit){
        followerClickListener=listener
    }
    fun setfollowing(followingstate:Int){
        followingperson=followingstate
    }
    fun setuserinfo(profileimage:String?,nickname:String,gender:String,age:Int,followingcount:Int,followercount:Int,postscount:Int){
        userprofileimage=profileimage
        usernickname=nickname
        this.postscount=postscount
        usergender=gender
        userage=age
        this.followingcount=followingcount
        this.followercount=followercount
    }
    var chatClickListener:(()->Unit)?=null
    fun setOnChatClickListener(listener:()->Unit){
        chatClickListener=listener
    }
}