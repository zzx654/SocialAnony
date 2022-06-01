package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.MessageData
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.other.Constants.PAGE_SIZE
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.GpsTracker
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
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
    protected abstract val scrollTool:FloatingActionButton
    protected abstract val rvPosts:RecyclerView
    protected abstract val loadProgressBar:ProgressBar
    protected abstract val srLayout:SwipeRefreshLayout
    var isLoading=false
    var isScrolling=false
    var isLast=false
    var isScrollTop=true
    var beforeitemssize=0
    lateinit var api: MainApi

    lateinit var gpsTracker: GpsTracker
    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObserver()

    }
    @RequiresApi(Build.VERSION_CODES.M)
    protected fun setView()
    {
        activity?.runOnUiThread {
            srLayout.setOnRefreshListener {
                isLast=false
                refreshPosts()
            }
            scrollTool.setOnClickListener {
                scrollTopOrRefresh()
            }
            postAdapter.setOntagClickListener { tag->
                val bundle=Bundle()
                bundle.putString("tag",tag)
                (activity as MainActivity).replaceFragment("tagPostsFragment",TagPostsFragment(),bundle)

            }
            postAdapter.setOnPostClickListener {
                basePostViewModel.getSelectedPost(it.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            setupRecyclerView()
        }
    }
    val scrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(!recyclerView.canScrollVertically(1)&&(beforeitemssize!=postAdapter.differ.currentList.size)&&!isLoading&&isScrolling&&!isLast&&postAdapter.differ.currentList.size>=PAGE_SIZE){
                isScrolling=false
                loadNewPosts()
            }
            if(!recyclerView.canScrollVertically(-1))
            {
                isScrollTop=true
                scrollTool.setImageDrawable(context?.let {
                    AppCompatResources.getDrawable(
                        it,
                        R.drawable.ic_refresh
                    )
                })
            }
            else
            {
                isScrollTop=false
                scrollTool.setImageDrawable(context?.let {
                    AppCompatResources.getDrawable(
                        it,
                        R.drawable.ic_totop
                    )
                })
            }
        }
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling=true
        }
    }
    protected fun setupRecyclerView()=rvPosts.apply{
        adapter=postAdapter
        layoutManager= LinearLayoutManager(requireContext())
        addOnScrollListener(this@BasePostFragment.scrollListener)
        setHasFixedSize(true)
        setItemViewCacheSize(20)
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
    private fun navigateToPostFragment(post: Post)
    {
        val bundle=Bundle()
        bundle.putParcelable("post",post)
        (activity as MainActivity).replaceFragment("postFragment",PostFragment(),bundle)
    }
    protected fun subscribeToObserver()
    {
        basePostViewModel.getPostResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
              loadingDialog.show()
            },
            onError={
                snackbar(it)
                loadingDialog.dismiss()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode){
                when(it.resultCode)
                {
                    100->Toast.makeText(requireActivity(),"삭제된 게시물입니다",Toast.LENGTH_SHORT).show()
                    400->Toast.makeText(requireActivity(),"차단당하거나 차단한 유저의 게시물입니다",Toast.LENGTH_SHORT).show()
                    else->navigateToPostFragment(it.posts[0])
                }
            }

        })
        basePostViewModel.getPostsResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onLoading={
                isLoading=true
                if(!srLayout.isRefreshing)
                {
                    if(postAdapter.differ.currentList.isEmpty()) {
                        loadProgressBar.visibility = View.VISIBLE
                    }
                    else {
                        if(postAdapter.posts.size>0)
                        {
                            postAdapter.posts+=listOf(Post(0,null,0,null,null,"",null,
                                null,null,",null,",",","","","",0,0,null,null,null,"",0))


                            postAdapter.notifyItemInserted(postAdapter.itemCount)
                        }

                    }
                }
            },
            onError = {
                snackbar(it)
                if(!srLayout.isRefreshing)
                {
                    if(postAdapter.differ.currentList.isEmpty())
                        loadProgressBar.visibility=View.GONE
                    else{
                        var currentllist=postAdapter.differ.currentList.toMutableList()
                        currentllist.removeLast()
                        postAdapter.differ.submitList(currentllist)
                    }

                }
                else
                    srLayout.isRefreshing=false
            }
        ){
            var currentllist=postAdapter.differ.currentList.toMutableList()
            if(!srLayout.isRefreshing)
            {
                if(postAdapter.differ.currentList.isEmpty())
                    loadProgressBar.visibility=View.GONE
                else if(postAdapter.differ.currentList.size>0) {
                    currentllist.removeLast()
                }
            }
            handleResponse(requireContext(),it.resultCode){
                var posts=currentllist.toList()
                if(it.resultCode==200) {
                    if(srLayout.isRefreshing)
                    {
                        beforeitemssize=0
                        postAdapter.differ.submitList(it.posts)
                        srLayout.isRefreshing=false
                    }
                    else
                    {
                        if(posts.isEmpty())
                            rvPosts.scrollToPosition(0)
                        beforeitemssize=posts.size
                        posts+=it.posts

                        postAdapter.differ.submitList(posts)

                    }
                    isLoading=false
                }
                else{
                    postAdapter.differ.submitList(posts)
                    if(posts.size>0)
                        snackbar("더이상 표시할 게시물이 없습니다")
                    isLast=true
                    isLoading=false
                    loadProgressBar.visibility=View.GONE
                    srLayout.isRefreshing=false
                }
            }
        })
    }
    fun scrollTopOrRefresh(){
        if(isScrollTop) {
            isLast=false
            srLayout.isRefreshing=true
            refreshPosts()
        }
        else
            rvPosts.smoothScrollToPosition(0)

    }
}