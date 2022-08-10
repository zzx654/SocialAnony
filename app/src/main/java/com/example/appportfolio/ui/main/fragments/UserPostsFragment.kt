package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
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
class UserPostsFragment:BasePostFragment(),MenuProvider {
    private val userid:Int
        get() = arguments?.getInt("userid",0)!!
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: OthersProfileViewModel by viewModels()
            return vm
        }
    private val viewModel: OthersProfileViewModel
        get() = basePostViewModel as OthersProfileViewModel

    override fun setupRecyclerView()=binding.rvPosts.apply{
        adapter=postAdapter
        layoutManager= LinearLayoutManager(requireContext())
        addOnScrollListener(scrollListener)
        setHasFixedSize(true)
        setItemViewCacheSize(20)
        itemAnimator=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).setToolBarVisible("userPostsFragment")
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
        val curPosts=postAdapter?.currentList
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
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="전체 게시물"
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    }
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
                true
            }
            else->false
        }
    }
}