package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
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
import com.example.appportfolio.ui.main.viewmodel.NotiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class NotificationFragment: Fragment(R.layout.fragment_notification) {
    lateinit var binding: FragmentNotificationBinding
    private val vmNoti: NotiViewModel by viewModels()
    lateinit var vmAuth:AuthViewModel
    var isLoading=false
    var beforeitemssize=0
    lateinit var api: MainApi
    lateinit var selectedNoti:Noti
    lateinit var selectedComment:Comment
    @Inject
    lateinit var notiAdapter: NotiAdapter
    @Inject
    lateinit var preferences:UserPreferences
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentNotificationBinding>(inflater,
            R.layout.fragment_notification,container,false)
        binding.srLayout.setOnRefreshListener {
            notiAdapter.differ.submitList(listOf())
            vmNoti.clearnotis()
        }
        (activity as MainActivity).checkunreadnoti()
        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
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
            vmNoti.readNoti(it.notiid,api)
            vmNoti.setSelectedNoti(it)
        }
        setupRecyclerView()
        subsribeToObserver()
        notiAdapter.differ.submitList(listOf())
        vmNoti.clearnotis()
        return binding.root
    }

    private fun subsribeToObserver()
    {
        vmNoti.readAllNotiResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            if(it.resultCode==200)
            {
                vmNoti.readAllNoti(api)
                for(i in notiAdapter.notis.indices)
                {
                    notiAdapter.notis[i].apply{
                        this.isread=1
                        notiAdapter.notifyItemChanged(i)
                    }
                }
                (activity as MainActivity).hidenotibadge()
            }
        })
        vmNoti.deleteAllNotiResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            if(it.resultCode==200)
            {
                notiAdapter.differ.submitList(listOf())
                vmNoti.clearnotis()
                (activity as MainActivity).hidenotibadge()
            }
        })
        vmNoti.getPostResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            when(it.resultCode)
            {
                100->Toast.makeText(requireActivity(),"삭제된 게시물입니다",Toast.LENGTH_SHORT).show()
                400->Toast.makeText(requireActivity(),"차단당하거나 차단한 유저의 게시물입니다",Toast.LENGTH_SHORT).show()
                else->{
                    when(selectedNoti.type){
                        COMMENTADDED,POSTLIKED,COMMENTLIKED->findNavController().navigate(NotificationFragmentDirections.actionGlobalPostFragment(it.posts[0]))
                        else->   findNavController().navigate(PostFragmentDirections.actionGlobalReplyFragment(selectedComment,it.posts[0]))
                    }
                }
            }

        })
        vmNoti.checkSelectedCommentResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            if(it.resultCode==100)
            {
                Toast.makeText(requireContext(),"해당 댓글은 삭제되었습니다", Toast.LENGTH_SHORT).show()
            }
            else
            {
                selectedComment=it.comments[0]
                vmNoti.getSelectedPost(selectedNoti.postid,null,null,api)
            }
        })
        vmNoti.selectedNoti.observe(viewLifecycleOwner){
            selectedNoti=it
        }
        vmNoti.readNotiResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            if(it.resultCode==200)
            {
                if(selectedNoti.commentid==null)
                    vmNoti.getSelectedPost(selectedNoti.postid,null,null,api)
                else
                    vmNoti.checkSelectedComment(null,null,selectedNoti.commentid!!,selectedNoti.postid,api)
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
            if(binding.srLayout.isRefreshing)
                binding.srLayout.isRefreshing=false

            if(it.resultCode==200) {

                vmNoti.addnotis(it.notis)
            }
            else{
                isLoading=false
                binding.notiprogress.visibility=View.GONE
                if(!notiAdapter.differ.currentList.isEmpty()) {
                   // snackbar("더이상 표시할 알림이 없습니다")
                }

            }
        })
        vmNoti.curnotis.observe(viewLifecycleOwner){
            if(it.isEmpty())
            {
                vmNoti.getNotis(null,null,api)
            }
            else
            {
                beforeitemssize=notiAdapter.notis.size
                vmNoti.setbeforeSize(notiAdapter.notis.size)
                binding.notiprogress.visibility=View.GONE
                notiAdapter.differ.submitList(it)
                isLoading=false
                var unreadexist=false
                for(noti in it){
                    if(noti.isread==0)
                    {
                        unreadexist=true
                        break
                    }
                }
                if(unreadexist)
                {
                    (activity as MainActivity).shownotibadge()//읽고나서 response받은후에 이런식으로 다시 리스트검사해서 isread 0인거없으면 hide하기
                }

            }
        }
        vmNoti.beforesize.observe(viewLifecycleOwner){
            beforeitemssize=it
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
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_complete)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="알림"
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