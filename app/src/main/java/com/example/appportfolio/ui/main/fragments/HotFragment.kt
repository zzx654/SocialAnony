package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.adapters.DividerAdapter
import com.example.appportfolio.adapters.HorizontalAdapter
import com.example.appportfolio.adapters.PostPreviewAdapter
import com.example.appportfolio.adapters.TextHeaderAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentHotBinding
import com.example.appportfolio.other.Constants.AUDIOCONTENT
import com.example.appportfolio.other.Constants.IMAGECONTENT
import com.example.appportfolio.other.Constants.VOTECONTENT
import com.example.appportfolio.other.CustomDecoration
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.GpsTracker
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.HotPersonViewModel
import com.example.appportfolio.ui.main.viewmodel.hotContentsViewModel
import com.example.appportfolio.ui.main.viewmodel.hotPostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class HotFragment: Fragment(R.layout.fragment_hot) {
    lateinit var binding: FragmentHotBinding
    lateinit var vmAuth: AuthViewModel
    lateinit var concatAdapter: ConcatAdapter
    lateinit var hotpersonAdapter:HorizontalAdapter
    lateinit var hotpersonHeaderAdapter: TextHeaderAdapter
    lateinit var hotImagesAdapter:PostPreviewAdapter
    lateinit var hotAudioAdapter:PostPreviewAdapter
    lateinit var hotPostsAdapter:PostPreviewAdapter
    lateinit var hotVotesAdapter: PostPreviewAdapter
    lateinit var hotImagesHeaderAdapter:TextHeaderAdapter
    lateinit var hotAudioHeaderAdapter: TextHeaderAdapter
    lateinit var hotPostsHeaderAdapter:TextHeaderAdapter
    lateinit var hotVoteHeaderAdapter: TextHeaderAdapter
    private val hotPersonViewModel: HotPersonViewModel by viewModels()
    private val vmHotContents: hotContentsViewModel by viewModels()
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
            activity?.run{
                vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
            }
            gpsTracker= GpsTracker(requireContext())
            hotpersonAdapter= HorizontalAdapter(requireContext())
            hotpersonHeaderAdapter=TextHeaderAdapter()
            hotImagesHeaderAdapter=TextHeaderAdapter()
            hotAudioHeaderAdapter= TextHeaderAdapter()
            hotPostsHeaderAdapter= TextHeaderAdapter()
            hotVoteHeaderAdapter= TextHeaderAdapter()
            hotImagesAdapter=PostPreviewAdapter()
            hotPostsAdapter= PostPreviewAdapter()
            hotAudioAdapter= PostPreviewAdapter()
            hotVotesAdapter= PostPreviewAdapter()
            hotImagesAdapter.setOnPostClickLitener { post->
                vmHotContents.getSelectedPost(post.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            hotPostsAdapter.setOnPostClickLitener { post->
                vmHotPosts.getSelectedPost(post.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            hotAudioAdapter.setOnPostClickLitener { post->
                vmHotContents.getSelectedPost(post.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            hotVotesAdapter.setOnPostClickLitener { post->
                vmHotContents.getSelectedPost(post.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            hotpersonAdapter.hotpersonAdapter.setOnPersonClickListener { person->
                if(person.userid!=vmAuth.userid.value!!)
                {
                    curTogglinguser=person.userid!!
                    curselectedfollowing=person.following
                    hotPersonViewModel.checkuser(person.userid!!,api)
                }
            }
            hotpersonHeaderAdapter.title="인기유저"
            hotImagesHeaderAdapter.title="인기사진"
            hotPostsHeaderAdapter.title="인기게시물"
            hotAudioHeaderAdapter.title="인기 음성게시물"
            hotVoteHeaderAdapter.title="인기 투표게시물"
            concatAdapter=ConcatAdapter(hotpersonHeaderAdapter,hotpersonAdapter,DividerAdapter(),hotImagesHeaderAdapter,hotImagesAdapter,DividerAdapter(),
                hotAudioHeaderAdapter,hotAudioAdapter,DividerAdapter(),hotVoteHeaderAdapter,hotVotesAdapter,DividerAdapter(),hotPostsHeaderAdapter,hotPostsAdapter)
            binding.srLayout.setOnRefreshListener {
                hotPersonViewModel.getHotUsers(null,null,api)
            }
            setupRecyclerView()
            if((activity as MainActivity).isConnected!!)
                hotPersonViewModel.getHotUsers(null,null,api)
            else
                showwarn()
            binding.retry.onSingleClick {
                if((activity as MainActivity).isConnected!!)
                {
                    loadingDialog.show()
                    binding.srLayout.visibility=View.VISIBLE
                    binding.tvWarn.visibility=View.GONE
                    binding.retry.visibility=View.GONE
                    hotPersonViewModel.getHotUsers(null,null,api)
                }
            }
            mRootView=binding.root
        }
        subscribeToObserver()
        return mRootView
    }
    private fun showwarn(){
        binding.srLayout.visibility=View.GONE
        binding.tvWarn.text=requireContext().getString(R.string.networkdisdconnected)
        binding.tvWarn.visibility=View.VISIBLE
        binding.retry.visibility=View.VISIBLE
    }
    private fun setupRecyclerView(){
        val customDecoration=CustomDecoration(
            ContextCompat.getDrawable(requireContext(), R.drawable.divider),20f,true)
        binding.rvHot.apply {
            adapter=concatAdapter
            layoutManager= LinearLayoutManager(requireContext())
            itemAnimator=null
            addItemDecoration(customDecoration)
        }
    }
    private fun subscribeToObserver()
    {
        hotPersonViewModel.checkuserResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError ={
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it,
                )
            }
        ){
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                when (it.resultCode) {
                    200 -> {
                        val bundle = Bundle()
                        bundle.putInt("userid", it.userid)
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
            loadingDialog.dismiss()
            SocialApplication.showError(
                binding.root,
                requireContext(),
                (activity as MainActivity).isConnected!!,
                it
            )
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
        hotPersonViewModel.gethotUsersResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError = {
                loadingDialog.dismiss()
                binding.srLayout.isRefreshing=false
                showwarn()

            }
        ){
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                when(it.resultCode){
                    200->{
                        vmHotContents.getHotImages(null,null,gpsTracker.latitude,gpsTracker.longitude,api)
                        hotpersonAdapter.hotpersonlist=it.persons
                        hotpersonHeaderAdapter.loadmoreVis=true
                        hotpersonHeaderAdapter.setloadmoreClickListener {
                            (activity as MainActivity).replaceFragment("hotUsersFragment",HotUsersFragment(),null)
                        }
                        hotpersonAdapter.hotpersonAdapter.submitList(it.persons)
                    }
                    else-> {
                        binding.srLayout.isRefreshing=false
                        //Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        vmHotContents.getHotImagesResponse.observe(viewLifecycleOwner,Event.EventObserver(

            onError={
                loadingDialog.dismiss()
              showwarn()
            }
        ){
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                vmHotContents.getHotAudio(null,null,gpsTracker.latitude,gpsTracker.longitude,api)
                when(it.resultCode)
                {
                    200->{
                        //vmHotContents.getHotAudio(null,null,gpsTracker.latitude,gpsTracker.longitude,api)
                        hotImagesHeaderAdapter.loadmoreVis=true
                        hotImagesHeaderAdapter.setloadmoreClickListener {
                            val bundle=Bundle()
                            bundle.putInt("contenttype",IMAGECONTENT)
                            (activity as MainActivity).replaceFragment("hotContentsFragment",HotContentsFragment(),bundle)
                        }
                        hotImagesAdapter.submitList(it.posts)
                    }
                    else->{
                        binding.srLayout.isRefreshing=false
                        //Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }

            }

        })
        vmHotContents.getHotAudioResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                loadingDialog.dismiss()
                binding.srLayout.isRefreshing=false
                showwarn()
            }
        ){
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                vmHotContents.getHotVotes(null,null,gpsTracker.latitude,gpsTracker.longitude,api)
                when(it.resultCode)
                {

                    200->{
                        //vmHotContents.getHotVotes(null,null,gpsTracker.latitude,gpsTracker.longitude,api)
                        hotAudioHeaderAdapter.loadmoreVis=true
                        hotAudioHeaderAdapter.setloadmoreClickListener {
                            val bundle=Bundle()
                            bundle.putInt("contenttype", AUDIOCONTENT)
                            (activity as MainActivity).replaceFragment("hotContentsFragment",HotContentsFragment(),bundle)
                        }
                        hotAudioAdapter.submitList(it.posts)
                    }
                    else->{
                        binding.srLayout.isRefreshing=false
                        //Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        })
        vmHotContents.getHotVoteResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                loadingDialog.dismiss()
                binding.srLayout.isRefreshing=false
                showwarn()
            }
        ){

            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                vmHotPosts.getHotPosts(null,null,gpsTracker.latitude,gpsTracker.longitude,10,api)
                when(it.resultCode)
                {
                    200->{

                        hotVoteHeaderAdapter.loadmoreVis=true
                        hotVoteHeaderAdapter.setloadmoreClickListener {
                            val bundle=Bundle()
                            bundle.putInt("contenttype", VOTECONTENT)

                            (activity as MainActivity).replaceFragment("hotContentsFragment",HotContentsFragment(),bundle)
                        }
                        //hotImagesHeaderAdapter.notifyDataSetChanged()
                        hotVotesAdapter.submitList(it.posts)
                    }
                    else->{
                        binding.srLayout.isRefreshing=false
                        //Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        })
        vmHotPosts.getPostsResponse.observe(viewLifecycleOwner,Event.EventObserver(

            onError={
                loadingDialog.dismiss()
                showwarn()
            }
        ){
            loadingDialog.dismiss()
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
                        concatAdapter.notifyDataSetChanged()
                        hotPostsAdapter.submitList(it.posts)
                    }
                    else->{
                        binding.srLayout.isRefreshing=false
                        //.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        })
    }
}