package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.DividerAdapter
import com.example.appportfolio.adapters.PostPreviewAdapter
import com.example.appportfolio.adapters.ProfileContainerAdapter
import com.example.appportfolio.adapters.TextHeaderAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentUserprofileBinding
import com.example.appportfolio.other.Constants
import com.example.appportfolio.other.CustomDecoration
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.GpsTracker
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class OthersProfileFragment: Fragment(R.layout.fragment_userprofile) {
    lateinit var api: MainApi
    @Inject
    lateinit var userPreferences: UserPreferences
    lateinit var binding:FragmentUserprofileBinding
    private var mRootView:View?=null
    private val userid:Int
        get() = arguments?.getInt("userid",0)!!
    private val from:String
        get() = arguments?.getString("from")!!
    private var following=0
    lateinit var gpsTracker: GpsTracker
    @Inject
    lateinit var loadingDialog: LoadingDialog
    private lateinit var ImagesHeaderAdapter: TextHeaderAdapter
    private lateinit var AudioHeaderAdapter: TextHeaderAdapter
    private lateinit var VoteHeaderAdapter: TextHeaderAdapter
    private lateinit var EveryHeaderAdapter:TextHeaderAdapter
    private lateinit var ImagesAdapter: PostPreviewAdapter
    private lateinit var AudioAdapter: PostPreviewAdapter
    private lateinit var VotesAdapter: PostPreviewAdapter
    private lateinit var EveryAdapter:PostPreviewAdapter
    private val vmUserProfile:OthersProfileViewModel by viewModels()
    private val vmUserContents:UserContentsViewModel by viewModels()
    private lateinit var vmPerson:BasePersonViewModel
    private lateinit var vmToggle:applyFollowViewModel
    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var profileAdapter:ProfileContainerAdapter
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            vmToggle= ViewModelProvider(requireActivity())[applyFollowViewModel::class.java]
            api= RemoteDataSource().buildApi(
                MainApi::class.java,
                runBlocking { userPreferences.authToken.first() })
            gpsTracker= GpsTracker(requireContext())
            vmPerson = if(from=="searchPersonFragment")
                ViewModelProvider(requireActivity())[SearchPersonViewModel::class.java]
            else
                ViewModelProvider(requireActivity())[MyFollowingViewModel::class.java]
            binding= DataBindingUtil.inflate(inflater,
                R.layout.fragment_userprofile,container,false)
            profileAdapter= ProfileContainerAdapter()
            ImagesHeaderAdapter= TextHeaderAdapter()
            AudioHeaderAdapter= TextHeaderAdapter()
            VoteHeaderAdapter= TextHeaderAdapter()
            EveryHeaderAdapter= TextHeaderAdapter()
            ImagesAdapter= PostPreviewAdapter()
            AudioAdapter= PostPreviewAdapter()
            VotesAdapter= PostPreviewAdapter()
            EveryAdapter= PostPreviewAdapter()
            ImagesAdapter.setOnPostClickLitener { post->
                vmUserContents.getSelectedPost(post.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            AudioAdapter.setOnPostClickLitener { post->
                vmUserContents.getSelectedPost(post.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            VotesAdapter.setOnPostClickLitener { post->
                vmUserContents.getSelectedPost(post.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            EveryAdapter.setOnPostClickLitener { post->
                vmUserContents.getSelectedPost(post.postid!!,gpsTracker.latitude,gpsTracker.longitude,api)
            }
            concatAdapter= ConcatAdapter(profileAdapter,ImagesHeaderAdapter,ImagesAdapter,DividerAdapter(),AudioHeaderAdapter,AudioAdapter,DividerAdapter(),
            VoteHeaderAdapter,VotesAdapter,DividerAdapter(),EveryHeaderAdapter,EveryAdapter)
            (activity as MainActivity).setToolBarVisible("othersProfileFragment")
            following=arguments?.getInt("follow")!!
            profileAdapter.setfollowing(following)
            setupRecyclerView()
            binding.srLayout.setOnRefreshListener {
                vmUserProfile.getuserProfile(userid,api)
            }
            loadingDialog.show()
            mRootView=binding.root
        }


        subscribeToObserver()
        return mRootView
    }

    private fun subscribeToObserver(){
        vmUserContents.getPostResponse.observe(viewLifecycleOwner,Event.EventObserver(
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
        vmPerson.togglefollowResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
                loadingDialog.dismiss()
            },
            onLoading={
                loadingDialog.show()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    if (profileAdapter.followingperson == 1) {
                        profileAdapter.setfollowing(0)
                        profileAdapter.followercount-=1
                        profileAdapter.notifyDataSetChanged()
                        Toast.makeText(
                            requireContext(),
                            "${profileAdapter.usernickname}님 팔로우를 해제했습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        profileAdapter.setfollowing(1)
                        profileAdapter.followercount+=1
                        profileAdapter.notifyDataSetChanged()
                        Toast.makeText(
                            requireContext(),
                            "${profileAdapter.usernickname}님을 팔로우 했습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    profileAdapter.followingperson?.let { it ->
                        vmToggle.setcurtoggle(userid,
                            it
                        )
                    }
                }
            }
        })
        vmUserProfile.requestchatResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
                loadingDialog.dismiss()
            },
            onLoading={
                loadingDialog.show()
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                loadingDialog.dismiss()
                when (it.resultCode) {
                    200 -> Toast.makeText(requireContext(), "대화 요청이 완료되었습니다", Toast.LENGTH_SHORT)
                        .show()
                    300 -> Toast.makeText(
                        requireContext(),
                        "해당유저가 대화요청을 차단한 상태입니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    500 -> Toast.makeText(
                        requireContext(),
                        "해당유저를 차단했거나 차단당한 상태입니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> Toast.makeText(
                        requireContext(),
                        "해당유저에게 이미 대화를 요청했습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        vmUserProfile.getprofileResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                snackbar(it)
                loadingDialog.dismiss()
                binding.srLayout.isRefreshing=false
                if(!(activity as MainActivity).isConnected!!){

                        binding.srLayout.visibility=View.GONE
                        binding.tvWarn.text=requireContext().getString(R.string.networkdisdconnected)
                        binding.tvWarn.visibility=View.VISIBLE
                        binding.retry.visibility=View.VISIBLE

                }
                else
                    snackbar("$it\n 잠시후 다시 시도해주세요",true,"확인")
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    ImagesHeaderAdapter.title="사진"
                    EveryHeaderAdapter.title="전체게시물"
                    AudioHeaderAdapter.title="음성게시물"
                    VoteHeaderAdapter.title="투표게시물"
                    vmUserContents.getUserImages(userid,null,null,gpsTracker.latitude,gpsTracker.longitude,api)
                    profileAdapter.setuserinfo(it.profileimage,it.nickname,it.gender,it.age!!,it.followingcount!!,it.followercount!!,it.postscount!!)
                    (activity as MainActivity).binding.title.text = it.nickname
                    profileAdapter.toolsVis=true
                    it.profileimage?.let{
                        profileAdapter.setOnProfileClickListener {
                            val bundle= Bundle()
                            bundle.putString("image",it)
                            (activity as MainActivity).replaceFragment("imageFragment",
                                ImageFragment(),bundle)
                        }
                    }
                    profileAdapter.setOnFollowingClickListener {
                        it.followingcount.let{
                            val bundle=Bundle()
                            bundle.putInt("userid",userid)
                            bundle.putInt("getInfoType", Constants.FOLLOWING)
                            (activity as MainActivity).replaceFragment("userFollowFragment",UserFollowFragment(),bundle)
                        }

                    }
                    profileAdapter.setOnFollowerClickListener {
                        it.followercount.let{
                            val bundle=Bundle()
                            bundle.putInt("userid",userid)
                            bundle.putInt("getInfoType", Constants.FOLLOWER)
                            (activity as MainActivity).replaceFragment("userFollowFragment",UserFollowFragment(),bundle)
                        }
                    }
                }
                else{
                    Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss()
                }
            }
        })
        with(vmUserContents) {
            getUserImagesResponse.observe(viewLifecycleOwner,Event.EventObserver(
                onError={
                    loadingDialog.dismiss()
                    binding.srLayout.isRefreshing=false
                    if(!(activity as MainActivity).isConnected!!){

                        binding.srLayout.visibility=View.GONE
                        binding.tvWarn.text=requireContext().getString(R.string.networkdisdconnected)
                        binding.tvWarn.visibility=View.VISIBLE
                        binding.retry.visibility=View.VISIBLE

                    }
                    else
                        snackbar("$it\n 잠시후 다시 시도해주세요",true,"확인")
                }
            ){
                handleResponse(requireContext(), it.resultCode) {
                    getUserAudio(userid,null,null,gpsTracker.latitude,gpsTracker.longitude,api)
                    when(it.resultCode)
                    {

                        200->{
                            ImagesHeaderAdapter.tvContainerVis=false
                            ImagesHeaderAdapter.loadmoreVis=true
                            ImagesHeaderAdapter.setloadmoreClickListener {
                                val bundle=Bundle()
                                bundle.putInt("contenttype", Constants.IMAGECONTENT)
                                bundle.putInt("userid",userid)
                                (activity as MainActivity).replaceFragment("userContentsFragment",UserContentsFragment(),bundle)
                            }
                            ImagesAdapter.submitList(it.posts)
                        }
                        100->{
                            ImagesHeaderAdapter.loadmoreVis=false
                            ImagesHeaderAdapter.tvContainerVis=true
                            ImagesHeaderAdapter.guideText="닉네임을 공개한 사진게시물이 없습니다"
                        }
                        else->{

                            binding.srLayout.isRefreshing=false
                            Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            })
            getUserAudioResponse.observe(viewLifecycleOwner,Event.EventObserver(
                onError={
                    binding.srLayout.isRefreshing=false
                    loadingDialog.dismiss()
                    if(!(activity as MainActivity).isConnected!!){

                        binding.srLayout.visibility=View.GONE
                        binding.tvWarn.text=requireContext().getString(R.string.networkdisdconnected)
                        binding.tvWarn.visibility=View.VISIBLE
                        binding.retry.visibility=View.VISIBLE

                    }
                    else
                        snackbar("$it\n 잠시후 다시 시도해주세요",true,"확인")
                }
            ){
                handleResponse(requireContext(), it.resultCode) {
                    getUserVotes(userid,null,null,gpsTracker.latitude,gpsTracker.longitude,api)
                    when(it.resultCode)
                    {
                        200->{
                            AudioHeaderAdapter.tvContainerVis=false
                            AudioHeaderAdapter.loadmoreVis=true
                            AudioHeaderAdapter.setloadmoreClickListener {
                                val bundle=Bundle()
                                bundle.putInt("contenttype", Constants.AUDIOCONTENT)
                                bundle.putInt("userid",userid)
                                (activity as MainActivity).replaceFragment("userContentsFragment",UserContentsFragment(),bundle)
                            }
                            AudioAdapter.submitList(it.posts)
                        }
                        100->{
                            AudioHeaderAdapter.loadmoreVis=false
                            AudioHeaderAdapter.tvContainerVis=true
                            AudioHeaderAdapter.guideText="닉네임을 공개한 음성게시물이 없습니다"
                        }
                        else->{
                            binding.srLayout.isRefreshing=false
                            Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            })
            getUserVoteResponse.observe(viewLifecycleOwner,Event.EventObserver(
                onError={
                    binding.srLayout.isRefreshing=false
                    loadingDialog.dismiss()
                    if(!(activity as MainActivity).isConnected!!){

                        binding.srLayout.visibility=View.GONE
                        binding.tvWarn.text=requireContext().getString(R.string.networkdisdconnected)
                        binding.tvWarn.visibility=View.VISIBLE
                        binding.retry.visibility=View.VISIBLE

                    }
                    else
                        snackbar("$it\n 잠시후 다시 시도해주세요",true,"확인")
                }
            ){
                handleResponse(requireContext(), it.resultCode) {
                    vmUserProfile.getuserPosts(userid,null,null,gpsTracker.latitude,gpsTracker.longitude,10,api)
                    when(it.resultCode)
                    {
                        200->{
                            VoteHeaderAdapter.tvContainerVis=false
                            VoteHeaderAdapter.loadmoreVis=true
                            VoteHeaderAdapter.setloadmoreClickListener {
                                val bundle=Bundle()
                                bundle.putInt("contenttype", Constants.VOTECONTENT)
                                bundle.putInt("userid",userid)
                                (activity as MainActivity).replaceFragment("userContentsFragment",UserContentsFragment(),bundle)
                            }
                            VotesAdapter.submitList(it.posts)
                        }
                        100->{
                            VoteHeaderAdapter.tvContainerVis=true
                            VoteHeaderAdapter.loadmoreVis=false
                            VoteHeaderAdapter.guideText="닉네임을 공개한 투표게시물이 없습니다"
                        }
                        else->{
                            binding.srLayout.isRefreshing=false
                            Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            })
        }
        vmUserProfile.getPostsResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                if(!(activity as MainActivity).isConnected!!){

                    binding.srLayout.visibility=View.GONE
                    binding.tvWarn.text=requireContext().getString(R.string.networkdisdconnected)
                    binding.tvWarn.visibility=View.VISIBLE
                    binding.retry.visibility=View.VISIBLE

                }
                else
                    snackbar("$it\n 잠시후 다시 시도해주세요",true,"확인")
                loadingDialog.dismiss()
            }
        ){
            if(binding.srLayout.isRefreshing)
                binding.srLayout.isRefreshing=false
            loadingDialog.dismiss()
            binding.rvProfile.visibility=View.VISIBLE
            handleResponse(requireContext(), it.resultCode) {
                when(it.resultCode)
                {
                    200->{

                        EveryHeaderAdapter.tvContainerVis=false
                        EveryHeaderAdapter.loadmoreVis=true
                        EveryHeaderAdapter.setloadmoreClickListener {
                            val bundle=Bundle()
                            bundle.putInt("userid", userid)
                            (activity as MainActivity).replaceFragment("userPostsFragment",UserPostsFragment(),bundle)
                        }
                        concatAdapter.notifyDataSetChanged()
                        EveryAdapter.submitList(it.posts)

                    }
                    100->{
                        EveryHeaderAdapter.tvContainerVis=true
                        EveryHeaderAdapter.loadmoreVis=false
                        EveryHeaderAdapter.guideText="닉네임을 공개한 게시물이 없습니다"
                        concatAdapter.notifyDataSetChanged()
                    }
                    else->{
                        binding.srLayout.isRefreshing=false
                        Toast.makeText(requireContext(),"서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        })
    }
    private fun setupRecyclerView()=binding.rvProfile.apply{
        val customDecoration=CustomDecoration(
            ContextCompat.getDrawable(requireContext(), R.drawable.divider),20f,true)
        adapter=concatAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
        addItemDecoration(customDecoration)
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if((activity as MainActivity).isConnected!!){
            vmUserProfile.getuserProfile(userid,api)
        }
        else{
            binding.srLayout.visibility=View.GONE
            binding.tvWarn.text=requireContext().getString(R.string.networkdisdconnected)
            binding.tvWarn.visibility=View.VISIBLE
            binding.retry.visibility=View.VISIBLE
        }

        profileAdapter.setOnFollowClickListener { following->
            if(following==1)
                showunfollowalert()
            else
                vmPerson.toggleFollow(userid,following,api)

        }
        profileAdapter.setOnChatClickListener {
            showchatalert()
        }

    }
    private fun showchatalert(){
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="대화를 요청하시겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            vmUserProfile.requestchat(userid, UUID.randomUUID().toString(),api)
            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    private fun showunfollowalert(){
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="팔로우를 해제하시겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            vmPerson.toggleFollow(userid,following,api)
            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text=""

        super.onResume()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}