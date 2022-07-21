package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
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
import com.example.appportfolio.databinding.FragmentChatrequestsBinding
import com.example.appportfolio.other.CustomDecoration
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import javax.inject.Inject

@AndroidEntryPoint
class ChatRequestsFragment: Fragment(R.layout.fragment_chatrequests), MenuProvider {
    lateinit var binding: FragmentChatrequestsBinding
    lateinit var api: MainApi
    @Inject
    lateinit var chatrequestsAdapter: ChatRequestsAdapter
    @Inject
    lateinit var loadingDialog: LoadingDialog
    private lateinit var viewModel: ChatViewModel
    private lateinit var selectedRequest:ChatRequests
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DataBindingUtil.inflate(inflater,
            R.layout.fragment_chatrequests,container,false)
        (activity as MainActivity).setToolBarVisible("chatRequestsFragment")
        api= RemoteDataSource().buildApi(MainApi::class.java)
        activity?.run{
            viewModel= ViewModelProvider(this)[ChatViewModel::class.java]
        }
        chatrequestsAdapter.setOnAcceptClickListener {
            selectedRequest=it
            viewModel.acceptchat(it.roomid,it.organizer,it.participant,api)
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
        //표시될 대화의 내용에 포함되지않기떄문에 날짜로 아무값이나 넣음
        viewModel.insertChat(chatcontent)
    }
    private fun setupRecyclerView()=binding.rvRequests.apply{
        val customDecoration= CustomDecoration(
            ContextCompat.getDrawable(requireContext(), R.drawable.divider),20f,false)
        layoutManager= LinearLayoutManager(requireContext())
        adapter=chatrequestsAdapter
        itemAnimator=null
        addItemDecoration(customDecoration)
    }
    override fun onResume() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="채팅요청"
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

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}