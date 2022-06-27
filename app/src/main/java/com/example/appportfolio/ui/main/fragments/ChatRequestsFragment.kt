package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.getTodayString
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.ChatRequestsAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.data.entities.ChatData
import com.example.appportfolio.data.entities.ChatRequests
import com.example.appportfolio.data.entities.Chatroom
import com.example.appportfolio.databinding.FragmentChatrequestsBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import javax.inject.Inject

@AndroidEntryPoint
class ChatRequestsFragment: Fragment(R.layout.fragment_chatrequests) {
    lateinit var binding: FragmentChatrequestsBinding
    lateinit var api: MainApi
    @Inject
    lateinit var chatrequestsAdapter: ChatRequestsAdapter
    @Inject
    lateinit var loadingDialog: LoadingDialog
    private lateinit var viewModel: ChatViewModel
    private lateinit var selectedRequest:ChatRequests
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentChatrequestsBinding>(inflater,
            R.layout.fragment_chatrequests,container,false)
        (activity as MainActivity).setToolBarVisible("chatRequestsFragment")
        api= RemoteDataSource().buildApi(MainApi::class.java)
        activity?.run{
            viewModel= ViewModelProvider(this).get(ChatViewModel::class.java)
        }
        chatrequestsAdapter.setOnAcceptClickListener {
            selectedRequest=it
            viewModel.acceptchat(it.roomid,it.organizer,it.participant,getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),api)
        }
        chatrequestsAdapter.setOnRefuseClickListener {
            viewModel.refusechat(it.roomid,it.participant,api)
        }
        subscribeToObserver()
        setupRecyclerView()
        chatrequestsAdapter.submitList(viewModel.mychatRequests.value)
        return binding.root
    }
    private fun subscribeToObserver()
    {
        viewModel.acceptchatResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
            },
            onLoading={
                loadingDialog.show()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                chatrequestsAdapter.submitList(it.requests)
                viewModel.setChatRequests(it.requests)
                setnewChats(selectedRequest)
            }


        })
        viewModel.refusechatResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
            },
            onLoading={
                loadingDialog.show()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                chatrequestsAdapter.submitList(it.requests)
            }

        })
        viewModel.mychatRequests.observe(viewLifecycleOwner){
            chatrequestsAdapter.submitList(it)
        }
    }
    private fun setnewChats(selectedRequest: ChatRequests){
        val chatcontent=ChatData(null,selectedRequest.organizer,selectedRequest.roomid, getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),"start","대화가 시작되었습니다",1)
        viewModel.insertChat(chatcontent,0)
        var oldChatlist=viewModel.mychats.value!!
        //var newChatlist:List<Chatroom> = listOf(Chatroom(selectedRequest.organizer,selectedRequest.profileimage,selectedRequest.gender,selectedRequest.nickname,1,selectedRequest.participant,
         //   selectedRequest.roomid,getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),"start",
          //  "대화가 시작되었습니다",1))+oldChatlist
        //viewModel.setChats(newChatlist)
    }
    private fun setupRecyclerView()=binding.rvRequests.apply{
        layoutManager= LinearLayoutManager(requireContext())
        adapter=chatrequestsAdapter
        itemAnimator=null
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="채팅요청"
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