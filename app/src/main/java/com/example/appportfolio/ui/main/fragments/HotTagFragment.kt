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
class HotTagFragment: BasePostFragment(R.layout.fragment_posts) {
    lateinit var binding: FragmentPostsBinding
    private lateinit var hotpostAdapter: PostAdapter
    override val scrollTool: FloatingActionButton
        get() = binding.fbScrollTool
    override val rvPosts: RecyclerView
        get() = binding.rvPosts
    override val loadProgressBar: ProgressBar
        get() = binding.loadProgressBar
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: hotPostViewModel by viewModels()
            return vm
        }
    override val tvWarn: TextView
        get() = binding.tvWarn
    override val retry: TextView
        get() = binding.retry
    override val postAdapter: PostAdapter
        get() = hotpostAdapter
    private lateinit var vmTag: TagViewModel
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    private val viewModel: hotPostViewModel
        get() = basePostViewModel as hotPostViewModel
    private var mRootView:View?=null
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null) {
            binding = DataBindingUtil.inflate<FragmentPostsBinding>(
                inflater,
                R.layout.fragment_posts, container, false
            )
            hotpostAdapter = PostAdapter()
            vmTag= ViewModelProvider(requireActivity())[TagViewModel::class.java]
            setView()
            refreshPosts()
            mRootView=binding.root
        }
        return mRootView
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
        val curPosts=postAdapter.currentList
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