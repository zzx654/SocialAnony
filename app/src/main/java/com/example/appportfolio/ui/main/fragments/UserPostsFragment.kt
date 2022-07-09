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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.databinding.FragmentPostsBinding
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.OthersProfileViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UserPostsFragment:BasePostFragment(R.layout.fragment_posts) {
    lateinit var binding: FragmentPostsBinding
    private var mRootView: View?=null
    private lateinit var userpostAdapter: PostAdapter
    private val userid:Int
        get() = arguments?.getInt("userid",0)!!
    override val scrollTool: FloatingActionButton
        get() = binding.fbScrollTool
    override val rvPosts: RecyclerView
        get() = binding.rvPosts
    override val loadProgressBar: ProgressBar
        get() = binding.loadProgressBar
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: OthersProfileViewModel by viewModels()
            return vm
        }
    override val tvWarn: TextView
        get() = binding.tvWarn
    override val retry: TextView
        get() = binding.retry
    override val postAdapter: PostAdapter
        get() = userpostAdapter
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    private val viewModel: OthersProfileViewModel
        get() = basePostViewModel as OthersProfileViewModel

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {

            binding= DataBindingUtil.inflate(inflater,
                R.layout.fragment_posts,container,false)
            userpostAdapter=PostAdapter()
            (activity as MainActivity).setToolBarVisible("userPostsFragment")
            setView()
            refreshPosts()
            mRootView=binding.root
        }

        return mRootView
    }
    override fun setupRecyclerView()=binding.rvPosts.apply{
        adapter=userpostAdapter
        layoutManager= LinearLayoutManager(requireContext())
        addOnScrollListener(scrollListener)
        setHasFixedSize(true)
        setItemViewCacheSize(20)
        itemAnimator=null
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
        var lastpostdate:String?=null
        val curPosts=postAdapter.currentList
        if(!refresh)
        {
            if(!curPosts.isNullOrEmpty())
            {
                val lastPost=curPosts.last()
                lastpostnum=lastPost.postnum
                lastpostdate=lastPost.date
            }
        }
        if(SocialApplication.checkGeoPermission(requireContext()))
        {
            viewModel.getuserPosts(userid,lastpostnum,lastpostdate,gpsTracker.latitude,gpsTracker.longitude,20,api)
        }
        else{
            viewModel.getuserPosts(userid,lastpostnum,lastpostdate,null,null,20,api)
        }
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="전체 게시물"

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

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}