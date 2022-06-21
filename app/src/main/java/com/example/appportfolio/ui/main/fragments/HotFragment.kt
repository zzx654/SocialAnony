package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.HorizontalAdapter
import com.example.appportfolio.adapters.PostPreviewAdapter
import com.example.appportfolio.adapters.TextHeaderAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentHotBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.GpsTracker
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.HotPersonViewModel
import com.example.appportfolio.ui.main.viewmodel.hotImagesViewModel
import com.example.appportfolio.ui.main.viewmodel.hotPostViewModel
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
    private var curselectedfollowing:Int=0
    private var curTogglinguser=0
    @Inject
    lateinit var userPreferences: UserPreferences
    @Inject
    lateinit var loadingDialog: LoadingDialog
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
            hotImagesAdapter.setOnPostClickLitener { post->
                vmHotImages.getSelectedPost(post.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            hotPostsAdapter.setOnPostClickLitener { post->
                vmHotPosts.getSelectedPost(post.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            hotpersonAdapter.hotpersonAdapter.setOnPersonClickListener { person->
                curTogglinguser=person.userid!!
                curselectedfollowing=person.following
                hotPersonViewModel.checkuser(person.userid!!,api)
            }
            hotpersonHeaderAdapter.title="인기유저"
            hotImagesHeaderAdapter.title="인기사진"
            hotPostsHeaderAdapter.title="인기게시물"
            concatAdapter=ConcatAdapter(hotpersonHeaderAdapter,hotpersonAdapter,hotImagesHeaderAdapter,hotImagesAdapter,hotPostsHeaderAdapter,hotPostsAdapter)
            binding.srLayout.setOnRefreshListener {
                hotPersonViewModel.getHotUsers(null,null,api)
            }
            setupRecyclerView()

            mRootView=binding.root
        }
        subscribeToObserver()

        hotPersonViewModel.getHotUsers(null,null,api)

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
        hotPersonViewModel.checkuserResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError ={
                snackbar(it)
            }
        ){
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                when (it.resultCode) {
                    200 -> {
                        val bundle = Bundle()
                        bundle.putInt("userid", it.value)
                        bundle.putInt("follow", curselectedfollowing)
                        bundle.putString("from", "HotFragment")
                        (activity as MainActivity).replaceFragment(
                            "othersProfileFragment",
                            OthersProfileFragment(),
                            bundle
                        )
                    }
                    400 -> {
                        Toast.makeText(requireContext(), "탈퇴한 회원입니다", Toast.LENGTH_SHORT).show()
                    }
                    500 -> {
                        Toast.makeText(requireContext(), "해당유저를 차단했거나 차단당했습니다", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
        vmHotPosts.getPostResponse.observe(viewLifecycleOwner,Event.EventObserver(
        onLoading={
            loadingDialog.show()
        },
        onError={
            snackbar(it)
            loadingDialog.dismiss()
        }
    ){
        loadingDialog.dismiss()
        SocialApplication.handleResponse(requireContext(), it.resultCode) {
            when (it.resultCode) {
                100 -> Toast.makeText(requireActivity(), "삭제된 게시물입니다", Toast.LENGTH_SHORT).show()
                400 -> Toast.makeText(
                    requireActivity(),
                    "차단당하거나 차단한 유저의 게시물입니다",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    val bundle=Bundle()
                    bundle.putParcelable("post",it.posts[0])
                    (activity as MainActivity).replaceFragment("postFragment",PostFragment(),bundle)
                }
            }
        }
    })
        vmHotImages.getPostResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                loadingDialog.show()
            },
            onError={
                snackbar(it)
                loadingDialog.dismiss()
            }
        ){
            loadingDialog.dismiss()
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                when (it.resultCode) {
                    100 -> Toast.makeText(requireActivity(), "삭제된 게시물입니다", Toast.LENGTH_SHORT)
                        .show()
                    400 -> Toast.makeText(
                        requireActivity(),
                        "차단당하거나 차단한 유저의 게시물입니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> {
                        val bundle=Bundle()
                        bundle.putParcelable("post",it.posts[0])
                        (activity as MainActivity).replaceFragment("postFragment",PostFragment(),bundle)
                    }
                }
            }
        })
        hotPersonViewModel.gethotUsersResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError = {
                snackbar(it)
                binding.srLayout.isRefreshing=false
            }
        ){
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                when(it.resultCode){
                    200->{
                        vmHotImages.getHotImages(null,null,gpsTracker.latitude,gpsTracker.longitude,10,api)
                        hotpersonAdapter.hotpersonlist=it.persons
                        hotpersonHeaderAdapter.loadmoreVis=true
                        hotpersonHeaderAdapter.setloadmoreClickListener {
                            (activity as MainActivity).replaceFragment("hotUsersFragment",HotUsersFragment(),null)
                        }
                        //hotpersonHeaderAdapter.notifyDataSetChanged()
                        //hotpersonAdapter.notifyDataSetChanged()
                        hotpersonAdapter.hotpersonAdapter.submitList(it.persons)


                    }
                    else-> {
                        binding.srLayout.isRefreshing=false
                        Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        vmHotImages.getPostsResponse.observe(viewLifecycleOwner,Event.EventObserver(

            onError={
                binding.srLayout.isRefreshing=false
                snackbar(it)
            }
        ){
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                when(it.resultCode)
                {
                    200->{
                        vmHotPosts.getHotPosts(null,null,gpsTracker.latitude,gpsTracker.longitude,10,api)
                        hotImagesHeaderAdapter.loadmoreVis=true
                        hotImagesHeaderAdapter.setloadmoreClickListener {
                            (activity as MainActivity).replaceFragment("hotImagesFragment",HotImagesFragment(),null)
                        }
                        //hotImagesHeaderAdapter.notifyDataSetChanged()
                        hotImagesAdapter.submitList(it.posts)
                    }
                    else->{
                        binding.srLayout.isRefreshing=false
                        Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }

            }

        })
        vmHotPosts.getPostsResponse.observe(viewLifecycleOwner,Event.EventObserver(

            onError={
                snackbar(it)
            }
        ){
            if(binding.srLayout.isRefreshing)
                binding.srLayout.isRefreshing=false
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                when(it.resultCode)
                {
                    200->{
                        hotPostsHeaderAdapter.loadmoreVis=true
                        hotPostsHeaderAdapter.setloadmoreClickListener {
                            (activity as MainActivity).replaceFragment("hotPostsFragment",HotPostsFragment(),null)
                        }
                        //hotPostsHeaderAdapter.notifyDataSetChanged()
                        concatAdapter.notifyDataSetChanged()
                        hotPostsAdapter.submitList(it.posts)
                    }
                    else->{
                        binding.srLayout.isRefreshing=false
                        Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        })
    }



}