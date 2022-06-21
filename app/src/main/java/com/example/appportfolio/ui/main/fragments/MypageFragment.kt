package com.example.appportfolio.ui.main.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.ProfileContainerAdapter
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.databinding.FragmentMypageBinding
import com.example.appportfolio.other.Constants.FOLLOWER
import com.example.appportfolio.other.Constants.FOLLOWING

import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MypageFragment: Fragment(R.layout.fragment_mypage) {
    lateinit var vmAuth: AuthViewModel
    lateinit var prefs: SharedPreferences
    lateinit var binding: FragmentMypageBinding
    lateinit var profileContainerAdapter: ProfileContainerAdapter
    lateinit var api: MainApi
    lateinit var authapi: AuthApi
    var profileimgurl:String?=null
    lateinit var gender:String
    var curtoggle:Boolean?=null
    var error=false
    @Inject
    lateinit var loadingDialog: LoadingDialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
        }
        binding= DataBindingUtil.inflate<FragmentMypageBinding>(inflater,
            R.layout.fragment_mypage,container,false)
        api= RemoteDataSource().buildApi(MainApi::class.java)
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, settingpreFragment(), "setting_fragment")
            .commit()
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        profileContainerAdapter=ProfileContainerAdapter()
        setupRecyclerView()
        subsribeToObserver()
        (activity as MainActivity).getmyprofile()
        return binding.root
    }
    val prefListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences?, key: String? ->
            when (key) {
                "chatonoff" -> {
                    val value = prefs.getBoolean("chatonoff", false)
                        if(!error)
                        {
                            curtoggle=!value
                            (activity as MainActivity).toggleChatReceive(value)
                        }
                        else
                        {
                            childFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, settingpreFragment(), "setting_fragment")
                                .commit()
                            error=false
                        }
                    }
                }
        }

    private fun setupRecyclerView()=binding.profilecontainer.apply {
        adapter=profileContainerAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
    }
    private fun subsribeToObserver()
    {
        vmAuth.logoutResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                loadingDialog.show()
            },
            onError={
                loadingDialog.dismiss()
                snackbar(it)
            }
        ){
            loadingDialog.dismiss()
        })
        vmAuth.toggleChatResponse.observe(viewLifecycleOwner, Event.EventObserver(

            onError={
                snackbar(it)
                error=true
                (activity as MainActivity).setChatReceive(curtoggle!!)


            }
        ){
            handleResponse(requireContext(),it.resultCode){

            }
        })
        vmAuth.getmyprofileResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 400)
                    snackbar("서버 오류 발생")
                else {
                        profileContainerAdapter.setuserinfo(it.profileimage,it.nickname,it.gender,it.age!!,it.followingcount!!,it.followercount!!,it.postscount!!)
                        profileContainerAdapter.editprofileVis=true
                        profileContainerAdapter.setOnProfileClickListener {
                            val bundle=Bundle()
                            bundle.putString("profileurl",it.profileimage)
                            bundle.putString("nickname",it.nickname)
                            bundle.putString("gender",it.gender)
                            (activity as MainActivity).replaceFragment("profileEditFragment",ProfileEditFragment(),bundle)
                        }
                        profileContainerAdapter.setOnFollowingClickListener {
                            it.followingcount.let{
                                (activity as MainActivity).replaceFragment("myFollowingFragment",MyFollowingFragment(),null)
                            }

                        }
                        profileContainerAdapter.setOnFollowerClickListener {
                            it.followercount.let{
                                val bundle=Bundle()
                                bundle.putInt("getInfoType",FOLLOWER)
                                (activity as MainActivity).replaceFragment("userFollowFragment",UserFollowFragment(),bundle)
                            }
                        }

                        profileContainerAdapter.notifyDataSetChanged()
                        binding.scrollview.isSmoothScrollingEnabled=false
                        binding.scrollview.fullScroll(ScrollView.FOCUS_UP)
                }
            }
        })

    }
    override fun onResume() {
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
        super.onResume()

    }

    // 리스너 해제
    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }

}

