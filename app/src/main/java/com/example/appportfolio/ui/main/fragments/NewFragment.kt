package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.databinding.FragmentPostsBinding
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.newPostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewFragment: BasePostFragment(){

    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: newPostViewModel by viewModels()
            return vm
        }

    private val viewModel: newPostViewModel
        get() = basePostViewModel as newPostViewModel

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
            viewModel.getNewPosts(lastpostnum,lastpostdate,gpsTracker.latitude,gpsTracker.longitude,api)
        }
        else{
            viewModel.getNewPosts(lastpostnum,lastpostdate,null,null,api)
        }
    }
}