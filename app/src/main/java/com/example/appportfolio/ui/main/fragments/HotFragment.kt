package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.FragmentHotBinding
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.hotPostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HotFragment: BasePostFragment(R.layout.fragment_hot) {
    lateinit var binding: FragmentHotBinding
    lateinit var hotpostAdapter: PostAdapter
    override val scrollView: NestedScrollView
        get() = binding.scrollView
    override val scrollTool: FloatingActionButton
        get() = binding.fbScrollTool
    override val rvPosts: RecyclerView
        get() = binding.rvPosts
    override val loadMoreProgressBar: ProgressBar
        get() = binding.loadMoreProgressbar
    override val loadProgressBar: ProgressBar
        get() = binding.loadProgressBar
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: hotPostViewModel by viewModels()
            return vm
        }
    override val postAdapter: PostAdapter
        get() = hotpostAdapter
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    protected val viewModel: hotPostViewModel
        get() = basePostViewModel as hotPostViewModel
    private var mRootView:View?=null
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            binding= DataBindingUtil.inflate<FragmentHotBinding>(inflater,
            R.layout.fragment_hot,container,false)
            hotpostAdapter= PostAdapter()
            setView()
            mRootView=binding.root
            refreshPosts()

        }

        return mRootView
    }
    override fun loadNewPosts() {
        getPosts()
    }

    override fun refreshPosts() {
        getPosts(true)
    }




    fun getPosts(refresh:Boolean=false)
    {
        var lastpostnum:Int?=null
        var lastposthot:Int?=null
        val curPosts=postAdapter.differ.currentList
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
            viewModel.getHotPosts(lastpostnum,lastposthot,gpsTracker.latitude,gpsTracker.longitude,api)
        }
        else{
            viewModel.getHotPosts(lastpostnum,lastposthot,null,null,api)
        }
    }
}