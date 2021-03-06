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
class HotContentsFragment: BasePostFragment(R.layout.fragment_posts),MenuProvider {
    lateinit var binding: FragmentPostsBinding
    private lateinit var hotContentsAdapter: PostAdapter
    private var contenttypeStr:String?=null
    private val contentType:Int?
        get() = arguments?.getInt("contenttype")
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
            val vm: hotContentsViewModel by viewModels()
            return vm
        }
    override val postAdapter: PostAdapter
        get() = hotContentsAdapter
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    private val viewModel: hotContentsViewModel
        get() = basePostViewModel as hotContentsViewModel
    private var mRootView: View?=null
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            (activity as MainActivity).setToolBarVisible("hotContentsFragment")
            binding= DataBindingUtil.inflate<FragmentPostsBinding>(inflater,
                R.layout.fragment_posts,container,false)
            contentType?.let{
                contenttypeStr=when(contentType){
                    IMAGECONTENT->"IMAGE"
                    VOTECONTENT->"VOTE"
                    AUDIOCONTENT->"AUDIO"
                    else->null
                }
            }
            hotContentsAdapter= PostAdapter()
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
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        when(contentType){
            IMAGECONTENT->(activity as MainActivity).binding.title.text="????????????"
            VOTECONTENT->(activity as MainActivity).binding.title.text="?????? ???????????????"
            AUDIOCONTENT->(activity as MainActivity).binding.title.text="?????? ???????????????"
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
        val curPosts=postAdapter.currentList
        if(!refresh)
        {
            if(curPosts.isNotEmpty())
            {
                val lastPost=curPosts.last()
                lastpostnum=lastPost.postnum
                lastposthot=lastPost.commentcount+lastPost.likecount
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