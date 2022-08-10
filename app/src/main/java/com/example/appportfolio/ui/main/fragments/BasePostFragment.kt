package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.ui.auth.viewmodel.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.FragmentPostsBinding
import com.example.appportfolio.other.Constants.PAGE_SIZE
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.GpsTracker
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

abstract class BasePostFragment: Fragment(R.layout.fragment_posts) {
    lateinit var binding:FragmentPostsBinding
    lateinit var vmAuth: AuthViewModel
    protected abstract val basePostViewModel: BasePostViewModel
    private var _postAdapter:PostAdapter?=null
    val postAdapter get() = _postAdapter
    protected val scrollTool get() = binding.fbScrollTool
    protected val rvPosts get() = binding.rvPosts
    protected val loadProgressBar get() = binding.loadProgressBar
    protected val srLayout get()=binding.sr
    protected val tvWarn get() = binding.tvWarn
    protected val retry get()= binding.retry
    var isLoading=false
    var isScrolling=false
    var isLast=false
    var isScrollTop=true
    lateinit var api: MainApi
    protected var mRootView:View?=null
    lateinit var gpsTracker: GpsTracker
    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

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
            setView()
            mRootView=binding.root
            refreshPosts()
        }
        return mRootView
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
                if((activity as MainActivity).isConnected!!){
                    isLast=false
                    refreshPosts()
                }
                else{
                    srLayout.isRefreshing=false
                }


            }
            scrollTool.setOnClickListener {
                scrollTopOrRefresh()
            }
            postAdapter?.setOntagClickListener { tag->
                val bundle=Bundle()
                bundle.putString("tag",tag)
                (activity as MainActivity).replaceFragment("tagPostsFragment",TagPostsFragment(),bundle)

            }
            postAdapter?.setOnPostClickListener {
                basePostViewModel.getSelectedPost(it.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            retry.onSingleClick {
                if((activity as MainActivity).isConnected!!)
                {
                    retry.visibility=View.GONE
                    tvWarn.visibility=View.GONE
                    srLayout.visibility=View.VISIBLE
                    (activity as MainActivity).setAccessToken()
                    refreshPosts()
                }
            }
            setupRecyclerView()
        }
    }
    protected val scrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition =
                (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            val totalItemCount = recyclerView.adapter!!.itemCount - 1

            if(postAdapter?.currentList!!.isNotEmpty())
            {
                if(!recyclerView.canScrollVertically(1)&&(lastVisibleItemPosition == totalItemCount)&&postAdapter?.currentList?.last()?.postid!=null&&!isLoading&&isScrolling&&!isLast&&postAdapter?.currentList!!.size>=PAGE_SIZE){
                isScrolling=false
                loadNewPosts()
                }
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
    protected open fun setupRecyclerView()=rvPosts.apply{
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
            vmAuth= ViewModelProvider(this)[AuthViewModel::class.java]
        }
        api= RemoteDataSource().buildApi(
            MainApi::class.java,
            runBlocking { userPreferences.authToken.first() })
        _postAdapter=PostAdapter()
    }

    override fun onResume() {
        super.onResume()
        gpsTracker= GpsTracker(requireContext())
    }
    abstract fun loadNewPosts()
    abstract fun refreshPosts()



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
                    if(postAdapter?.currentList!!.isEmpty()) {
                        loadProgressBar.visibility = View.VISIBLE
                    }
                    else {
                        if(postAdapter?.currentList!!.isNotEmpty())
                        {
                            var templist=postAdapter?.currentList?.toList()
                            templist = templist?.plus(
                                listOf(Post(0,null,0,null,null,"",null,
                                    null,null,",null,",",","","","",0,0,null,null,null,"",0))
                            )
                            postAdapter?.submitList(templist)
                        }

                    }
                }
            },
            onError = {
                if(!(activity as MainActivity).isConnected!!){
                    if(postAdapter?.currentList!!.isEmpty())
                    {
                        srLayout.visibility=View.GONE
                        tvWarn.text=requireContext().getString(R.string.networkdisdconnected)
                        tvWarn.visibility=View.VISIBLE
                        retry.visibility=View.VISIBLE
                    }
                }
                else
                    snackbar("$it\n 잠시후 다시 시도해주세요",true,"확인")
                if(!srLayout.isRefreshing)
                {
                    if(postAdapter?.currentList!!.isEmpty())
                        loadProgressBar.visibility=View.GONE
                    else{
                        val currentllist=postAdapter?.currentList!!.toList()
                        postAdapter?.submitList(currentllist.filter { post-> post.postid!=null })
                    }

                }
                else
                    srLayout.isRefreshing=false
            }
        ){
            val currentllist=postAdapter?.currentList!!.toMutableList()
            if(!srLayout.isRefreshing&&postAdapter?.currentList!!.isEmpty())
                    loadProgressBar.visibility=View.GONE

            handleResponse(requireContext(),it.resultCode){
                var posts=currentllist.toList()
                if(it.resultCode==200) {
                    if(srLayout.isRefreshing)
                    {
                        postAdapter?.submitList(it.posts)
                        srLayout.isRefreshing=false
                    }
                    else
                    {
                        if(posts.isEmpty())
                            rvPosts.scrollToPosition(0)
                        posts+=it.posts
                        postAdapter?.submitList(posts.filter { post->  post.postid!=null })
                    }
                    isLoading=false
                }
                else{
                    postAdapter?.submitList(posts.filter { post->post.postid!=null })
                    if(posts.isNotEmpty())
                        snackbar("더이상 표시할 게시물이 없습니다")
                    isLast=true
                    isLoading=false
                    loadProgressBar.visibility=View.GONE
                    srLayout.isRefreshing=false
                }
            }
        })
    }
    private fun scrollTopOrRefresh(){
        if(isScrollTop) {
            isLast=false
            srLayout.isRefreshing=true
            refreshPosts()
        }
        else
            rvPosts.smoothScrollToPosition(0)
    }
}