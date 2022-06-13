package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.getAge
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.adapters.PostAdapter

import com.example.appportfolio.databinding.FragmentOthersprofileBinding
import com.example.appportfolio.databinding.FragmentPostsBinding
import com.example.appportfolio.other.Constants.PROFILE_HEADER
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class OthersProfileFragment:BasePostFragment(R.layout.fragment_posts) {

    lateinit var binding:FragmentPostsBinding
    private var mRootView:View?=null
    lateinit var userpostAdapter: PostAdapter
    private val userid:Int
        get() = arguments?.getInt("userid",0)!!
    private val from:String
        get() = arguments?.getString("from")!!
    private var following=0
    override val scrollTool: FloatingActionButton
        get() = binding.fbScrollTool
    override val rvPosts: RecyclerView
        get() = binding.rvPosts
    override val loadProgressBar: ProgressBar
        get() = binding.loadProgressBar
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm: OthersProfileViewModel by viewModels()
            return vm
        }
    override val postAdapter: PostAdapter
        get() = userpostAdapter
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    protected val viewModel: OthersProfileViewModel
        get() = basePostViewModel as OthersProfileViewModel

    private lateinit var vmPerson:BasePersonViewModel
    private lateinit var vmToggle:applyFollowViewModel
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            vmToggle=ViewModelProvider(requireActivity()).get(applyFollowViewModel::class.java)
            if(from=="searchPersonFragment")
                vmPerson=ViewModelProvider(requireActivity()).get(SearchPersonViewModel::class.java)
            else
                vmPerson=ViewModelProvider(requireActivity()).get(MyFollowingViewModel::class.java)
            binding= DataBindingUtil.inflate<FragmentPostsBinding>(inflater,
                R.layout.fragment_posts,container,false)
            userpostAdapter=PostAdapter()

            setView()
            (activity as MainActivity).setToolBarVisible("othersProfileFragment")
            following=arguments?.getInt("follow")!!
            userpostAdapter.setfollowing(following)
            refreshPosts()
            mRootView=binding.root
        }

        return mRootView
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getuserProfile(userid,api)
        userpostAdapter.setOnFollowClickListener { following->
            if(following==1)
                showunfollowalert()
            else
                vmPerson.toggleFollow(userid,following,api)

        }
        userpostAdapter.setOnChatClickListener {
            showchatalert()
        }
        vmPerson.togglefollowResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    if (following == 1) {
                        //binding.imgfollow.setImageResource(R.drawable.favorite_off)
                        //following = 0
                            userpostAdapter.setfollowing(0)
                        userpostAdapter.notifyItemChanged(0)
                        Toast.makeText(
                            requireContext(),
                            "${userpostAdapter.usernickname}님 팔로우를 해제했습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        //binding.imgfollow.setImageResource(R.drawable.favorite_on)
                        //following = 1
                        userpostAdapter.setfollowing(1)
                        userpostAdapter.notifyItemChanged(0)
                        Toast.makeText(
                            requireContext(),
                            "${userpostAdapter.usernickname}님을 팔로우 했습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    vmToggle.setcurtoggle(userid,userpostAdapter.followingperson)
                }
            }
        })
        viewModel.requestchatResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
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

        viewModel.getprofileResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    userpostAdapter.setuserinfo(it.profileimage,it.nickname,it.gender,it.age)
                    userpostAdapter.setheadertype(PROFILE_HEADER)
                    userpostAdapter.notifyDataSetChanged()
                    (activity as MainActivity).binding.title.text = it.nickname
                    userpostAdapter.setOnProfileClickListener { profileimg->
                        val bundle= Bundle()
                        bundle.putString("image",profileimg)
                        (activity as MainActivity).replaceFragment("imageFragment",
                            ImageFragment(),bundle)
                    }

                }
            }
        })
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
            viewModel.requestchat(userid, UUID.randomUUID().toString(),api)
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
        val curPosts=postAdapter.currentList
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
            viewModel.getuserPosts(userid,lastpostnum,lastpostdate,gpsTracker.latitude,gpsTracker.longitude,api)
        }
        else{
            viewModel.getuserPosts(userid,lastpostnum,lastpostdate,null,null,api)
        }
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