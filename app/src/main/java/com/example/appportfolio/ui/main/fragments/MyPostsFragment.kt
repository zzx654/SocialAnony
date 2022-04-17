package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.FragmentNewBinding
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.myPostsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPostsFragment :BasePostFragment(R.layout.fragment_new) {
    lateinit var binding: FragmentNewBinding
    lateinit var mypostsAdapter: PostAdapter
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
            val vm: myPostsViewModel by viewModels()
            return vm
        }
    override val postAdapter: PostAdapter
        get() = mypostsAdapter
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    protected val viewModel: myPostsViewModel
        get() = basePostViewModel as myPostsViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentNewBinding>(inflater,
            R.layout.fragment_new,container,false)
        mypostsAdapter= PostAdapter()
        mypostsAdapter.setOntagClickListener { tag->
            findNavController().navigate(MyPostsFragmentDirections.actionGlobalTagPostsFragment(tag))
        }
        return binding.root

    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="내가 쓴 글"
        super.onResume()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                findNavController().popBackStack()
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

    override fun navigateToPostFragment(post: Post) {
        findNavController().navigate(MyPostsFragmentDirections.actionGlobalPostFragment(post))
    }
    fun getPosts(refresh:Boolean=false)
    {
        var lastpostnum:Int?=null
        var lastpostdate:String?=null
        val curPosts=postAdapter.differ.currentList
        if(!refresh)
        {
            if(!curPosts.isNullOrEmpty())
            {
                val lastPost=curPosts.last()
                lastpostnum=lastPost.postnum
                lastpostdate=lastPost.date
            }
        }
            viewModel.getmyPosts(lastpostnum,lastpostdate,api)

    }
}