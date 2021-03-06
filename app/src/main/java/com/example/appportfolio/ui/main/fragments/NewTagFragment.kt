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
import com.example.appportfolio.ui.main.viewmodel.newPostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewTagFragment: BasePostFragment(R.layout.fragment_posts) {
    lateinit var binding: FragmentPostsBinding
    private lateinit var newpostAdapter: PostAdapter
    private lateinit var vmTag: TagViewModel
    override val scrollTool: FloatingActionButton
        get() = binding.fbScrollTool
    override val rvPosts: RecyclerView
        get() = binding.rvPosts
    override val loadProgressBar: ProgressBar
        get() = binding.loadProgressBar
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: newPostViewModel by viewModels()
            return vm
        }
    override val postAdapter: PostAdapter
        get() = newpostAdapter
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    override val tvWarn: TextView
        get() = binding.tvWarn
    override val retry: TextView
        get() = binding.retry
    private val viewModel: newPostViewModel
        get() = basePostViewModel as newPostViewModel
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
            vmTag= ViewModelProvider(requireActivity())[TagViewModel::class.java]
            newpostAdapter = PostAdapter()
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
        vmTag.getTagLiked((this@NewTagFragment.parentFragment as TagPostsFragment).tagname!!,api)
        getPosts(true)
    }

    fun getPosts(refresh:Boolean=false)
    {
        var lastpostnum:Int?=null
        var lastpostdate:String?=null
        val curPosts=postAdapter.currentList
        if(!refresh)
        {
            if(curPosts.isNotEmpty())
            {
                val lastPost=curPosts.last()
                lastpostnum=lastPost.postnum
                lastpostdate=lastPost.date
            }
        }
        if(SocialApplication.checkGeoPermission(requireContext()))
        {
            viewModel.getTagNewPosts((this@NewTagFragment.parentFragment as TagPostsFragment).tagname!!,lastpostnum,lastpostdate,gpsTracker.latitude,gpsTracker.longitude,api)
        }
        else{
            viewModel.getTagNewPosts((this@NewTagFragment.parentFragment as TagPostsFragment).tagname!!,lastpostnum,lastpostdate,null,null,api)
        }
    }
}