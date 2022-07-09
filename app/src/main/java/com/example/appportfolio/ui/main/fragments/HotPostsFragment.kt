package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.databinding.FragmentPostsBinding
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.hotPostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HotPostsFragment: BasePostFragment(R.layout.fragment_posts) {
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
    override val postAdapter: PostAdapter
        get() = hotpostAdapter
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    private val viewModel: hotPostViewModel
        get() = basePostViewModel as hotPostViewModel
    private var mRootView:View?=null
    override val tvWarn: TextView
        get() = binding.tvWarn
    override val retry: TextView
        get() = binding.retry
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            (activity as MainActivity).setToolBarVisible("hotPostsFragment")
            binding= DataBindingUtil.inflate<FragmentPostsBinding>(inflater,
                R.layout.fragment_posts,container,false)
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
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="인기게시물"

        super.onResume()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
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
            viewModel.getHotPosts(lastpostnum,lastposthot,gpsTracker.latitude,gpsTracker.longitude,20,api)
        }
        else{
            viewModel.getHotPosts(lastpostnum,lastposthot,null,null,20,api)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}