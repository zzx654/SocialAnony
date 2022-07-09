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
import com.example.appportfolio.other.Constants
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.UserContentsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserContentsFragment:BasePostFragment(R.layout.fragment_posts) {
    lateinit var binding: FragmentPostsBinding
    private lateinit var userContentsAdapter: PostAdapter
    private var contenttypeStr:String?=null
    private val contentType:Int?
        get() = arguments?.getInt("contenttype")
    private val userid:Int
        get() = arguments?.getInt("userid",0)!!
    override val scrollTool: FloatingActionButton
        get() = binding.fbScrollTool
    override val rvPosts: RecyclerView
        get() = binding.rvPosts
    override val loadProgressBar: ProgressBar
        get() = binding.loadProgressBar
    override val tvWarn: TextView
        get() = binding.tvWarn
    override val retry: TextView
        get() = binding.retry
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: UserContentsViewModel by viewModels()
            return vm
        }
    override val postAdapter: PostAdapter
        get() = userContentsAdapter
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    private val viewModel: UserContentsViewModel
        get() = basePostViewModel as UserContentsViewModel
    private var mRootView: View?=null
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            (activity as MainActivity).setToolBarVisible("userContentsFragment")
            binding= DataBindingUtil.inflate(inflater,
                R.layout.fragment_posts,container,false)
            contentType?.let{
                contenttypeStr=when(contentType){
                    Constants.IMAGECONTENT ->"IMAGE"
                    Constants.VOTECONTENT ->"VOTE"
                    Constants.AUDIOCONTENT ->"AUDIO"
                    else->null
                }
            }
            userContentsAdapter= PostAdapter()
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
        when(contentType){
            Constants.IMAGECONTENT ->(activity as MainActivity).binding.title.text="사진게시물"
            Constants.VOTECONTENT ->(activity as MainActivity).binding.title.text="투표게시물"
            Constants.AUDIOCONTENT ->(activity as MainActivity).binding.title.text="음성게시물"
        }

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
            contenttypeStr?.let{ type->
                viewModel.getUserContents(userid,lastpostnum,lastpostdate,gpsTracker.latitude,gpsTracker.longitude,type,api)
            }

        }
        else{
            contenttypeStr?.let{ type->
                viewModel.getUserContents(userid,lastpostnum,lastpostdate,null,null,type,api)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}