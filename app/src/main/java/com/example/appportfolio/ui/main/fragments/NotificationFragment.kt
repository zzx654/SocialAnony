package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.NotiAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.Comment
import com.example.appportfolio.data.entities.Noti
import com.example.appportfolio.databinding.FragmentNotificationBinding
import com.example.appportfolio.other.Constants.COMMENTADDED
import com.example.appportfolio.other.Constants.COMMENTLIKED
import com.example.appportfolio.other.Constants.POSTLIKED
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.NotiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class NotificationFragment: Fragment(R.layout.fragment_notification) {
    lateinit var binding: FragmentNotificationBinding
    private lateinit var vmNoti: NotiViewModel
    private var mRootView:View?=null
    lateinit var vmAuth:AuthViewModel
    var isLoading=false
    var beforeitemssize=0
    lateinit var api: MainApi
    lateinit var selectedNoti:Noti
    lateinit var selectedComment:Comment
    @Inject
    lateinit var notiAdapter: NotiAdapter

    @Inject
    lateinit var loadingDialog: LoadingDialog
    @Inject
    lateinit var preferences:UserPreferences
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            binding= DataBindingUtil.inflate<FragmentNotificationBinding>(inflater,
                R.layout.fragment_notification,container,false)
            binding.srLayout.setOnRefreshListener {
                vmNoti.getNotis(null,null,api)
            }
            (activity as MainActivity).checkunreadnoti()
            activity?.run{
                vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
                vmNoti= ViewModelProvider(this).get(NotiViewModel::class.java)
            }
            api= RemoteDataSource().buildApi(MainApi::class.java,
                runBlocking { preferences.authToken.first() })
            binding.scrollview.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if(!v.canScrollVertically(1)){
                    if(!isLoading&&beforeitemssize!=notiAdapter.notis.size) {
                        val curNotis=notiAdapter.differ.currentList
                        if(!curNotis.isEmpty())
                        {
                            val lastNoti=curNotis.last()
                            lastNoti.date
                            lastNoti.notiid
                            vmNoti.getNotis(lastNoti.notiid,lastNoti.date,api)
                        }
                    }
                }
            }
            notiAdapter.setOnNotiClickListener {
                loadingDialog.show()
                vmNoti.readNoti(it.notiid,api)
                vmNoti.setSelectedNoti(it)
            }

            //notiAdapter.differ.submitList(listOf())
            notiAdapter.apply {
                stateRestorationPolicy= RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                setHasStableIds(true)
            }

            vmNoti.getNotis(null,null,api)
            mRootView=binding.root
        }
        setupRecyclerView()
        subsribeToObserver()

        return mRootView
    }

    private fun subsribeToObserver()
    {
        vmNoti.readAllNotiResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {

                    var templist=vmNoti.curnotis.value!!
                    for(i in templist)
                            i.isread=1
                    vmNoti.setNotis(templist)
                    (activity as MainActivity).hidenotibadge()
                }
            }
        })
        vmNoti.deleteAllNotiResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    notiAdapter.differ.submitList(listOf())
                    (activity as MainActivity).hidenotibadge()
                }
            }
        })
        vmNoti.getPostResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
                loadingDialog.dismiss()
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                loadingDialog.dismiss()
                when (it.resultCode) {
                    100 -> Toast.makeText(requireActivity(), "삭제된 게시물입니다", Toast.LENGTH_SHORT)
                        .show()
                    400 -> Toast.makeText(
                        requireActivity(),
                        "차단당하거나 차단한 유저의 게시물입니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> {
                        when (selectedNoti.type) {
                            COMMENTADDED, POSTLIKED, COMMENTLIKED -> {
                                val bundle=Bundle()
                                bundle.putParcelable("post",it.posts[0])
                                (activity as MainActivity).replaceFragment("postFragment",PostFragment(),bundle)
                            }
                            else -> {
                                val bundle=Bundle()
                                bundle.putParcelable("comment",selectedComment)
                                bundle.putParcelable("post",it.posts[0])
                                (activity as MainActivity).replaceFragment("replyFragment",ReplyFragment(),bundle)
                            }
                        }
                    }
                }
            }

        })
        vmNoti.checkSelectedCommentResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
                loadingDialog.dismiss()
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 100) {
                    Toast.makeText(requireContext(), "해당 댓글은 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss()
                } else {
                    selectedComment = it.comments[0]
                    vmNoti.getSelectedPost(selectedNoti.postid, null, null, api)
                }
            }
        })
        vmNoti.selectedNoti.observe(viewLifecycleOwner){
            selectedNoti=it
        }
        vmNoti.readNotiResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
                loadingDialog.dismiss()
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    if (selectedNoti.commentid == null)
                        vmNoti.getSelectedPost(selectedNoti.postid, null, null, api)
                    else
                        vmNoti.checkSelectedComment(
                            null,
                            null,
                            selectedNoti.commentid!!,
                            selectedNoti.postid,
                            api
                        )
                    var templist=vmNoti.curnotis.value!!
                    val selectedindex=templist.indexOf(selectedNoti)
                    templist[selectedindex].isread=1
                    vmNoti.setNotis(templist)
                    if(!notiAdapter.differ.currentList.toList().any{ noti->
                        noti.isread==0
                    })
                        (activity as MainActivity).hidenotibadge()
                }
            }
        })
        vmNoti.getNotiResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                isLoading=true
                binding.notiprogress.visibility=View.VISIBLE

            },
            onError = {
                binding.notiprogress.visibility = View.GONE
                snackbar(it)
            }
        ){

            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    var templist:List<Noti>
                    if(binding.srLayout.isRefreshing)
                    {
                        templist=listOf()
                        binding.srLayout.isRefreshing=false
                        beforeitemssize=0
                    }
                    else{
                        templist=notiAdapter.differ.currentList.toList()
                        beforeitemssize=notiAdapter.notis.size

                    }

                    binding.notiprogress.visibility=View.GONE

                    isLoading=false

                    templist+=it.notis
                    vmNoti.setNotis(templist)
                } else {
                    isLoading = false
                    binding.notiprogress.visibility = View.GONE

                }
            }
        })
        vmNoti.curnotis.observe(viewLifecycleOwner){
            //처음불러올때 새로불러올때 읽을때 전부읽을때
                  notiAdapter.differ.submitList(it)
            notiAdapter.notifyDataSetChanged()
        }
    }
    fun showReadAll()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text=this.getString(R.string.warn_readall)
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {

                dialog.dismiss()
                dialog.cancel()
           //읽음처리하라고서버에 보내고 응답받아서 읽음으로 전부 변경하면됨

            vmNoti.readAllNoti(api)
            //이 내용을 서버응답받은후에 수행
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    fun showDeleteAll()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text=this.getString(R.string.warn_deleteallnoti)
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {

            dialog.dismiss()
            dialog.cancel()
            //삭제하라고 보내고 응답받으면 리스트비우기및 라이브데이터비우기하면됨
            vmNoti.deleteAllNoti(api)
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        super.onResume()

    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.noti_tools, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                //전부 읽음으로 변경하기 구현
                showReadAll()
            }
            R.id.delete->{
                //전부 삭제해버리기 구현
                showDeleteAll()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setupRecyclerView()=binding.rvNoti.apply{
        adapter=notiAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
    }
}