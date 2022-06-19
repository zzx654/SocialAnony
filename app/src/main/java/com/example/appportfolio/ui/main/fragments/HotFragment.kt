package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.HorizontalAdapter
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.adapters.PostPreviewAdapter
import com.example.appportfolio.adapters.TextHeaderAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.FragmentHotBinding
import com.example.appportfolio.databinding.FragmentPostsBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.main.GpsTracker
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.HotPersonViewModel
import com.example.appportfolio.ui.main.viewmodel.hotImagesViewModel
import com.example.appportfolio.ui.main.viewmodel.hotPostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class HotFragment: Fragment(R.layout.fragment_hot) {
    lateinit var binding: FragmentHotBinding
    lateinit var concatAdapter: ConcatAdapter
    lateinit var hotpersonAdapter:HorizontalAdapter
    lateinit var hotpersonHeaderAdapter: TextHeaderAdapter
    lateinit var hotImagesAdapter:PostPreviewAdapter
    lateinit var hotPostsAdapter:PostPreviewAdapter
    lateinit var hotImagesHeaderAdapter:TextHeaderAdapter
    lateinit var hotPostsHeaderAdapter:TextHeaderAdapter
    private val hotPersonViewModel: HotPersonViewModel by viewModels()
    private val vmHotImages: hotImagesViewModel by viewModels()
    private val vmHotPosts:hotPostViewModel by viewModels()
    lateinit var gpsTracker: GpsTracker
    private var mRootView:View?=null
    lateinit var api: MainApi
    @Inject
    lateinit var userPreferences: UserPreferences
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            binding= DataBindingUtil.inflate<FragmentHotBinding>(inflater,
            R.layout.fragment_hot,container,false)
            api= RemoteDataSource().buildApi(
                MainApi::class.java,
                runBlocking { userPreferences.authToken.first() })
            gpsTracker= GpsTracker(requireContext())
            hotpersonAdapter= HorizontalAdapter(requireContext())
            hotpersonHeaderAdapter=TextHeaderAdapter()
            hotImagesHeaderAdapter=TextHeaderAdapter()
            hotPostsHeaderAdapter= TextHeaderAdapter()
            hotImagesAdapter=PostPreviewAdapter()
            hotPostsAdapter= PostPreviewAdapter()
            hotpersonHeaderAdapter.title="인기유저"
            hotImagesHeaderAdapter.title="인기사진"
            hotPostsHeaderAdapter.title="인기게시물"
            concatAdapter=ConcatAdapter(hotpersonHeaderAdapter,hotpersonAdapter,hotImagesHeaderAdapter,hotImagesAdapter,hotPostsHeaderAdapter,hotPostsAdapter)
            setupRecyclerView()
            subscribeToObserver()
            mRootView=binding.root
        }

        hotPersonViewModel.getHotUsers(null,null,api)
        vmHotImages.getHotImages(null,null,gpsTracker.latitude,gpsTracker.longitude,10,api)
        vmHotPosts.getHotPosts(null,null,gpsTracker.latitude,gpsTracker.longitude,10,api)

        return mRootView
    }
    private fun setupRecyclerView(){
        binding.rvHot.apply {
            adapter=concatAdapter
            layoutManager= LinearLayoutManager(requireContext())
            itemAnimator=null
        }
    }
    private fun subscribeToObserver()
    {
        hotPersonViewModel.gethotUsersResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onLoading={

            },
            onError = {

            }
        ){
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                when(it.resultCode){
                    200->{
                        hotpersonAdapter.hotpersonlist=it.persons
                        hotpersonHeaderAdapter.loadmoreVis=true
                        hotpersonHeaderAdapter.setloadmoreClickListener {
                            (activity as MainActivity).replaceFragment("hotUsersFragment",HotUsersFragment(),null)
                        }
                        hotpersonHeaderAdapter.notifyDataSetChanged()
                        hotpersonAdapter.notifyDataSetChanged()

                    }
                    else-> Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        })
        vmHotImages.getPostsResponse.observe(viewLifecycleOwner,Event.EventObserver(

        ){
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                when(it.resultCode)
                {
                    200->{
                        hotImagesHeaderAdapter.loadmoreVis=true
                        hotImagesHeaderAdapter.setloadmoreClickListener {

                        }
                        hotImagesHeaderAdapter.notifyDataSetChanged()
                        hotImagesAdapter.submitList(it.posts)
                    }
                    else->Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }

            }

        })
        vmHotPosts.getPostsResponse.observe(viewLifecycleOwner,Event.EventObserver(

        ){
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                when(it.resultCode)
                {
                    200->{
                        hotPostsHeaderAdapter.loadmoreVis=true
                        hotPostsHeaderAdapter.setloadmoreClickListener {
                            (activity as MainActivity).replaceFragment("hotPostsFragment",HotPostsFragment(),null)
                        }
                        hotPostsHeaderAdapter.notifyDataSetChanged()
                        println("또뭐가 문제 ${it.posts}")
                        hotPostsAdapter.submitList(it.posts)
                    }
                    else->Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }

            }
        })
    }



}