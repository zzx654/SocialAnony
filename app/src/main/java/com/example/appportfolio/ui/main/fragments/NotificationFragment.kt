package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.NotiAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.Comment
import com.example.appportfolio.data.entities.Noti
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.FragmentNotificationBinding
import com.example.appportfolio.other.Constants.COMMENTADDED
import com.example.appportfolio.other.Constants.COMMENTLIKED
import com.example.appportfolio.other.Constants.PAGE_SIZE
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
    lateinit var api: MainApi
    lateinit var selectedNoti:Noti
    lateinit var selectedComment:Comment
    private var isScrolling=false
    private var isLast=false
    private var isLoading=false
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
                isLast=false
                vmNoti.getNotis(null,null,api)
            }
            (activity as MainActivity).checkunreadnoti()
            activity?.run{
                vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
                vmNoti= ViewModelProvider(this).get(NotiViewModel::class.java)
            }
            api= RemoteDataSource().buildApi(MainApi::class.java,
                runBlocking { preferences.authToken.first() })
            notiAdapter.setOnNotiClickListener {
                loadingDialog.show()
                vmNoti.readNoti(it.notiid!!,api)
                vmNoti.setSelectedNoti(it)
            }
            notiAdapter.apply {
                //stateRestorationPolicy= RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
           }

            vmNoti.getNotis(null,null,api)
            mRootView=binding.root
        }
        setupRecyclerView()

        subsribeToObserver()

        return mRootView
    }
    val scrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            val totalItemCount = recyclerView.adapter!!.itemCount - 1
            if(notiAdapter.currentList.isNotEmpty())
            {
                notiAdapter.currentList.last().notiid?.let{
                    if(!recyclerView.canScrollVertically(1)&&(lastVisibleItemPosition == totalItemCount)&&!isLoading&&isScrolling&&!isLast&&notiAdapter.currentList.size>=PAGE_SIZE){
                        isScrolling=false
                            vmNoti.getNotis(it,notiAdapter.currentList.last().date,api)
                    }
                }
            }
        }
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling=true
        }
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
            isLast=false
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    notiAdapter.submitList(listOf())
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
                    if(!notiAdapter.currentList.toList().any{ noti->
                        noti.isread==0
                    })
                        (activity as MainActivity).hidenotibadge()
                }
            }
        })
        vmNoti.getNotiResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                if(!binding.srLayout.isRefreshing)
                {
                    if(notiAdapter.currentList.isEmpty())
                        binding.loadProgressBar.visibility=View.VISIBLE
                    else{
                        if(notiAdapter.currentList.isNotEmpty())
                        {
                            var templist=notiAdapter.currentList.toList()
                            templist+=listOf(
                                Noti(null,0,"","","",null,0)
                            )
                            notiAdapter.submitList(templist)
                        }
                    }
                }
                isLoading=true
            },
            onError = {
                snackbar(it)
                if(!binding.srLayout.isRefreshing)
                {
                    if(notiAdapter.currentList.isEmpty())
                        binding.loadProgressBar.visibility=View.GONE
                    else{
                        var currentllist=notiAdapter.currentList.toMutableList()
                        currentllist.removeLast()
                        notiAdapter.submitList(currentllist)
                    }

                }
                else
                    binding.srLayout.isRefreshing=false
            }
        ){
            isLoading = false
            var currentllist=notiAdapter.currentList.toMutableList()
            if(!binding.srLayout.isRefreshing)
            {
                if(notiAdapter.currentList.isEmpty())
                    binding.loadProgressBar.visibility=View.GONE
                else if(notiAdapter.currentList.size>=20) {
                    currentllist.removeLast()
                }
            }
            handleResponse(requireContext(),it.resultCode) {
                var notis=currentllist.toList()
                if(it.resultCode==200) {
                    if(binding.srLayout.isRefreshing)
                    {
                        vmNoti.setNotis(it.notis)
                        binding.srLayout.isRefreshing=false
                    }
                    else
                    {
                        if(notis.isEmpty())
                            binding.rvNoti.scrollToPosition(0)
                        notis+=it.notis
                        vmNoti.setNotis(notis)
                    }
                    isLoading=false
                }
                else{
                    vmNoti.setNotis(notis)
                    if(notis.size>20)
                        snackbar("더이상 표시할 알림이 없습니다")
                    isLast=true
                    binding.loadProgressBar.visibility=View.GONE
                    binding.srLayout.isRefreshing=false
                }

            }
        })
        vmNoti.curnotis.observe(viewLifecycleOwner){
                  notiAdapter.submitList(it)
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
        setHasFixedSize(true)
        itemAnimator=null
        addOnScrollListener(this@NotificationFragment.scrollListener)
        setItemViewCacheSize(20)
    }
}