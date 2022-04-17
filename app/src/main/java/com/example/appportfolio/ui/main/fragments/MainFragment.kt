package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.appportfolio.R
import com.example.appportfolio.databinding.FragmentMainBinding
import com.example.appportfolio.other.Constants.TAG_CHAT
import com.example.appportfolio.other.Constants.TAG_HOME
import com.example.appportfolio.other.Constants.TAG_MYPAGE
import com.example.appportfolio.other.Constants.TAG_NOTI
import com.example.appportfolio.other.Constants.TAG_POST
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.NavViewModel


class MainFragment: Fragment(R.layout.fragment_img) {
    lateinit var binding: FragmentMainBinding
    lateinit var vmNav: NavViewModel
    private var curFrag= TAG_HOME
    private var mRootView:View?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null){
            binding= DataBindingUtil.inflate<FragmentMainBinding>(inflater,
                R.layout.fragment_main,container,false)
            setFragment(TAG_HOME,HomeFragment())
            mRootView=binding.root
        }
        activity?.run{
            vmNav= ViewModelProvider(this).get(NavViewModel::class.java)
        }
        subscribeToObserver()
        return mRootView
    }
    private fun setFragment(tag: String, fragment: Fragment?){
        val manager: FragmentManager = childFragmentManager
        val ft: FragmentTransaction = manager.beginTransaction()
        //트랜잭션에 tag로 전달된 fragment가 없을 경우 add
        if(tag== TAG_POST)
        {
            findNavController().navigate(MainFragmentDirections.actionGlobalUploadFragment())
            vmNav.setFrag(curFrag)
        }
        else
        {
            if(manager.findFragmentByTag(tag) == null){
                ft.add(R.id.mainNaviFragmentContainer, fragment!!, tag)

            }
            //작업이 수월하도록 manager에 add되어있는 fragment들을 변수로 할당해둠
            val home = manager.findFragmentByTag(TAG_HOME)
            val chat = manager.findFragmentByTag(TAG_CHAT)
            val noti = manager.findFragmentByTag(TAG_NOTI)
            val mypage = manager.findFragmentByTag(TAG_MYPAGE)
            //모든 프래그먼트 hide
            if(home!=null){
                ft.hide(home)
            }
            if(chat!=null){
                ft.hide(chat)
            }
            if(noti!=null){
                ft.hide(noti)
            }
            if(mypage!=null)
            {
                ft.hide(mypage)
            }
            //선택한 항목에 따라 그에 맞는 프래그먼트만 show
            if(tag == TAG_HOME){
                if(home!=null){
                    ft.show(home)
                }
            }
            else if(tag == TAG_CHAT){
                if(chat!=null){
                    ft.show(chat)
                }
                setHasOptionsMenu(true)

            }
            else if(tag == TAG_MYPAGE){
                if(mypage!=null){
                    ft.show(mypage)
                }
                setHasOptionsMenu(true)

            }
            else if(tag == TAG_NOTI)
            {
                if(noti!=null)
                    ft.show(noti)
            }
            //마무리
            ft.commitAllowingStateLoss()
        }
    }
    fun subscribeToObserver()
    {
        vmNav.destinationFragment.observe(viewLifecycleOwner){
            if(curFrag!=it)
            {
                if(it!= TAG_POST)
                    curFrag=it

                when(it)
                {
                    TAG_HOME->setFragment(TAG_HOME,HomeFragment())
                    TAG_CHAT->{
                        setFragment(TAG_CHAT,ChatroomFragment())
                        (activity as MainActivity).binding.title.text="대화"
                    }
                    TAG_NOTI->{
                        setFragment(TAG_NOTI,NotificationFragment())
                        (activity as MainActivity).binding.title.text="알림"
                    }
                    TAG_MYPAGE->{
                        setFragment(TAG_MYPAGE,MypageFragment())
                        (activity as MainActivity).binding.title.text="마이페이지"
                    }
                    TAG_POST->setFragment(TAG_POST,null)
                }
                when(it)
                {
                    TAG_HOME->{
                        (activity as MainActivity).binding.toolbar.visibility=View.GONE
                        (activity as MainActivity).binding.linetop.visibility=View.GONE
                    }
                    else->{
                        (activity as MainActivity).supportActionBar?.apply {
                            setDisplayHomeAsUpEnabled(false)
                            setDisplayShowTitleEnabled(false)
                        }
                        (activity as MainActivity).binding.linetop.visibility=View.VISIBLE
                        (activity as MainActivity).binding.toolbar.visibility=View.VISIBLE
                    }
                }
            }
            else
            {
                when(it)
                {
                    TAG_CHAT->{
                        (activity as MainActivity).binding.title.text="대화"
                    }
                    TAG_NOTI->{
                        (activity as MainActivity).binding.title.text="알림"
                    }
                    TAG_MYPAGE->{
                        (activity as MainActivity).binding.title.text="마이페이지"
                    }
                }
            }
        }
    }

}