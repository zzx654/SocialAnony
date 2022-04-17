package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.GpsTracker
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

abstract class BasePostFragment(
    layoutId:Int
): Fragment(layoutId) {
    lateinit var vmAuth: AuthViewModel
    protected abstract val basePostViewModel: BasePostViewModel
    protected abstract val postAdapter:PostAdapter
    protected abstract val scrollView:NestedScrollView
    protected abstract val scrollTool:FloatingActionButton
    protected abstract val rvPosts:RecyclerView
    protected abstract val loadMoreProgressBar:ProgressBar
    protected abstract val loadProgressBar:ProgressBar
    protected abstract val srLayout:SwipeRefreshLayout
    var isLoading=false
    var isLast=false
    var beforeitemssize=0
    lateinit var api: MainApi

    lateinit var gpsTracker: GpsTracker
    @Inject
    lateinit var userPreferences: UserPreferences
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        srLayout.setOnRefreshListener {
            postAdapter.differ.submitList(listOf())
            basePostViewModel.clearposts()
        }
        scrollTool.setOnClickListener {
            scrollTopOrRefresh()
        }
        scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            if(!v.canScrollVertically(1)){
                if(!isLoading&&beforeitemssize!=postAdapter.posts.size) {
                    loadNewPosts()
                }
            }
            if(!v.canScrollVertically(-1)){
                scrollTool.setImageDrawable(context?.let {
                    AppCompatResources.getDrawable(
                        it,
                        R.drawable.ic_refresh
                    )
                })
            }
            else{
                scrollTool.setImageDrawable(context?.let {
                    AppCompatResources.getDrawable(
                        it,
                        R.drawable.ic_totop
                    )
                })
            }
        }
        postAdapter.setOnPostClickListener {
            basePostViewModel.getSelectedPost(it.postid,gpsTracker.latitude,gpsTracker.longitude,api)
        }
        subscribeToObserver()
    }
    private fun setupRecyclerView()=rvPosts.apply{
        adapter=postAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
    }
    fun init()
    {
        gpsTracker= GpsTracker(requireContext())
        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
        }
        api= RemoteDataSource().buildApi(
            MainApi::class.java,
            runBlocking { userPreferences.authToken.first() })
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        gpsTracker= GpsTracker(requireContext())
    }
    open fun loadNewPosts()
    {
    }
    open fun refreshPosts()
    {
    }
    open fun navigateToPostFragment(post: Post)
    {

    }
    private fun subscribeToObserver()
    {
        basePostViewModel.getPostResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            when(it.resultCode)
            {
                100->Toast.makeText(requireActivity(),"삭제된 게시물입니다",Toast.LENGTH_SHORT).show()
                400->Toast.makeText(requireActivity(),"차단당하거나 차단한 유저의 게시물입니다",Toast.LENGTH_SHORT).show()
                else->navigateToPostFragment(it.posts[0])
            }
        })
        basePostViewModel.beforesize.observe(viewLifecycleOwner){
            beforeitemssize=it
        }

        basePostViewModel.curposts.observe(viewLifecycleOwner){
            if(it.isEmpty())
            {
                isLast=false
                refreshPosts()
            }
            else
            {
                beforeitemssize=postAdapter.posts.size
                basePostViewModel.setbeforeSize(postAdapter.posts.size)
                loadMoreProgressBar.visibility=View.GONE
                postAdapter.differ.submitList(it)
                isLoading=false
            }

        }
        basePostViewModel.getPostsResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onLoading={
                isLoading=true
                if(postAdapter.differ.currentList.isEmpty())
                {
                    if(!srLayout.isRefreshing)
                    {
                        scrollView.visibility=View.GONE
                        loadProgressBar.visibility=View.VISIBLE
                    }

                }
                else{
                    if(!srLayout.isRefreshing)
                        loadMoreProgressBar.visibility=View.VISIBLE
                }

            },
            onError = {
                if(postAdapter.differ.currentList.isEmpty()) {
                    loadProgressBar.visibility = View.GONE
                    snackbar(it)
                }
                else{
                    loadMoreProgressBar.visibility=View.GONE
                }
            }
        ){

            if(!srLayout.isRefreshing)
            {
                loadProgressBar.visibility=View.GONE
                scrollView.visibility=View.VISIBLE
            }
            else
                srLayout.isRefreshing=false

            if(it.resultCode==200) {
                basePostViewModel.addposts(it.posts)
            }
            else{
                isLast=true
                isLoading=false
                loadMoreProgressBar.visibility=View.GONE
            }
        })
    }
    fun scrollTopOrRefresh(){
        if(scrollView.scrollY==0)
        {

            postAdapter.differ.submitList(listOf())
            basePostViewModel.clearposts()
        }
        else
        {
            scrollView.fullScroll(ScrollView.FOCUS_UP)
        }
    }
}