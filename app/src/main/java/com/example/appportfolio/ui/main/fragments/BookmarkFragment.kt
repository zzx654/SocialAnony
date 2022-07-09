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
import com.example.appportfolio.ui.main.viewmodel.BookmarkViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BookmarkFragment :BasePostFragment(R.layout.fragment_posts) {
    lateinit var binding: FragmentPostsBinding
    private lateinit var bookmarkAdapter: PostAdapter
    override val scrollTool: FloatingActionButton
        get() = binding.fbScrollTool
    override val rvPosts: RecyclerView
        get() = binding.rvPosts
    override val loadProgressBar: ProgressBar
        get() = binding.loadProgressBar
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: BookmarkViewModel by viewModels()
            return vm
        }
    override val postAdapter: PostAdapter
        get() = bookmarkAdapter
    override val tvWarn: TextView
        get() = binding.tvWarn
    override val retry: TextView
        get() = binding.retry
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    private val viewModel: BookmarkViewModel
        get() = basePostViewModel as BookmarkViewModel
    private var mRootView:View?=null
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_posts, container, false
            )
            (activity as MainActivity).setToolBarVisible("bookmarkFragment")
            bookmarkAdapter = PostAdapter()
            setView()
            refreshPosts()
            mRootView=binding.root
        }

        return mRootView

    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="북마크"
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
            if(curPosts.isNotEmpty())
            {
                val lastPost=curPosts.last()
                lastpostnum=lastPost.postnum
                lastpostdate=lastPost.date
            }
        }
        if(SocialApplication.checkGeoPermission(requireContext()))
        {
            viewModel.getBookmarkedPost(lastpostnum,lastpostdate,gpsTracker.latitude,gpsTracker.longitude,api)
        }
        else{
            viewModel.getBookmarkedPost(lastpostnum,lastpostdate,null,null,api)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }


}