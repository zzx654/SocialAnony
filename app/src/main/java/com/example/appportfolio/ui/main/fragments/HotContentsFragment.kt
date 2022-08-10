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
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.databinding.FragmentPostsBinding
import com.example.appportfolio.other.Constants.AUDIOCONTENT
import com.example.appportfolio.other.Constants.IMAGECONTENT
import com.example.appportfolio.other.Constants.VOTECONTENT
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.hotContentsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HotContentsFragment: BasePostFragment(),MenuProvider {
    private var contenttypeStr:String?=null
    private val contentType:Int?
        get() = arguments?.getInt("contenttype")

    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: hotContentsViewModel by viewModels()
            return vm
        }

    private val viewModel: hotContentsViewModel
        get() = basePostViewModel as hotContentsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).setToolBarVisible("hotContentsFragment")
        contentType?.let{
            contenttypeStr=when(contentType){
                IMAGECONTENT->"IMAGE"
                VOTECONTENT->"VOTE"
                AUDIOCONTENT->"AUDIO"
                else->null
            }
        }
    }
    override fun loadNewPosts() {
        getPosts()
    }

    override fun refreshPosts() {
        getPosts(true)
    }
    override fun onResume() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        when(contentType){
            IMAGECONTENT->(activity as MainActivity).binding.title.text="인기사진"
            VOTECONTENT->(activity as MainActivity).binding.title.text="인기 투표게시물"
            AUDIOCONTENT->(activity as MainActivity).binding.title.text="인기 음성게시물"
        }

        super.onResume()

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
    fun getPosts(refresh:Boolean=false)
    {
        var lastpostnum:Int?=null
        var lastposthot:Int?=null
        val curPosts=postAdapter?.currentList
        if(!refresh)
        {
            if (curPosts != null) {
                if(curPosts.isNotEmpty()) {
                    val lastPost=curPosts.last()
                    lastpostnum=lastPost.postnum
                    lastposthot=lastPost.commentcount+lastPost.likecount
                }
            }
        }
        if(SocialApplication.checkGeoPermission(requireContext()))
        {
            contenttypeStr?.let{ type->
                viewModel.getHotContents(lastpostnum,lastposthot,gpsTracker.latitude,gpsTracker.longitude,20,type,api)
            }

        }
        else{
            contenttypeStr?.let { type ->
                viewModel.getHotContents(lastpostnum, lastposthot, null, null, 20,type, api)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}