package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.databinding.FragmentPostsBinding
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.TagViewModel
import com.example.appportfolio.ui.main.viewmodel.hotPostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HotTagFragment: BasePostFragment() {

    private lateinit var hotpostAdapter: PostAdapter

    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: hotPostViewModel by viewModels()
            return vm
        }


    private lateinit var vmTag: TagViewModel

    private val viewModel: hotPostViewModel
        get() = basePostViewModel as hotPostViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vmTag= ViewModelProvider(requireActivity())[TagViewModel::class.java]
    }
    override fun loadNewPosts() {
        getPosts()
    }

    override fun refreshPosts() {
        vmTag.getTagLiked((this@HotTagFragment.parentFragment as TagPostsFragment).tagname!!,api)
        getPosts(true)
    }


    fun getPosts(refresh:Boolean=false)
    {
        var lastpostnum:Int?=null
        var lastposthot:Int?=null
        val curPosts=postAdapter?.currentList
        if(!refresh)
        {
            if(!curPosts.isNullOrEmpty())
            {
                val lastPost=curPosts.last()
                lastpostnum=lastPost.postnum
                lastposthot=lastPost.commentcount+lastPost.likecount
            }
        }
        if(SocialApplication.checkGeoPermission(requireContext()))
        {

                viewModel.getTagHotPosts((this@HotTagFragment.parentFragment as TagPostsFragment).tagname!!,lastpostnum,lastposthot,gpsTracker.latitude,gpsTracker.longitude,api)

        }
        else{
            viewModel.getTagHotPosts((this@HotTagFragment.parentFragment as TagPostsFragment).tagname!!,lastpostnum,lastposthot,null,null,api)
        }
    }
}