package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.adapters.ChatRoomAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentChatroomBinding
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
@AndroidEntryPoint
class ChatroomFragment: Fragment(R.layout.fragment_chatroom) {
    lateinit var binding: FragmentChatroomBinding
    lateinit var vmAuth: AuthViewModel
    lateinit var vmChat: ChatViewModel
    var check=0
    lateinit var api: MainApi
    @Inject
    lateinit var preferences: UserPreferences

    lateinit var chatroomAdapter: ChatRoomAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        check=1
        activity?.run{
            vmChat= ViewModelProvider(this).get(ChatViewModel::class.java)
        }
        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
        }
        chatroomAdapter=ChatRoomAdapter()
        api= RemoteDataSource().buildApi(MainApi::class.java, runBlocking { preferences.authToken.first() })
        binding= DataBindingUtil.inflate<FragmentChatroomBinding>(inflater,
            R.layout.fragment_chatroom,container,false)
        if(vmChat.mychatRequests.value!!.size==0)
        {
            binding.chatrequests.visibility=View.GONE
        }
        else{
            binding.chatrequests.visibility=View.VISIBLE
            binding.tvrequestnum.text="${vmChat.mychatRequests.value!!.size} >"
        }
        binding.chatrequests.setOnClickListener {
            findNavController().navigate(ChatroomFragmentDirections.actionGlobalChatRequestsFragment())
        }
        subsribeToObserver()
        chatroomAdapter.setroomClickListener {
            findNavController().navigate(ChatroomFragmentDirections.actionGlobalChatFragment(it.userid!!,it.roomid))
        }
        setupRecyclerView()
        chatroomAdapter.differ.submitList(vmChat.mychats.value!!)
        return binding.root
    }
    private fun setupRecyclerView()=binding.rvchatroom.apply{
        adapter=chatroomAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
    }
    private fun subsribeToObserver()
    {
        vmChat.mychats.observe(viewLifecycleOwner){
           chatroomAdapter.differ.submitList(it)
       }
        vmChat.mychatRequests.observe(viewLifecycleOwner){
            if(it.size==0)
            {
                binding.chatrequests.visibility=View.GONE
            }
            else
            {
                binding.chatrequests.visibility=View.VISIBLE
                binding.tvrequestnum.text="${vmChat.mychatRequests.value!!.size} >"
            }
        }
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as MainActivity).binding.title.text="대화"
        super.onResume()
    }
}